package br.faustech.memory;

/**
 * Custom exception class to handle memory-related errors.
 */
public class MemoryException extends RuntimeException {

    /**
     * Constructs a new MemoryException with a specified detail message.
     *
     * @param message The detail message that explains the reason for the exception.
     */
    public MemoryException(String message) {

        super(message); // Call superclass constructor with the provided message
    }

}
