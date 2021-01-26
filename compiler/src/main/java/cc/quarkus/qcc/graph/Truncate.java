package cc.quarkus.qcc.graph;

import cc.quarkus.qcc.type.WordType;
import cc.quarkus.qcc.type.definition.element.ExecutableElement;

/**
 *
 */
public final class Truncate extends AbstractWordCastValue {
    Truncate(final Node callSite, final ExecutableElement element, final int line, final int bci, final Value value, final WordType toType) {
        super(callSite, element, line, bci, value, toType);
    }

    public <T, R> R accept(final ValueVisitor<T, R> visitor, final T param) {
        return visitor.visit(param, this);
    }
}
