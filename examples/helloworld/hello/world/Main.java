// Basic helloworld example with the commands to execute it. 
//
// Compile the example with jbang (0.65.1+):
// $ jbang build --java=11 examples/helloworld/hello/world/Main.java
//
// Build the native executable in /tmp/output with:
// $ jbang org.qbicc:qbicc-main:0.1.0-SNAPSHOT --boot-module-path $(jbang info classpath --deps org.qbicc.rt:qbicc-rt-java.base:11.0.1-SNAPSHOT --deps org.qbicc:qbicc-runtime-main:0.1.0-SNAPSHOT --deps org.qbicc:qbicc-runtime-unwind:0.1.0-SNAPSHOT --deps org.qbicc:qbicc-runtime-gc-nogc:0.1.0-SNAPSHOT examples/helloworld/hello/world/Main.java) --output-path /tmp/output hello.world.Main
//
// Run the executable
// $ /tmp/output/a.out
//
//DEPS org.qbicc:qbicc-runtime-api:0.1.0-SNAPSHOT
package hello.world;

import static org.qbicc.runtime.CNative.*;
import static org.qbicc.runtime.stdc.Stdint.*;
import static org.qbicc.runtime.stdc.Stddef.*;
import static org.qbicc.runtime.stdc.Stdlib.*;

/**
 *
 */
public class Main {
    @extern
    public static native int putchar(int arg);

    @internal
    public static final class test_struct extends object {
        public int test;
        public int test2;
    }

    public static void main(String[] args) {
        uint32_t pointerToIntDerefTestResult = pointerToIntDerefTest();
        int pointerToStructMemberTestResult = pointerToStructMemberTest();
        //int pointerToStructMemberTest2Result = pointerToStructMemberTest2();
        //test_struct pointerToStructDerefTestResult = pointerToStructDerefTest();
        //uint32_t pointerAsArrayAccessResult = pointerAsArrayAccess();

       // void_ptr pointerToIntAddrOfTestResult = pointerToIntAddrOfTest();
    }

    static void_ptr pointerToIntAddrOfTest() {
        ptr<?> ptrVoid = malloc(sizeof(c_void.class));
        ptr<c_void> ptr = (ptr<c_void>) castPtr(ptrVoid, c_void.class);
        c_void value = ptr.deref(ptr);
        return addr_of(value);
    }

    static uint32_t pointerToIntDerefTest() {
        ptr<?> intPtrVoid = malloc(sizeof(uint32_t.class));
        ptr<uint32_t> intPtr = (ptr<uint32_t>) castPtr(intPtrVoid, uint32_t.class);
        uint32_t intField = ptr.deref(intPtr);
        return intField;
    }

    static uint32_t pointerAsArrayAccess() {
        ptr<?> intPtrVoid = malloc(sizeof(uint32_t.class));
        ptr<uint32_t> intPtr = (ptr<uint32_t>) castPtr(intPtrVoid, uint32_t.class);
        uint32_t intField = intPtr.asArray()[0];
        return intField;
    }

    static int pointerToStructMemberTest() {
        ptr<?> structPtrVoid = malloc(sizeof(test_struct.class));
        ptr<test_struct> structPtr = (ptr<test_struct>)castPtr(structPtrVoid, test_struct.class);
        return ptr.deref(structPtr).test;
    }

    static int pointerToStructMemberTest2() {
        ptr<?> structPtrVoid = malloc(sizeof(test_struct.class));
        ptr<test_struct> structPtr = (ptr<test_struct>)castPtr(structPtrVoid, test_struct.class);
        test_struct myStruct = ptr.deref(structPtr);
        return myStruct.test;
    }

    static test_struct pointerToStructDerefTest() {
        ptr<?> structPtrVoid = malloc(sizeof(test_struct.class));
        ptr<test_struct> structPtr = (ptr<test_struct>)castPtr(structPtrVoid, test_struct.class);
        return ptr.deref(structPtr);
    }
}

