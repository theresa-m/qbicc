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

/**
 *
 */
public class Main {
    @extern
    public static native int putchar(int arg);

    public static void main(String[] args) throws InterruptedException {
        /* Pattern for commented tests is: ^01234567#01234567#01234567#01234567#01234567# */
        for (TestThread.TestVariation var: TestThread.TestVariation.values()) {
            CountTest countTest = new CountTest();
            TestThread  t1 = new TestThread(countTest, var);
            TestThread  t2 = new TestThread(countTest, var);

            t1.start();
            t2.start();
            t1.join();
            t2.join();

            putchar('#');
            putchar('\n');
        }
    }
}

class TestThread extends Thread {
    CountTest countTest;
    TestVariation variation;
    final int n = 4;

    @extern
    public static native int putchar(int arg);

    enum TestVariation {
        TEST//,
//        METHOD_INST,
//        METHOD_STATIC,
//        SEGMENT,
//        REENTRANT,
//        EMBEDDED
    };

    TestThread(CountTest countTest, TestVariation variation) {
        this.countTest = countTest;
        this.variation = variation;
    }

    public void run() {
        for (int i = 0; i < n; i++) {
            if (variation.equals(TestVariation.TEST)) {
                countTest.testCount();
//            } else if (variation.equals(TestVariation.METHOD_INST)) {
//                countTest.synchInstance();
//            } else if (variation.equals(TestVariation.METHOD_STATIC)) {
//                countTest.synchStatic();
//            } else if (variation.equals(TestVariation.SEGMENT)) {
//                countTest.segment();
//            } else if (variation.equals(TestVariation.REENTRANT)) {
//                countTest.reentrant();
//            } else if (variation.equals(TestVariation.EMBEDDED)) {
//                countTest.embedded();
            }
        }
    }
}

class CountTest {
    static int staticCount = 0;
    volatile int count = 0;

    @extern
    public static native int putchar(int arg);

    public static void printInt(int n) {
        char[] numbers = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        boolean seenNonZero = false;
        int divsor = 1000000000;
        do {
            int i = n / divsor;
            if (!seenNonZero && i == 0) {
                // skip
            } else {
                seenNonZero = true;
                putchar(numbers[i]);
            }
            n %= divsor;
            divsor /= 10;
        } while (divsor != 0);
        if (!seenNonZero) {
            putchar(numbers[0]);
        }
    }

    // TODO test without synchronization -
    public void testCount() {
        printInt(count++);
    }

    /* synchronized instance method */
    public synchronized void synchInstance() {
        printInt(count++);
    }

    /* synchronized static method */
    public static synchronized void synchStatic() {
        printInt(staticCount++);
    }

    /* segment synchronized on instance */
    public void segment() {
        synchronized(this) {
            printInt(count++);
        }
    }

    /* re-enter synchronized block on same object */
    public void reentrant() {
        synchronized(this) {
            printInt(count);
            synchronized(this) {
                count++;
            }
        }
    }

    /* embed more than one synchronized block */
    public void embedded() {
        Object o = new Object();
        synchronized(this) {
            synchronized(o) {
                printInt(count++);
            }
        }
    }
}


