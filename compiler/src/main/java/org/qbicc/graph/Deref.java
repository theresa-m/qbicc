package org.qbicc.graph;

import org.qbicc.type.PointerType;
import org.qbicc.type.ValueType;
import org.qbicc.type.definition.element.ExecutableElement;

/**
 * Wraps a dereferenced pointer. If this node is not removed by block in the program it will be replaced by a load.
 */

// TODO still unary value?
public final class Deref extends AbstractValue implements UnaryValue {
    private final Value value;
    private final PointerHandle handle;
    private final ValueType toType;

    Deref(Node callSite, ExecutableElement element, int line, int bci, ValueHandle handle) {
        super(callSite, element, line, bci);
        this.handle = (PointerHandle)handle;
        this.value = ((PointerHandle) handle).getPointerValue();

        PointerType pt = (PointerType)value.getType();
        this.toType = pt.getPointeeType();
    }

    public Value getInput() {
        return value;
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
        return this == other || other != null && value.equals(other.value);
    }

    public ValueType getType() {
        return toType;
    }

//    public Load getLoad() {
//        return load;
//    }

    public <T, R> R accept(final ValueVisitor<T, R> visitor, final T param) {
        return visitor.visit(param, this);
    }
}