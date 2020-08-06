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
    public Object getObjectPlain(int index) {
        return objects.getPlain(index);
    }

    @Override
    public Object getObjectVolatile(int index) {
        return objects.get(index);
    }

    @Override
    public Object getObjectAcquire(int index) {
        return objects.getAcquire(index);
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
    public void setObjectPlain(int index, Object value) {
        objects.setPlain(index, value);
    }

    @Override
    public void setObjectVolatile(int index, Object value) {
        objects.set(index, value);
    }

    @Override
    public void setObjectRelease(int index, Object value) {
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
    public void setLongRelease(int index, long value) {
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
    public void setIntRelease(int index, int value) {
        objects.setRelease(index, Integer.valueOf(value));
    }
}
