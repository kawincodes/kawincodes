package com.kawincodes.ksession.util;

/** Result of an asynchronous operation. Services always deliver it on the main thread. */
public interface Callback<T> {
    void onSuccess(T value);

    void onError(Throwable error);
}
