// Basic helloworld example with the commands to execute it.
//
// Compile the example with jbang (0.65.1+):
// $ jbang build examples/helloworld/hello/world/Main.java
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
import static org.qbicc.runtime.stdc.Stdlib.*;
import static org.qbicc.runtime.stdc.Stdint.*;

/**
 *
 */
public class Main {
    @extern
    public static native int putchar(int arg);

    @internal
    public static final class test_struct extends object {
        int testInt;
        boolean testBoolean;
        object testObject;
    }
    public static final class test_struct_ptr extends ptr<test_struct> {}

    public static void main(String[] args) {
//        Thread t = new Thread();
//        t.start();
        /* integer */
//        ptr<?> myIntPtrVoid = malloc(sizeof(int32_t_ptr.class));
//        ptr<int32_t> myIntPtr = (ptr<int32_t>)castPtr(myIntPtrVoid, int32_t.class);
//        int32_t value = wordExact(2);
//        myIntPtr.derefAssign(value);

        //myIntDeref = word(2);
//        int myIntDeref2 = myIntPtr.deref().intValue();
//        if (myIntDeref2 == 2) {
//            putchar('T');
//        } else {
//            putchar('F');
//        }

        /* object */


        /* compound type */
        ptr<?> myStructPtrVoid = malloc(sizeof(test_struct_ptr.class));
        ptr<test_struct> myStructPtr = (ptr<test_struct>)castPtr(myStructPtrVoid, test_struct.class);
//        test_struct myStructDeref = myStructPtr.deref();
//        myStructDeref.testInt = 3;
//        myStructDeref.testBoolean = true;

        /* addr_of should return the same result */
       // ptr<test_struct> newPtr = addr_of(myStructDeref);
//        if (newPtr.equals(myStructPtr)) {
//            putchar('T');
//        }
//        if (newPtr.equals(myStructPtrVoid)) {
//            putchar('R');
//        }

        /* try accessing later and get the same results */
//        test_struct myStructDeref2 = myStructPtr.deref();
//        if (myStructDeref.equals(myStructDeref2)) {
//            putchar('T');
//        }


        // TODO turn this into a real test
//        ptr<?> merVoid = malloc(sizeof(pthread_t_ptr.class));
//        ptr<pthread_t> mer = (ptr<pthread_t>)castPtr(merVoid, pthread_t.class);
//        pthread_t mer2 = mer.deref();

//        putchar('h');
//        putchar('e');
//        putchar('l');
//        putchar('l');
//        putchar('o');
//        putchar(' ');
//        putchar('w');
//        putchar('o');
//        putchar('r');
//        putchar('l');
//        putchar('d');
//        putchar('\n');
    }
}

