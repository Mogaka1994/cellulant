package com.moha.backend.chama.exception;
/**
 *
 * @author moha
 */
public class NonRollbackException extends Exception {

    public NonRollbackException() {
    }

    public NonRollbackException(String message) {
        super(message);
    }

}