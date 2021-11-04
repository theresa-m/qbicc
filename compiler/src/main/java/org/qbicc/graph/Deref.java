package org.qbicc.graph;

import org.qbicc.type.PointerType;
import org.qbicc.type.ValueType;
import org.qbicc.type.definition.element.ExecutableElement;

/**
 * Wraps a dereferenced pointer. If this node is not removed by block in the program it will be replaced by a load.
 */

// TODO still unary value?
public final class Deref extends AbstractValue implements UnaryValue {
    private final Load load;
    private final PointerHandle handle;
    private final Value value;
    private final ValueType toType;
    private boolean isMemberAccess;


    Deref(Node callSite, ExecutableElement element, int line, int bci, Value load) {
        super(callSite, element, line, bci);
        this.load = (Load)load;
        this.handle = (PointerHandle)load.getValueHandle();
        this.value = handle.getPointerValue();

        PointerType pt = (PointerType)value.getType();
        this.toType = pt.getPointeeType();
        this.isMemberAccess = false;
    }

    public void setIsMemberAccess() {
        isMemberAccess = true;
    }

    public Value getInput() {
        /* load is not required if this is just a member access */
        if (isMemberAccess) {
            return value;
        } else {
            return load;
        }
    }

    public ValueHandle getValueHandle() {
        return handle;
    }

    int calcHashCode() {
        return value.hashCode() * 19;
    }

    @Override
    String getNodeName() {
        return "Deref";
    }

    public boolean equals(final Object other) {
        return other instanceof Deref && equals((Deref) other);
    }

    public boolean equals(final Deref other) {
        return this == other || other != null && load.equals(other.load);
    }

    public ValueType getType() {
        return toType;
    }

    public <T, R> R accept(final ValueVisitor<T, R> visitor, final T param) {
        return visitor.visit(param, this);
    }
}