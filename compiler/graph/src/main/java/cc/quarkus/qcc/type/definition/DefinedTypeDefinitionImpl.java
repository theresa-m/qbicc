package cc.quarkus.qcc.type.definition;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 */
final class DefinedTypeDefinitionImpl implements DefinedTypeDefinition {
    private final Dictionary definingLoader;
    private final String name;
    private final int access;
    private final ByteBuffer classBytes;
    private final String superName;
    private final String[] interfaceNames;
    private final DefinedFieldDefinition[] fields;
    private final DefinedMethodDefinition[] methods;

    private volatile DefinedTypeDefinition verified;

    DefinedTypeDefinitionImpl(final Dictionary definingLoader, final String name, final ByteBuffer orig) {
        ByteBuffer buffer = validateClassfileFormat(orig);
        int[] cpOffsets = processConstantPool(buffer);
        StringBuilder scratch = new StringBuilder(64);

        this.definingLoader = definingLoader;
        this.classBytes = orig;
        this.name = processClassName(buffer, cpOffsets, name);
        this.access = buffer.getShort() & 0xffff;
        this.superName = processSuperClassName(buffer, cpOffsets, scratch);
        this.interfaceNames = processInterfaces(buffer, cpOffsets, scratch);
        this.fields = processFields(buffer, cpOffsets, scratch);
        this.methods = processMethods(buffer, cpOffsets, scratch);

        // Just to make sure something else is not broken in the definition.
        validateRestOfEntry(buffer);
    }

    private void validateRestOfEntry(ByteBuffer buffer) {
        // now just check that the rest makes sense
        int attrCnt = buffer.getShort() & 0xffff;
        for (int i = 0; i < attrCnt; i ++) {
            buffer.getShort(); // name index
            int size = buffer.getInt();
            buffer.position(buffer.position() + size);
        }
        if (buffer.hasRemaining()) {
            throw new DefineFailedException("Extra data at end of class file");
        }
    }

    private DefinedMethodDefinition[] processMethods(ByteBuffer buffer, int[] cpOffsets, StringBuilder scratch) {
        int methodsCnt = buffer.getShort() & 0xffff;
        int[] methodOffsets = new int[methodsCnt];

        DefinedMethodDefinition[] methods = new DefinedMethodDefinition[methodsCnt];
        for (int i = 0; i < methodsCnt; i ++) {
            methodOffsets[i] = buffer.position();
            int methodAccess = buffer.getShort() & 0xffff;
            String methodName = ClassFile.getUtf8Entry(buffer, cpOffsets[buffer.getShort() & 0xffff], scratch);
            String methodDescriptor = ClassFile.getUtf8Entry(buffer, cpOffsets[buffer.getShort() & 0xffff], scratch);
            // skip attributes - except for code (for now)
            int attrCnt = buffer.getShort() & 0xffff;
            boolean hasCode = false;
            String[] parameterDescriptors = getParameterDescriptors(methodDescriptor, 1, 0);
            String returnTypeDescriptor = methodDescriptor.substring(methodDescriptor.lastIndexOf(')') + 1);
            String[] parameterNames = new String[parameterDescriptors.length];
            for (int j = 0; j < attrCnt; j ++) {
                if (ClassFile.utf8EntryEquals(buffer, cpOffsets[buffer.getShort() & 0xffff], "Code")) {
                    hasCode = true;
                }
                int size = buffer.getInt();
                buffer.position(buffer.position() + size);
            }
            methods[i] = new DefinedMethodDefinitionImpl(this, i, hasCode, methodAccess, methodName, parameterDescriptors, returnTypeDescriptor, parameterNames);
        }
        return methods;
    }

    private String processClassName(ByteBuffer buffer, int[] cpOffsets, String name) {
        int thisClassIdx = buffer.getShort() & 0xffff;

        if (name != null && ! ClassFile.classNameEquals(buffer, thisClassIdx, cpOffsets, name)) {
            throw new DefineFailedException("Class name mismatch");
        }

        return name;
    }

    private int[] processConstantPool(ByteBuffer buffer) {
        int cpCount = (buffer.getShort() & 0xffff) - 1;
        // one extra slot because the constant pool is one-based, so just leave a hole at the beginning
        int[] cpOffsets = new int[cpCount + 1];
        for (int i = 1; i < cpCount + 1; i ++) {
            cpOffsets[i] = buffer.position();
            int tag = buffer.get() & 0xff;
            switch (tag) {
                case ClassFile.CONSTANT_Utf8: {
                    int size = buffer.getShort() & 0xffff;
                    buffer.position(buffer.position() + size);
                    break;
                }
                case ClassFile.CONSTANT_Integer:
                case ClassFile.CONSTANT_Float:
                case ClassFile.CONSTANT_Fieldref:
                case ClassFile.CONSTANT_Methodref:
                case ClassFile.CONSTANT_InterfaceMethodref:
                case ClassFile.CONSTANT_NameAndType:
                case ClassFile.CONSTANT_Dynamic:
                case ClassFile.CONSTANT_InvokeDynamic: {
                    buffer.position(buffer.position() + 4);
                    break;
                }
                case ClassFile.CONSTANT_Class:
                case ClassFile.CONSTANT_String:
                case ClassFile.CONSTANT_MethodType:
                case ClassFile.CONSTANT_Module:
                case ClassFile.CONSTANT_Package: {
                    buffer.position(buffer.position() + 2);
                    break;
                }
                case ClassFile.CONSTANT_Long:
                case ClassFile.CONSTANT_Double: {
                    buffer.position(buffer.position() + 8);
                    i++; // two slots
                    break;
                }
                case ClassFile.CONSTANT_MethodHandle: {
                    buffer.position(buffer.position() + 3);
                    break;
                }
                default: {
                    throw new DefineFailedException("Unknown constant pool tag " + Integer.toHexString(tag) + " at index " + i);
                }
            }
        }
        return cpOffsets;
    }

    private String[] processInterfaces(ByteBuffer buffer, int[] cpOffsets, StringBuilder scratch) {
        int interfacesCount = buffer.getShort() & 0xffff;
        String[] interfaceNames = new String[interfacesCount];
        for (int i = 0; i < interfacesCount; i ++) {
            interfaceNames[i] = ClassFile.getClassName(buffer, buffer.getShort() & 0xffff, cpOffsets, scratch);
        }
        return interfaceNames;
    }

    private DefinedFieldDefinition[] processFields(ByteBuffer buffer, int[] cpOffsets, StringBuilder scratch) {
        int fieldsCnt = buffer.getShort() & 0xffff;
        int[] fieldOffsets = new int[fieldsCnt];
        DefinedFieldDefinition[] fields = new DefinedFieldDefinition[fieldsCnt];
        for (int i = 0; i < fieldsCnt; i ++) {
            fieldOffsets[i] = buffer.position();
            int fieldAccess = buffer.getShort() & 0xffff;
            String fieldName =  ClassFile.getUtf8Entry(buffer, cpOffsets[buffer.getShort() & 0xffff], scratch);
            String fieldDescriptor =  ClassFile.getUtf8Entry(buffer, cpOffsets[buffer.getShort() & 0xffff], scratch);
            // skip attributes
            int attrCnt = buffer.getShort() & 0xffff;
            for (int j = 0; j < attrCnt; j ++) {
                // todo: get annotations, signature, and flags
                buffer.getShort(); // name index
                int size = buffer.getInt();
                buffer.position(buffer.position() + size);
            }
            fields[i] = new DefinedFieldDefinitionImpl(this, fieldAccess, fieldName, fieldDescriptor);
        }
        return fields;
    }

    private String processSuperClassName(ByteBuffer buffer, int[] cpOffsets, StringBuilder scratch) {
        int superClassIdx = buffer.getShort() & 0xffff;

        return ClassFile.getClassName(buffer, superClassIdx, cpOffsets, scratch);
    }

    private ByteBuffer validateClassfileFormat(ByteBuffer orig) {
        // do some basic pieces of verification
        if (orig.order() != ByteOrder.BIG_ENDIAN) {
            throw new DefineFailedException("Wrong byte buffer order");
        }
        ByteBuffer buffer = orig.duplicate();
        int magic = buffer.getInt();
        if (magic != 0xcafebabe) {
            throw new DefineFailedException("Bad magic number");
        }

        validateValidClassFileVersion(buffer);

        return buffer;
    }

    private void validateValidClassFileVersion(ByteBuffer buffer) {
        int minor = buffer.getShort() & 0xffff;
        int major = buffer.getShort() & 0xffff;
        // todo fix up
        if (major < 45 || major == 45 && minor < 3 || major > 55 || major == 55 && minor > 0) {
            throw new DefineFailedException("Unsupported class version " + major + "." + minor);
        }
    }

    private static String getReturnTypeDescriptor(final String descriptor) {
        int idx = descriptor.lastIndexOf(')');
        assert idx != -1; // otherwise getParameterDescriptors would have failed
        int start = idx + 1;
        idx = start;
        char ch = descriptor.charAt(idx);
        int arrayLevel = 0;
        while (ch == '[') {
            arrayLevel ++;
            idx ++;
            ch = descriptor.charAt(idx);
        }
        if (ch == 'L') {
            int term = descriptor.indexOf(';', idx + 1);
            if (term == -1) {
                throw new DefineFailedException("Unterminated type descriptor");
            }
            return descriptor.substring(start);
        }
        if (arrayLevel == 0) {
            switch (ch) {
                case 'Z': return "Z";
                case 'B': return "B";
                case 'C': return "C";
                case 'S': return "S";
                case 'I': return "I";
                case 'J': return "J";
                case 'F': return "F";
                case 'D': return "D";
                case 'V': return "V";
                default: throw new DefineFailedException("Invalid type signature character " + ch);
            }
        } else {
            // todo: cache these strings
            switch (ch) {
                case 'Z':
                case 'B':
                case 'C':
                case 'S':
                case 'I':
                case 'J':
                case 'F':
                case 'D':
                case 'V': {
                    return descriptor.substring(start);
                }
                default: throw new DefineFailedException("Invalid type signature character " + ch);
            }
        }
    }

    private static final String[] NO_STRINGS = new String[0];

    private static String[] getParameterDescriptors(final String descriptor, int strIdx, final int arrayIdx) {
        int start = strIdx;
        char ch = descriptor.charAt(strIdx);
        if (ch == ')') {
            return arrayIdx == 0 ? NO_STRINGS : new String[arrayIdx];
        }
        int arrayLevel = 0;
        while (ch == '[') {
            arrayLevel ++;
            strIdx ++;
            ch = descriptor.charAt(strIdx);
        }
        String[] array;
        if (ch == 'L') {
            int term = descriptor.indexOf(';', strIdx + 1);
            if (term == -1) {
                throw new DefineFailedException("Unterminated type descriptor");
            }
            array = getParameterDescriptors(descriptor, term + 1, arrayIdx + 1);
            array[arrayIdx] = descriptor.substring(start, term + 1);
            return array;
        }
        array = getParameterDescriptors(descriptor, strIdx + 1, arrayIdx + 1);
        if (arrayLevel == 0) {
            switch (ch) {
                case 'Z': array[arrayIdx] = "Z"; break;
                case 'B': array[arrayIdx] = "B"; break;
                case 'C': array[arrayIdx] = "C"; break;
                case 'S': array[arrayIdx] = "S"; break;
                case 'I': array[arrayIdx] = "I"; break;
                case 'J': array[arrayIdx] = "J"; break;
                case 'F': array[arrayIdx] = "F"; break;
                case 'D': array[arrayIdx] = "D"; break;
                case 'V': array[arrayIdx] = "V"; break;
                default: throw new DefineFailedException("Invalid type signature character " + ch);
            }
        } else {
            // todo: cache these strings
            switch (ch) {
                case 'Z':
                case 'B':
                case 'C':
                case 'S':
                case 'I':
                case 'J':
                case 'F':
                case 'D':
                case 'V': {
                    array[arrayIdx] = descriptor.substring(start, strIdx + 1);
                    break;
                }
                default: throw new DefineFailedException("Invalid type signature character " + ch);
            }
        }
        return array;
    }



    public Dictionary getDefiningClassLoader() {
        return definingLoader;
    }

    public String getName() {
        return name;
    }

    public int getModifiers() {
        return access;
    }

    public String getSuperClassName() {
        return superName;
    }

    public int getInterfaceCount() {
        return interfaceNames.length;
    }

    public String getInterfaceName(final int index) throws IndexOutOfBoundsException {
        return interfaceNames[index];
    }

    public VerifiedTypeDefinition verify() throws VerifyFailedException {
        DefinedTypeDefinition verified = this.verified;
        if (verified != null) {
            return verified.verify();
        }
        VerifiedTypeDefinition superType;
        if (superName != null) {
            superType = definingLoader.findClass(superName).verify();
        } else {
            superType = null;
        }
        int cnt = getInterfaceCount();
        VerifiedTypeDefinition[] interfaces = new VerifiedTypeDefinition[cnt];
        for (int i = 0; i < cnt; i ++) {
            interfaces[i] = definingLoader.findClass(getInterfaceName(i)).verify();
        }
        synchronized (this) {
            verified = this.verified;
            if (verified != null) {
                return verified.verify();
            }
            try {
                verified = new VerifiedTypeDefinitionImpl(this, superType, interfaces);
            } catch (VerifyFailedException e) {
                DefinedTypeDefinition failed = new VerificationFailedDefinitionImpl(this, e.getMessage(), e.getCause());
                definingLoader.replaceTypeDefinition(name, this, failed);
                this.verified = failed;
                throw e;
            }
            // replace in the map *first*, *then* replace our local ref
            definingLoader.replaceTypeDefinition(name, this, verified);
            this.verified = verified;
            return verified.verify();
        }
    }

    public int getFieldCount() {
        return fields.length;
    }

    public int getMethodCount() {
        return methods.length;
    }

    public DefinedMethodDefinition getMethodDefinition(final int index) throws IndexOutOfBoundsException {
        return methods[index];
    }

    public DefinedFieldDefinition getFieldDefinition(final int index) throws IndexOutOfBoundsException {
        return fields[index];
    }

    // internal

    ByteBuffer getClassBytes() {
        return classBytes.duplicate();
    }
}
