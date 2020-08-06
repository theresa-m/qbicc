package cc.quarkus.qcc.interpreter;

public interface FieldContainer {
    FieldSet getFieldSet();

    int getFieldIndex(String name);

    Object getObjectVolatile(int index);

    long getLongVolatile(int index);

    int getIntVolatile(int index);

    void setObjectVolatile(int index, Object value);

    void setLongVolatile(int index, long value);

    void setIntVolatile(int index, int value);

    default Object getObjectPlain(int index) {
        return getObjectVolatile(index);
    }

    default Object getObjectAcquire(int index) {
        return getObjectVolatile(index);
    }

    default long getLongPlain(int index) {
        return getLongVolatile(index);
    }

    default long getLongAcquire(int index) {
        return getLongVolatile(index);
    }

    default int getIntPlain(int index) {
        return getIntVolatile(index);
    }

    default int getIntAcquire(int index) {
        return getIntVolatile(index);
    }

    default void setObjectPlain(int index, Object value) {
        setObjectVolatile(index, value);
    }

    default void setObjectRelease(int index, Object value) {
        setObjectVolatile(index, value);
    }

    default void setLongPlain(int index, long value) {
        setLongVolatile(index, value);
    }

    default void setLongRelease(int index, long value) {
        setLongVolatile(index, value);
    }

    default void setIntPlain(int index, int value) {
        setIntVolatile(index, value);
    }

    default void setIntRelease(int index, int value) {
        setIntVolatile(index, value);
    }

    default JavaObject getObjectFieldPlain(String name) {
        int index = getFieldIndex(name);
        return (JavaObject) getObjectPlain(index);
    }

    default JavaObject getObjectFieldVolatile(String name) {
        int index = getFieldIndex(name);
        return (JavaObject) getObjectVolatile(index);
    }

    default JavaObject getObjectFieldAcquire(String name) {
        int index = getFieldIndex(name);
        return (JavaObject) getObjectAcquire(index);
    }

    default long getLongFieldPlain(String name) {
        int index = getFieldIndex(name);
        return getLongPlain(index);
    }

    default long getLongFieldVolatile(String name) {
        int index = getFieldIndex(name);
        return getLongVolatile(index);
    }

    default long getLongFieldAcquire(String name) {
        int index = getFieldIndex(name);
        return getLongAcquire(index);
    }

    default int getIntFieldPlain(String name) {
        int index = getFieldIndex(name);
        return getIntPlain(index);
    }

    default int getIntFieldVolatile(String name) {
        int index = getFieldIndex(name);
        return getIntVolatile(index);
    }

    default int getIntFieldAcquire(String name) {
        int index = getFieldIndex(name);
        return getIntAcquire(index);
    }

    default void setFieldPlain(String name, JavaObject value) {
        int index = getFieldIndex(name);
        setObjectPlain(index, value);
    }

    default void setFieldVolatile(String name, JavaObject value) {
        int index = getFieldIndex(name);
        setObjectVolatile(index, value);
    }

    default void setFieldRelease(String name, JavaObject value) {
        int index = getFieldIndex(name);
        setObjectRelease(index, value);
    }

    default void setFieldPlain(String name, long value) {
        int index = getFieldIndex(name);
        setLongPlain(index, value);
    }

    default void setFieldVolatile(String name, long value) {
        int index = getFieldIndex(name);
        setLongVolatile(index, value);
    }

    default void setFieldRelease(String name, long value) {
        int index = getFieldIndex(name);
        setLongRelease(index, value);
    }

    default void setFieldPlain(String name, int value) {
        int index = getFieldIndex(name);
        setIntPlain(index, value);
    }

    default void setFieldVolatile(String name, int value) {
        int index = getFieldIndex(name);
        setIntVolatile(index, value);
    }

    default void setFieldRelease(String name, int value) {
        int index = getFieldIndex(name);
        setIntRelease(index, value);
    }
}
