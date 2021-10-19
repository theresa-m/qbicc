package org.qbicc.runtime.main;

import static org.qbicc.runtime.CNative.*;
import static org.qbicc.runtime.stdc.Stdlib.malloc;
import static org.qbicc.runtime.posix.PThread.*;

import org.qbicc.runtime.ThreadScoped;

/**
 * VM Utilities.
 */
public final class VM {

    /**
     * Internal holder for the pointer to the current thread.  Thread objects are not allowed to move in memory
     * after being constructed.
     * <p>
     * GC must take care to include this object in the root set of each thread.
     */
    @ThreadScoped
    @export
    @SuppressWarnings("unused")
    static Thread _qbicc_bound_thread;

    // TODO manually create a linked list - don't want to use fancy java classes if this will be used by the GC
    // TODO what about the first thread? will that be recorded?
//    static native_thread_ptr _native_thread_list;
//    // TODO mutex cleanup needed
//    static pthread_mutex_t_ptr _native_thread_list_mutex = VMHelpers.create_recursive_pthread();

//    @extern
//    public static native int putchar(int arg);

    /* TODO comment - return false if failed */
    static boolean addToNativeThreadList(java.lang.Thread threadObject, pthread_t_ptr pthread) {
        //pthread_mutex_lock(_native_thread_list_mutex);

        // TODO move to VMHelpers for now - classloading issues
        ptr<?> nativeThreadPtrVoid = malloc(sizeof(native_thread_ptr.class));
        if (nativeThreadPtrVoid.isNull()) {
            return false;
        }
        ptr<native_thread> newNativeThreadPtr = (ptr<native_thread>) castPtr(nativeThreadPtrVoid, native_thread.class);

        native_thread newNativeThread = newNativeThreadPtr.deref();

        newNativeThread.test = 3;
        VMHelpers.printInt(newNativeThread.test);

        newNativeThread.threadObject = threadObject;
        newNativeThread.pthread = pthread;
//
//        if (_native_thread_list == null) {
//            putchar('T');
//            newNativeThread.next = (native_thread_ptr) newNativeThreadPtr;
//            _native_thread_list = (native_thread_ptr) newNativeThreadPtr;
//        } else {
//            native_thread_ptr lastEntryPtr = _native_thread_list;
//            native_thread lastEntry = lastEntryPtr.deref();
//            while (lastEntry.next != _native_thread_list) {
//                putchar('T');
//                lastEntryPtr = lastEntry.next;
//                lastEntry = lastEntryPtr.deref();
//            }
//            newNativeThread.next = lastEntry.next;
//            lastEntry.next = (native_thread_ptr) newNativeThreadPtr;
//            putchar('T');
//
//        }
        //pthread_mutex_unlock(_native_thread_list_mutex);
        return true;
    }



    // Temporary manual implementation
    @SuppressWarnings("ManualArrayCopy")
    static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        if (src instanceof Object[] && dest instanceof Object[]) {
            Object[] srcArray = (Object[]) src;
            Object[] destArray = (Object[]) dest;
            for (int i = 0; i < length; i ++) {
                destArray[destPos + i] = srcArray[srcPos + i];
            }
        } else if (src instanceof byte[] && dest instanceof byte[]) {
            byte[] srcArray = (byte[]) src;
            byte[] destArray = (byte[]) dest;
            for (int i = 0; i < length; i ++) {
                destArray[destPos + i] = srcArray[srcPos + i];
            }
        } else if (src instanceof short[] && dest instanceof short[]) {
            short[] srcArray = (short[]) src;
            short[] destArray = (short[]) dest;
            for (int i = 0; i < length; i ++) {
                destArray[destPos + i] = srcArray[srcPos + i];
            }
        } else if (src instanceof int[] && dest instanceof int[]) {
            int[] srcArray = (int[]) src;
            int[] destArray = (int[]) dest;
            for (int i = 0; i < length; i ++) {
                destArray[destPos + i] = srcArray[srcPos + i];
            }
        } else if (src instanceof long[] && dest instanceof long[]) {
            long[] srcArray = (long[]) src;
            long[] destArray = (long[]) dest;
            for (int i = 0; i < length; i ++) {
                destArray[destPos + i] = srcArray[srcPos + i];
            }
        } else if (src instanceof char[] && dest instanceof char[]) {
            char[] srcArray = (char[]) src;
            char[] destArray = (char[]) dest;
            for (int i = 0; i < length; i ++) {
                destArray[destPos + i] = srcArray[srcPos + i];
            }
        } else if (src instanceof float[] && dest instanceof float[]) {
            float[] srcArray = (float[]) src;
            float[] destArray = (float[]) dest;
            for (int i = 0; i < length; i ++) {
                destArray[destPos + i] = srcArray[srcPos + i];
            }
        } else if (src instanceof double[] && dest instanceof double[]) {
            double[] srcArray = (double[]) src;
            double[] destArray = (double[]) dest;
            for (int i = 0; i < length; i ++) {
                destArray[destPos + i] = srcArray[srcPos + i];
            }
        } else if (src instanceof boolean[] && dest instanceof boolean[]) {
            boolean[] srcArray = (boolean[]) src;
            boolean[] destArray = (boolean[]) dest;
            for (int i = 0; i < length; i ++) {
                destArray[destPos + i] = srcArray[srcPos + i];
            }
        } else {
            throw new ClassCastException("Invalid array types for copy");
        }
    }
}
