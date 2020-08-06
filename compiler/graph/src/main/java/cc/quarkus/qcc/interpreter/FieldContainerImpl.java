package cc.quarkus.qcc.interpreter;

import cc.quarkus.qcc.type.definition.VerifiedTypeDefinition;

import java.util.concurrent.atomic.AtomicReferenceArray;

final class FieldContainerImpl implements FieldContainer {
    private final VerifiedTypeDefinition type;
    final FieldSet fieldSet;
    // todo: autoboxing is really a terrible idea
    final AtomicReferenceArray<Object> objects;

    FieldContainerImpl(VerifiedTypeDefinition type, FieldSet fieldSet) {
        this.type = type;
        this.fieldSet = fieldSet;
        objects = new AtomicReferenceArray<>(fieldSet.getSize());
    }

    @Override
    public FieldSet getFieldSet() {
        return fieldSet;
    }

    @Override
    public int getFieldIndex(String name) {
        return fieldSet.getIndex(name);
    }

    @Override
    public JavaObject getObjectPlain(int index) {
        return (JavaObject) objects.getPlain(index);
    }

    @Override
    public JavaObject getObjectVolatile(int index) {
        return (JavaObject) objects.get(index);
    }

    @Override
    public JavaObject getObjectAcquire(int index) {
        return (JavaObject) objects.getAcquire(index);
    }

    @Override
    public long getLongPlain(int index) {
        return ((Number) objects.getPlain(index)).longValue();
    }

    @Override
    public long getLongVolatile(int index) {
        return ((Number) objects.get(index)).longValue();
    }

    @Override
    public long getLongAcquire(int index) {
        return ((Number) objects.getAcquire(index)).longValue();
    }

    @Override
    public int getIntPlain(int index) {
        return ((Number) objects.getPlain(index)).intValue();
    }

    @Override
    public int getIntVolatile(int index) {
        return ((Number) objects.get(index)).intValue();
    }

    @Override
    public int getIntAcquire(int index) {
        return ((Number) objects.getAcquire(index)).intValue();
    }

    @Override
    public void setObjectPlain(int index, JavaObject value) {
        objects.setPlain(index, value);
    }

    @Override
    public void setObjectVolatile(int index, JavaObject value) {
        objects.set(index, value);
    }

    @Override
    public void setObjectRelease(int index, JavaObject value) {
        objects.setRelease(index, value);
    }

    @Override
    public void setLongPlain(int index, long value) {
        objects.setPlain(index, Long.valueOf(value));
    }

    @Override
    public void setLongVolatile(int index, long value) {
        objects.set(index, Long.valueOf(value));
    }

    @Override
    public void setLongRelease(long value, int index) {
        objects.setRelease(index, Long.valueOf(value));
    }

    @Override
    public void setIntPlain(int index, int value) {
        objects.setPlain(index, Integer.valueOf(value));
    }

    @Override
    public void setIntVolatile(int index, int value) {
        objects.set(index, Integer.valueOf(value));
    }

    @Override
    public void setIntRelease(int value, int index) {
        objects.setRelease(index, Integer.valueOf(value));
    }
}
