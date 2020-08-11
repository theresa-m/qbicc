package cc.quarkus.qcc.type.definition;

import cc.quarkus.qcc.graph.ClassType;
import cc.quarkus.qcc.graph.Type;
import cc.quarkus.qcc.type.descriptor.MethodIdentifier;

import java.util.List;

/**
 *
 */
final class PreparedTypeDefinitionImpl implements PreparedTypeDefinition {
    private final ResolvedTypeDefinitionImpl delegate;
    private volatile InitializedTypeDefinition initialized;
    private InitializedTypeDefinition initializing;

    PreparedTypeDefinitionImpl(final ResolvedTypeDefinitionImpl delegate) {
        this.delegate = delegate;
    }

    // delegations

    public ClassType getClassType() {
        return delegate.getClassType();
    }

    public PreparedTypeDefinition getSuperClass() {
        return delegate.getSuperClass().prepare();
    }

    public PreparedTypeDefinition getInterface(final int index) throws IndexOutOfBoundsException {
        return delegate.getInterface(index).prepare();
    }

    public boolean isArray() {
        return delegate.isArray();
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

    public int getMethodCount() {
        return delegate.getMethodCount();
    }

    public ResolvedFieldDefinition resolveField(final Type type, final String name) {
        return delegate.resolveField(type, name);
    }

    public ResolvedFieldDefinition findField(final String name) {
        return delegate.findField(name);
    }

    public ResolvedFieldDefinition getFieldDefinition(final int index) throws IndexOutOfBoundsException {
        return delegate.getFieldDefinition(index);
    }

    @Override
    public List<DefinedFieldDefinition> getFields() {
        return delegate.getFields();
    }

    @Override
    public int getStaticFieldCount() {
        return delegate.getStaticFieldCount();
    }

    public ResolvedFieldDefinition getStaticFieldDefinition(int index) throws IndexOutOfBoundsException {
        return delegate.getStaticFieldDefinition(index);
    }

    @Override
    public List<DefinedFieldDefinition> getStaticFields() {
        return delegate.getStaticFields();
    }

    public ResolvedMethodDefinition getMethodDefinition(final int index) throws IndexOutOfBoundsException {
        return delegate.getMethodDefinition(index);
    }

    public ResolvedMethodDefinition resolveMethod(final MethodIdentifier identifier) {
        return delegate.resolveMethod(identifier);
    }

    public ResolvedMethodDefinition resolveInterfaceMethod(final MethodIdentifier identifier) {
        return delegate.resolveInterfaceMethod(identifier);
    }

    public ResolvedMethodDefinition resolveInterfaceMethod(final MethodIdentifier identifier, final boolean searchingSuper) {
        return delegate.resolveInterfaceMethod(identifier, searchingSuper);
    }

    public InitializedTypeDefinition initialize() throws InitializationFailedException {
        InitializedTypeDefinition initialized = this.initialized;
        if (initialized != null) {
            return initialized;
        }
        PreparedTypeDefinition superClass = getSuperClass();
        if (superClass != null) {
            superClass.initialize();
        }
        int interfaceCount = getInterfaceCount();
        for (int i = 0; i < interfaceCount; i ++) {
            getInterface(i).initialize();
        }
        synchronized (this) {
            initialized = this.initialized;
            if (initialized == null) {
                if (initializing != null) {
                    // init in progress from this same thread
                    return initializing;
                }
                this.initializing = initialized = new InitializedTypeDefinitionImpl(this);
                // TODO: call into VM initializer
                // vm.initializeClass(xxx)
                // TODO: ^^^^
                getDefiningClassLoader().replaceTypeDefinition(getName(), this, initialized);
                this.initialized = initialized;
            }
        }
        return initialized;
    }
}
