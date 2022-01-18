package org.qbicc.runtime.main;

import static org.qbicc.runtime.CNative.*;
import static org.qbicc.runtime.stdc.Stdlib.malloc;
import static org.qbicc.runtime.posix.PThread.*;

import org.qbicc.runtime.ThreadScoped;
import org.qbicc.runtime.patcher.Replace;
import org.qbicc.runtime.posix.PThread;
import org.qbicc.runtime.stdc.Stdint;

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

    static boolean firstIsSet = false; // TODO add main thread to list then wouldn't need this?
    static native_thread_ptr _native_thread_list;
    // TODO need patch class to initialize this properly at runtime?
    //static pthread_mutex_t_ptr _native_thread_list_mutex_patch = VMHelpers.create_recursive_pthread();

    @extern
    public static native int putchar(int arg);

    /* TODO comment - return false if failed */
    static boolean addToNativeThreadList(void_ptr threadObject, pthread_t_ptr pthread) {
        //pthread_mutex_lock(_native_thread_list_mutex); // TODO this is wrong

        ptr<?> newNativeThreadPtrVoid = malloc(sizeof(native_thread_ptr.class));
        if (newNativeThreadPtrVoid.isNull()) {
            return false;
        }
        ptr<native_thread> newNativeThreadPtr = (ptr<native_thread>) castPtr(newNativeThreadPtrVoid, native_thread.class);

        /* set threadObject */
        ptr<void_ptr> newThreadObjectPtr = addr_of(newNativeThreadPtr.sel().threadObjectPtr);
        newThreadObjectPtr.set(0, threadObject);

        /* set pthread */
        ptr<pthread_t_ptr> newPthreadPtr = addr_of(newNativeThreadPtr.sel().pthread);
        newPthreadPtr.set(0, pthread);

        if (!firstIsSet) {
            firstIsSet = true;

            /* set next */
            ptr<ptr<?>> newNextPtr = addr_of(newNativeThreadPtr.sel().next);
            newNextPtr.set(0, newNativeThreadPtrVoid);

            _native_thread_list = (native_thread_ptr) newNativeThreadPtr;
        } else {
            // TODO add list - should list be circular? pros and cons?
        }

//        } else {
//            putchar('P');
////            VMHelpers.printInt(_native_thread_list.deref().test);
////            ptr<native_thread> lastEntryPtr = (ptr<native_thread>) castPtr(_native_thread_list.deref().next, native_thread.class);
////            if (_native_thread_list  == lastEntryPtr) {
////                putchar('K');
////            }
//            //boolean result = lastEntryPtr.equals((ptr<native_thread>)_native_thread_list);
//            //return lastEntryPtr == (ptr<native_thread>)_native_thread_list;
//        //    /*while*/ if (!lastEntryPtr.equals((ptr<native_thread>)_native_thread_list)) {
//                //putchar('S');
//                //lastEntryPtr = (ptr<native_thread>) castPtr(lastEntryPtr.deref().next, native_thread.class);
//          //  }
////            newNativeThreadPtr.deref().next = lastEntryPtr.deref().next;
////            lastEntryPtr.deref().next = newNativeThreadPtrVoid;
////            putchar('R');
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
