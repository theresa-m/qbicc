package cc.quarkus.qcc.type.definition;

import java.util.List;

/**
 *
 */
final class VerificationFailedDefinitionImpl implements DefinedTypeDefinition {
    private final DefinedTypeDefinitionImpl delegate;
    private final String msg;
    private final Throwable cause;

    VerificationFailedDefinitionImpl(final DefinedTypeDefinitionImpl delegate, final String msg, final Throwable cause) {
        this.delegate = delegate;
        this.msg = msg;
        this.cause = cause;
    }

    public Dictionary getDefiningClassLoader() {
        return delegate.getDefiningClassLoader();
    }

    public String getName() {
        return delegate.getName();
    }

    public int getModifiers() {
        return delegate.getModifiers();
    }

    public String getSuperClassName() {
        return delegate.getSuperClassName();
    }

    public int getInterfaceCount() {
        return delegate.getInterfaceCount();
    }

    public String getInterfaceName(final int index) throws IndexOutOfBoundsException {
        return delegate.getInterfaceName(index);
    }

    public int getFieldCount() {
        return delegate.getFieldCount();
    }

    @Override
    public List<DefinedFieldDefinition> getFields() {
        return delegate.getFields();
    }

    @Override
    public int getStaticFieldCount() {
        return delegate.getStaticFieldCount();
    }

    @Override
    public DefinedFieldDefinition getStaticFieldDefinition(int index) throws IndexOutOfBoundsException {
        return delegate.getStaticFieldDefinition(index);
    }

    @Override
    public List<DefinedFieldDefinition> getStaticFields() {
        return delegate.getStaticFields();
    }

    public int getMethodCount() {
        return delegate.getMethodCount();
    }

    public DefinedFieldDefinition getFieldDefinition(final int index) throws IndexOutOfBoundsException {
        return delegate.getFieldDefinition(index);
    }

    public DefinedMethodDefinition getMethodDefinition(final int index) throws IndexOutOfBoundsException {
        return delegate.getMethodDefinition(index);
    }

    public VerifiedTypeDefinition verify() {
        throw new VerifyFailedException(msg, cause);
    }
}
