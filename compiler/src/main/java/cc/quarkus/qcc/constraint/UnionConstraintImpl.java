package cc.quarkus.qcc.constraint;

import static cc.quarkus.qcc.constraint.Constraint.Satisfaction.*;

class UnionConstraintImpl extends AbstractConstraint {

    public UnionConstraintImpl(Constraint c1, Constraint c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    @Override
    public Satisfaction isSatisfiedBy(SatisfactionContext context) {
        Satisfaction s1 = this.c1.isSatisfiedBy(context);

        if ( s1 == YES ) {
            return YES;
        }

        Satisfaction s2 = this.c2.isSatisfiedBy(context);
        if ( s2 == YES ) {
            return YES;
        }
        return NO;
    }

    @Override
    public Satisfaction satisfies(SatisfactionContext context, RelationConstraint other) {
        Satisfaction s1 = this.c1.satisfies(context, other);
        if ( s1 == YES ) {
            return YES;
        }

        Satisfaction s2 = this.c2.satisfies(context, other);
        if ( s2 == YES) {
            return YES;
        }
        return NOT_APPLICABLE;
    }

    @Override
    public String toString() {
        return "UnionConstraintImpl{" +
                "c1=" + c1 +
                ", c2=" + c2 +
                '}';
    }

    private final Constraint c1;

    private final Constraint c2;
}
