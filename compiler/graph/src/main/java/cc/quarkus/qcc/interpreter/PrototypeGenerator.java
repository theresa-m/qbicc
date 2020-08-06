package cc.quarkus.qcc.interpreter;

import cc.quarkus.qcc.graph.BooleanType;
import cc.quarkus.qcc.graph.ClassType;
import cc.quarkus.qcc.graph.FloatType;
import cc.quarkus.qcc.graph.IntegerType;
import cc.quarkus.qcc.graph.Type;
import cc.quarkus.qcc.graph.WordType;
import cc.quarkus.qcc.type.definition.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static cc.quarkus.qcc.interpreter.CodegenUtils.*;
import static cc.quarkus.qcc.type.definition.ClassFile.ACC_PUBLIC;
import static cc.quarkus.qcc.type.definition.ClassFile.ACC_VOLATILE;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class PrototypeGenerator {
    private static Map<DefinedTypeDefinition, Prototype> prototypes = new HashMap<>();
    private static ClassDefiningClassLoader cdcl = new ClassDefiningClassLoader(PrototypeGenerator.class.getClassLoader());
    private static final String PACKAGE = FieldContainer.class.getPackageName().replace('.', '/');

    public static Prototype getPrototype(DefinedTypeDefinition definition) {
        return prototypes.computeIfAbsent(definition, PrototypeGenerator::generate);
    }

    private static Prototype generate(DefinedTypeDefinition defined) {
        VerifiedTypeDefinition verified = defined.verify();
        ClassType classType = verified.getClassType();

        // prepend qcc so they're isolated from containing VM
        String className = PACKAGE + '/' + classType.getClassName().replace('/', '_');

        ClassType superType = classType.getSuperClass();
        String superName;

        if (superType == null) {
            superName = "java/lang/Object";
        } else {
            Prototype superProto = generate(superType.getDefinition());
            superName = superProto.getClassName();
        }

        Printer printer = new Textifier ();
        this.method = new TraceMethodVisitor(mv, printer);
        ClassWriter proto = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);

        proto.visit(Opcodes.V9, verified.getModifiers(), p(className), null, p(superName), arrayOf(p(FieldContainer.class)));

        // generate fields
        Map<Integer, ResolvedFieldDefinition> indexedFields = new HashMap<>();
        verified.eachField(
                (field) -> {
                    ResolvedFieldDefinition resolved = field.resolve();
                    indexedFields.put(indexedFields.size(), resolved);
                    proto.visitField(
                            field.getModifiers() | ACC_VOLATILE,
                            field.getName(),
                            ci(javaTypeFromFieldType(resolved.getType())),
                            null, null);
                });

        // generate accessor methods
        int size = indexedFields.size();

        { // getObjectVolatile
            MethodVisitor getObjectVolatile = proto.visitMethod(ACC_PUBLIC, "getObjectVolatile", sig(JavaObject.class, int.class), null, null);
            getObjectVolatile.visitCode();

            Label govDefault = new Label();
            Label[] govLabels = IntStream.range(0, size).mapToObj(i -> {
                if (javaTypeFromFieldType(indexedFields.get(i).getType()) == Object.class) {
                    return new Label();
                }
                return govDefault;
            }).toArray(Label[]::new);

            getObjectVolatile.visitVarInsn(Opcodes.ILOAD, 1);
            getObjectVolatile.visitTableSwitchInsn(0, size, govDefault, govLabels);

            indexedFields.forEach((i, f) -> {
                Label l = govLabels[i];
                if (l != govDefault) {
                    getObjectVolatile.visitLabel(l);
                    getObjectVolatile.visitVarInsn(Opcodes.ALOAD, 0);
                    getObjectVolatile.visitFieldInsn(Opcodes.GETFIELD, className, f.getName(), ci(Object.class));
                    getObjectVolatile.visitInsn(Opcodes.ARETURN);
                }
            });
            getObjectVolatile.visitLabel(govDefault);
            getObjectVolatile.visitTypeInsn(Opcodes.NEW, p(RuntimeException.class));
            getObjectVolatile.visitLdcInsn("not an object field");
            getObjectVolatile.visitMethodInsn(Opcodes.INVOKESPECIAL, p(RuntimeException.class), "<init>", sig(void.class, String.class), false);
            getObjectVolatile.visitInsn(Opcodes.ATHROW);

            getObjectVolatile.visitMaxs(1, 1);
            getObjectVolatile.visitEnd();
        }

        proto.visitEnd();

        byte[] bytecode = proto.toByteArray();



        return new PrototypeImpl(classType, className, bytecode, cdcl);
    }

    private static class ClassDefiningClassLoader extends ClassLoader {
        ClassDefiningClassLoader(ClassLoader parent) {
            super(parent);
        }
        public Class<?> defineAndResolveClass(String name, byte[] b, int off, int len) {
            Class<?> cls = super.defineClass(name, b, off, len);
            resolveClass(cls);
            return cls;
        }
    }

    public static class PrototypeImpl implements Prototype {
        private final ClassType classType;
        private final String className;
        private final byte[] bytecode;
        private final ClassDefiningClassLoader cdcl;
        private final Class<? extends FieldContainer> cls;

        PrototypeImpl(ClassType classType, String className, byte[] bytecode, ClassDefiningClassLoader cdcl) {
            this.classType = classType;
            this.className = className;
            this.bytecode = bytecode;
            this.cdcl = cdcl;

            this.cls = initializeClass(cdcl, className, bytecode);
        }

        private static Class<? extends FieldContainer> initializeClass(ClassDefiningClassLoader cdcl, String name, byte[] bytecode) {
            Class<?> cls = cdcl.defineAndResolveClass(name.replaceAll("/", "."), bytecode, 0, bytecode.length);

            return (Class<FieldContainer>) cls;
        }

        @Override
        public byte[] getBytecode() {
            return bytecode;
        }

        @Override
        public String getClassName() {
            return className;
        }

        @Override
        public Class<? extends FieldContainer> getPrototypeClass() {
            return cls;
        }

        @Override
        public FieldContainer construct() {
            try {
                return cls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("BUG: could not instantiate", e);
            }
        }
    }

    private static Class javaTypeFromFieldType(Type type) {
        Class fieldType;

        if (type instanceof ClassType) {
            // reference type
            fieldType = Object.class;
        } else if (type instanceof WordType) {
            // primitive type
            fieldType = wordTypeToClass((WordType) type);
        } else {
            throw new IllegalStateException("unhandled type: " + type);
        }

        return fieldType;
    }

    private static Class wordTypeToClass(WordType wordType) {
        if (wordType instanceof BooleanType) {
            return boolean.class;
        } else if (wordType instanceof IntegerType) {
            switch (wordType.getSize()) {
                case 1:
                    return byte.class;
                case 2:
                    return short.class;
                case 4:
                    return int.class;
                case 8:
                    return long.class;
            }
        } else if (wordType instanceof FloatType) {
            switch (wordType.getSize()) {
                case 4:
                    return float.class;
                case 8:
                    return double.class;
            }
        }
        throw new IllegalArgumentException("unknown word type: " + wordType);
    }
}
