package org.qbicc.plugin.llvm;

import static java.lang.Math.max;

import io.smallrye.common.constraint.Assert;
import org.qbicc.context.CompilationContext;
import org.qbicc.graph.BasicBlock;
import org.qbicc.graph.literal.Literal;
import org.qbicc.graph.literal.LiteralFactory;
import org.qbicc.graph.schedule.Schedule;
import org.qbicc.machine.llvm.FunctionAttributes;
import org.qbicc.machine.llvm.FunctionDefinition;
import org.qbicc.machine.llvm.Global;
import org.qbicc.machine.llvm.LLValue;
import org.qbicc.machine.llvm.Linkage;
import org.qbicc.machine.llvm.Module;
import org.qbicc.machine.llvm.ModuleFlagBehavior;
import org.qbicc.machine.llvm.RuntimePreemption;
import org.qbicc.machine.llvm.ThreadLocalStorageModel;
import org.qbicc.machine.llvm.Types;
import org.qbicc.machine.llvm.Values;
import org.qbicc.object.Data;
import org.qbicc.object.DataDeclaration;
import org.qbicc.object.Function;
import org.qbicc.object.FunctionDeclaration;
import org.qbicc.object.GlobalXtor;
import org.qbicc.object.ProgramModule;
import org.qbicc.object.ProgramObject;
import org.qbicc.object.Section;
import org.qbicc.object.ThreadLocalMode;
import org.qbicc.type.ArrayType;
import org.qbicc.type.CompoundType;
import org.qbicc.type.FunctionType;
import org.qbicc.type.PointerType;
import org.qbicc.type.TypeSystem;
import org.qbicc.type.UnsignedIntegerType;
import org.qbicc.type.ValueType;
import org.qbicc.type.VariadicType;
import org.qbicc.type.definition.DefinedTypeDefinition;
import org.qbicc.type.definition.MethodBody;
import org.qbicc.type.definition.element.ExecutableElement;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

final class LLVMModuleGenerator {
    private final CompilationContext context;
    private final int picLevel;
    private final int pieLevel;

    LLVMModuleGenerator(final CompilationContext context, final int picLevel, final int pieLevel) {
        this.context = context;
        this.picLevel = picLevel;
        this.pieLevel = pieLevel;
    }

    public Path processProgramModule(final ProgramModule programModule) {
        DefinedTypeDefinition def = programModule.getTypeDefinition();
        Path outputFile = context.getOutputFile(def, "ll");
        final Module module = Module.newModule();
        final LLVMModuleNodeVisitor moduleVisitor = new LLVMModuleNodeVisitor(module, context);
        final LLVMModuleDebugInfo debugInfo = new LLVMModuleDebugInfo(module, context);
        final LLVMPseudoIntrinsics pseudoIntrinsics = new LLVMPseudoIntrinsics(module);

        if (picLevel != 0) {
            module.addFlag(ModuleFlagBehavior.Max, "PIC Level", Types.i32, Values.intConstant(picLevel));
        }

        if (pieLevel != 0) {
            module.addFlag(ModuleFlagBehavior.Max, "PIE Level", Types.i32, Values.intConstant(pieLevel));
        }

        // declare debug function here
        org.qbicc.machine.llvm.Function decl = module.declare("llvm.dbg.addr");
        decl.returns(Types.void_);
        decl.param(Types.metadata).param(Types.metadata).param(Types.metadata);

        // declare global ctors and dtors
        processXtors(programModule.constructors(), "llvm.global_ctors", module, moduleVisitor);
        processXtors(programModule.destructors(), "llvm.global_dtors", module, moduleVisitor);

        for (Section section : programModule.sections()) {
            String sectionName = section.getName();
            for (ProgramObject item : section.contents()) {
                String name = item.getName();
                Linkage linkage = map(item.getLinkage());
                if (item instanceof Function fn) {
                    ExecutableElement element = fn.getOriginalElement();
                    MethodBody body = fn.getBody();
                    boolean isExact = item == context.getExactFunction(element);
                    if (body == null) {
                        context.error("Function `%s` has no body", name);
                        continue;
                    }
                    BasicBlock entryBlock = body.getEntryBlock();
                    FunctionDefinition functionDefinition = module.define(name).linkage(linkage);
                    LLValue topSubprogram;

                    if (isExact) {
                        topSubprogram = debugInfo.getDebugInfoForFunction(element).getSubprogram();
                        functionDefinition.meta("dbg", topSubprogram);
                    } else {
                        topSubprogram = debugInfo.createThunkSubprogram(fn).asRef();
                        functionDefinition.meta("dbg", topSubprogram);
                    }
                    functionDefinition.attribute(FunctionAttributes.framePointer("non-leaf"));
                    functionDefinition.attribute(FunctionAttributes.uwtable);
                    functionDefinition.gc("statepoint-example");
                    if (fn.isNoReturn()) {
                        functionDefinition.attribute(FunctionAttributes.noreturn);
                    }

                    LLVMNodeVisitor nodeVisitor = new LLVMNodeVisitor(context, module, debugInfo, pseudoIntrinsics, topSubprogram, moduleVisitor, Schedule.forMethod(entryBlock), fn, functionDefinition);
                    if (! sectionName.equals(CompilationContext.IMPLICIT_SECTION_NAME)) {
                        functionDefinition.section(sectionName);
                    }

                    nodeVisitor.execute();
                } else if (item instanceof FunctionDeclaration fn) {
                    decl = module.declare(name).linkage(linkage);
                    FunctionType fnType = fn.getValueType();
                    decl.returns(moduleVisitor.map(fnType.getReturnType()));
                    int cnt = fnType.getParameterCount();
                    for (int i = 0; i < cnt; i++) {
                        ValueType type = fnType.getParameterType(i);
                        if (type instanceof VariadicType) {
                            if (i < cnt - 1) {
                                throw new IllegalStateException("Variadic type as non-final parameter type");
                            }
                            decl.variadic();
                        } else {
                            decl.param(moduleVisitor.map(type));
                        }
                    }
                } else if (item instanceof DataDeclaration) {
                    Global obj = module.global(moduleVisitor.map(item.getValueType())).linkage(Linkage.EXTERNAL);
                    ThreadLocalMode tlm = item.getThreadLocalMode();
                    if (tlm != null) {
                        obj.threadLocal(map(tlm));
                    }
                    obj.asGlobal(item.getName());
                    if (! sectionName.equals(CompilationContext.IMPLICIT_SECTION_NAME)) {
                        obj.section(sectionName);
                    }
                    if (item.getAddrspace() != 0) {
                        obj.addressSpace(item.getAddrspace());
                    }
                } else if (item instanceof Data data) {
                    Literal value = (Literal) data.getValue();
                    Global obj = module.global(moduleVisitor.map(data.getValueType()));
                    if (value != null) {
                        obj.value(moduleVisitor.map(value));
                    } else {
                        obj.value(Values.zeroinitializer);
                    }
                    obj.alignment(data.getValueType().getAlign());
                    obj.linkage(linkage);
                    ThreadLocalMode tlm = data.getThreadLocalMode();
                    if (tlm != null) {
                        obj.threadLocal(map(tlm));
                    }
                    if (data.isDsoLocal()) {
                        obj.preemption(RuntimePreemption.LOCAL);
                    }
                    if (! sectionName.equals(CompilationContext.IMPLICIT_SECTION_NAME)) {
                        obj.section(sectionName);
                    }
                    if (item.getAddrspace() != 0) {
                        obj.addressSpace(data.getAddrspace());
                    }
                    obj.asGlobal(data.getName());
                } else {
                    throw new IllegalStateException();
                }
            }
        }
        try {
            Path parent = outputFile.getParent();
            if (! Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                module.writeTo(writer);
            }
        } catch (IOException e) {
            context.error("Failed to write \"%s\": %s", outputFile, e.getMessage());
            try {
                Files.deleteIfExists(outputFile);
            } catch (IOException e2) {
                context.warning("Failed to clean \"%s\": %s", outputFile, e.getMessage());
            }
        }
        return outputFile;
    }

    private void processXtors(final List<GlobalXtor> xtors, final String xtorName, Module module, LLVMModuleNodeVisitor moduleVisitor) {
        if (! xtors.isEmpty()) {
            TypeSystem ts = context.getTypeSystem();
            LiteralFactory lf = context.getLiteralFactory();
            int xtorSize = 0;
            // this special type has a fixed size and layout
            UnsignedIntegerType u32 = ts.getUnsignedInteger32Type();
            CompoundType.Member priorityMember = ts.getUnalignedCompoundTypeMember("priority", u32, 0);
            xtorSize += u32.getSize();
            PointerType voidFnPtrType = ts.getFunctionType(ts.getVoidType()).getPointer();
            CompoundType.Member fnMember = ts.getUnalignedCompoundTypeMember("fn", voidFnPtrType, xtorSize);
            xtorSize += voidFnPtrType.getSize();
            PointerType u8ptr = ts.getUnsignedInteger8Type().getPointer();
            CompoundType.Member dataMember = ts.getUnalignedCompoundTypeMember("data", u8ptr, xtorSize);
            xtorSize += u8ptr.getSize();
            CompoundType xtorType = ts.getCompoundType(CompoundType.Tag.NONE, "xtor_t", xtorSize, 1, () -> List.of(
                priorityMember, fnMember, dataMember
            ));
            // special global
            ArrayType arrayType = ts.getArrayType(xtorType, xtors.size());
            Global global_ctors = module.global(moduleVisitor.map(arrayType));
            global_ctors.appending();
            global_ctors.value(moduleVisitor.map(lf.literalOf(
                arrayType, xtors.stream().map(g -> lf.literalOf(xtorType, Map.of(
                    priorityMember, lf.literalOf(g.getPriority()),
                    fnMember, lf.bitcastLiteral(lf.literalOf(g.getFunction()), voidFnPtrType),
                    dataMember, lf.nullLiteralOfType(u8ptr)
                ))).toList()
            )));
            global_ctors.asGlobal(xtorName);
        }
    }

    Linkage map(org.qbicc.object.Linkage linkage) {
        switch (linkage) {
            case COMMON: return Linkage.COMMON;
            case INTERNAL: return Linkage.INTERNAL;
            case PRIVATE: return Linkage.PRIVATE;
            case WEAK: return Linkage.EXTERN_WEAK;
            case EXTERNAL: return Linkage.EXTERNAL;
            default: throw Assert.impossibleSwitchCase(linkage);
        }
    }

    ThreadLocalStorageModel map(ThreadLocalMode mode) {
        switch (mode) {
            case GENERAL_DYNAMIC: return ThreadLocalStorageModel.GENERAL_DYNAMIC;
            case LOCAL_DYNAMIC: return ThreadLocalStorageModel.LOCAL_DYNAMIC;
            case INITIAL_EXEC: return ThreadLocalStorageModel.INITIAL_EXEC;
            case LOCAL_EXEC: return ThreadLocalStorageModel.LOCAL_EXEC;
            default: throw Assert.impossibleSwitchCase(mode);
        }
    }
}
