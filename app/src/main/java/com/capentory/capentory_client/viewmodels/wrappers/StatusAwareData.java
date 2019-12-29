package com.capentory.capentory_client.viewmodels.wrappers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StatusAwareData<T> {

    @NonNull
    private State status;

    @Nullable
    private T data;

    @Nullable
    private Throwable error;

    public StatusAwareData() {
        this.status = State.INITIALIZED;
        this.data = null;
        this.error = null;
    }

    public StatusAwareData<T> fetching() {
        this.status = State.FETCHING;
        this.data = null;
        this.error = null;
        return this;
    }

    public StatusAwareData<T> success(@NonNull T data) {
        this.status = State.SUCCESS;
        this.data = data;
        this.error = null;
        return this;
    }

    public StatusAwareData<T> error(@NonNull Exception exception) {
        this.status = State.ERROR;
        this.data = null;
        this.error = exception;
        return this;
    }


    @NonNull
    public State getStatus() {
        return status;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public Throwable getError() {
        return error;
    }



    public enum State {
        INITIALIZED,
        SUCCESS,
        ERROR,
        FETCHING;
    }
}