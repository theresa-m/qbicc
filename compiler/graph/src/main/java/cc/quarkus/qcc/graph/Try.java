package cc.quarkus.qcc.graph;

/**
 * An operation which may throw an exception.
 */
public final class Try extends AbstractNode implements Resume {
    private final Node dependency;
    private final Triable delegateOperation;
    private final ClassType[] catchTypes;
    private final BlockLabel[] catchTargetLabels;
    private final BlockLabel resumeTargetLabel;

    Try(final Node dependency, final Triable delegateOperation, final ClassType[] catchTypes, final BlockLabel[] catchTargetLabels, final BlockLabel resumeTargetLabel) {
        this.dependency = dependency;
        this.delegateOperation = delegateOperation;
        this.catchTypes = catchTypes;
        this.catchTargetLabels = catchTargetLabels;
        this.resumeTargetLabel = resumeTargetLabel;
    }

    public Triable getDelegateOperation() {
        return delegateOperation;
    }

    public int getCatchTypeCount() {
        return catchTypes.length;
    }

    public ClassType getCatchType(int index) {
        return catchTypes[index];
    }

    public BasicBlock getCatchTarget(int index) {
        return BlockLabel.getTargetOf(catchTargetLabels[index]);
    }

    public BlockLabel getCatchTargetLabel(int index) {
        return catchTargetLabels[index];
    }

    public BlockLabel getResumeTargetLabel() {
        return resumeTargetLabel;
    }

    public int getBasicDependencyCount() {
        return 2;
    }

    public Node getBasicDependency(final int index) throws IndexOutOfBoundsException {
        return index == 0 ? dependency : index == 1 ? delegateOperation : Util.throwIndexOutOfBounds(index);
    }

    public int getSuccessorCount() {
        return 1 + catchTargetLabels.length;
    }

    public BasicBlock getSuccessor(final int index) {
        int length = catchTargetLabels.length;
        return index < length ? getCatchTarget(index) : index == length ? getResumeTarget() : Util.throwIndexOutOfBounds(index);
    }

    public <T, R> R accept(final TerminatorVisitor<T, R> visitor, final T param) {
        return visitor.visit(param, this);
    }
}
