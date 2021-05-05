package com.universalna.nsds.exception;

import java.io.IOException;

public interface IoExceptionHandler {

    default <T> T tryIoOperation(final IoOperation<T> operation) {
        try {
            return operation.perform();
        } catch (final IOException e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    @FunctionalInterface
    interface IoOperation<T> {

        T perform() throws IOException;

    }
}
