/**
 * <copyright>
 * Copyright (c) 2026, AGG XML Serialization Refactoring Project.
 * All rights reserved.
 * </copyright>
 */
package agg.xml.core;

/**
 * Exception thrown when XML serialization or deserialization fails.
 * This is a checked exception that wraps underlying causes such as I/O errors,
 * XML parsing errors, or type conversion failures.
 */
public class XMLSerializationException extends Exception {
    
    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new XMLSerializationException with no detail message.
     */
    public XMLSerializationException() {
        super();
    }
    
    /**
     * Creates a new XMLSerializationException with the specified detail message.
     * 
     * @param message The detail message
     */
    public XMLSerializationException(String message) {
        super(message);
    }
    
    /**
     * Creates a new XMLSerializationException with the specified detail message and cause.
     * 
     * @param message The detail message
     * @param cause The underlying cause
     */
    public XMLSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new XMLSerializationException with the specified cause.
     * 
     * @param cause The underlying cause
     */
    public XMLSerializationException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Creates a new XMLSerializationException with the specified detail message,
     * cause, suppression enabled or disabled, and writable stack trace enabled or disabled.
     * 
     * @param message The detail message
     * @param cause The underlying cause
     * @param enableSuppression Whether suppression is enabled
     * @param writableStackTrace Whether the stack trace should be writable
     */
    protected XMLSerializationException(String message, Throwable cause, 
                                       boolean enableSuppression, 
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
