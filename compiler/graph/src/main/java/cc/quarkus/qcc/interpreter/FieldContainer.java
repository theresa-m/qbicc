package cc.quarkus.qcc.interpreter;

interface FieldContainer {
    FieldSet getFieldSet();

    int getFieldIndex(String name);

    JavaObject getObjectPlain(int index);

    JavaObject getObjectVolatile(int index);

    JavaObject getObjectAcquire(int index);

    long getLongPlain(int index);

    long getLongVolatile(int index);

    long getLongAcquire(int index);

    int getIntPlain(int index);

    int getIntVolatile(int index);

    int getIntAcquire(int index);

    void setObjectPlain(int index, JavaObject value);

    void setObjectVolatile(int index, JavaObject value);

    void setObjectRelease(int index, JavaObject value);

    void setLongPlain(int index, long value);

    void setLongVolatile(int index, long value);

    void setLongRelease(long value, int index);

    void setIntPlain(int index, int value);

    void setIntVolatile(int index, int value);

    void setIntRelease(int value, int index);

    default JavaObject getObjectFieldPlain(String name) {
        int index = getFieldIndex(name);
        return getObjectPlain(index);
    }

    default JavaObject getObjectFieldVolatile(String name) {
        int index = getFieldIndex(name);
        return getObjectVolatile(index);
    }

    default JavaObject getObjectFieldAcquire(String name) {
        int index = getFieldIndex(name);
        return getObjectAcquire(index);
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
        setLongRelease(value, index);
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
        setIntRelease(value, index);
    }
}
