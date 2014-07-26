package com.senz.sdk.utils;

import java.lang.Runnable;
import java.lang.Thread;

public class Asyncfied {
    public interface Asyncfiable<T> {
        public T runAndReturn() throws Exception;
        public void onReturn(T t);
        public void onError(Exception e);
    }

    private Asyncfiable mAsyncfied;

    public Asyncfied(Asyncfiable a) {
        if (a == null)
            throw new NullPointerException();
        this.mAsyncfied = a;
    }

    public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Object ret;
                try {
                    ret = Asyncfied.this.mAsyncfied.runAndReturn();
                }
                catch (Exception e) {
                    Asyncfied.this.mAsyncfied.onError(e);
                    return;
                }
                Asyncfied.this.mAsyncfied.onReturn(ret);
            }
        }).start();
    }

    public static void runAsyncfiable(Asyncfiable a) {
        Asyncfied as = new Asyncfied(a);
        as.run();
    }
}
