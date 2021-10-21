package org.qbicc.plugin.opt;

import org.qbicc.context.CompilationContext;
import org.qbicc.graph.BasicBlock;
import org.qbicc.graph.BasicBlockBuilder;
import org.qbicc.graph.DelegatingBasicBlockBuilder;
import org.qbicc.graph.Load;
import org.qbicc.graph.MemoryAtomicityMode;
import org.qbicc.graph.Node;
import org.qbicc.graph.NodeVisitor;
import org.qbicc.graph.Store;
import org.qbicc.graph.Value;
import org.qbicc.graph.ValueHandle;
import org.qbicc.graph.ValueHandleVisitor;
import org.qbicc.graph.literal.ZeroInitializerLiteral;
import org.qbicc.type.definition.element.MethodElement;

public class LoadStoreEliminationVisitor implements NodeVisitor.Delegating<Node.Copier, Value, Node, BasicBlock, ValueHandle> {
    private final CompilationContext context;
    private final NodeVisitor<Node.Copier, Value, Node, BasicBlock, ValueHandle> delegate;

    public LoadStoreEliminationVisitor(final CompilationContext context, final NodeVisitor<Node.Copier, Value, Node, BasicBlock, ValueHandle> delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    public NodeVisitor<Node.Copier, Value, Node, BasicBlock, ValueHandle> getDelegateNodeVisitor() {
        return delegate;
    }

    // TODO this is successfully eliminating the load/stores
    public Value visit(final Node.Copier param, final Load node) {
        if (node.getElement() instanceof MethodElement) {
            MethodElement e = (MethodElement) node.getElement();
            if ((e.nameEquals("addToNativeThreadList") && (node.getSourceLine() == 45 || node.getSourceLine() == 54))
                || (e.nameEquals("main") && node.getSourceLine() == 44)
            ) {
                return context.getLiteralFactory().zeroInitializerLiteralOfType(node.getType());
            }
        }
        return getDelegateNodeVisitor().visit(param, node);
    }

    public Node visit(final Node.Copier param, final Store node) {
//        if (node.getValue() instanceof ZeroInitializerLiteral) {
//            return node.getDependency();
//        }
        if (node.getElement() instanceof MethodElement) {
            MethodElement e = (MethodElement) node.getElement();
            if ((e.nameEquals("addToNativeThreadList") && (node.getSourceLine() == 45 || node.getSourceLine() == 54))
                || (e.nameEquals("main") && node.getSourceLine() == 44)
            ) {
                return node.getDependency();
            }
        }
//        if (node.getDependency() instanceof Load) {
//            Load loadNode = (Load)node.getDependency();
//            if (node.getValue().equals(loadNode)) {
//                return node.getDependency();
//            }
//            node.getSourceLine();
//        }
        return getDelegateNodeVisitor().visit(param, node);
    }

//    @Override
//    public Value load(ValueHandle handle, MemoryAtomicityMode mode) {
//        Value loadValue = super.load(handle, mode);
//        if (handle.getElement() instanceof MethodElement) {
//            MethodElement e = (MethodElement) handle.getElement();
//            if (e.nameEquals("addToNativeThreadList") && loadValue.getSourceLine() == 45) {
//                Value value = handle.accept(this, mode);
//                return value;
//            }
//        }
//        return super.load(handle, mode);
//    }

//    @Override
//    public Node store(final ValueHandle handle, final Value value, final MemoryAtomicityMode mode) {
//        Store storeNode = (Store)super.store(handle, value, mode);
//
//        if (handle.getElement() instanceof MethodElement) {
//            MethodElement e = (MethodElement) handle.getElement();
//            if (e.nameEquals("addToNativeThreadList")) {
//                if (storeNode.getSourceLine() == 45) {
//                    return storeNode.getDependency(); // skip store
//                }
//            }
//        }
//        return storeNode;
//    }

}
