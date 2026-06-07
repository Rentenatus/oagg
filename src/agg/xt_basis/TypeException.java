/**
 * <copyright>
 * Copyright (c) 1995, 2015 Technische Universitaet Berlin. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License
 * v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

/**
 * Exception thrown when an invalid typed graph object is added to a graph.
 * This exception wraps a {@link TypeError} that contains detailed information
 * about the type violation.
 *
 * @see TypeError
 */
@SuppressWarnings("serial")
public class TypeException extends Exception {

    /**
     * The type error that caused this exception.
     */
    TypeError typeError = null;

    /**
     * Constructs a new TypeException without a detail message.
     */
    public TypeException() {
    }

    /**
     * Constructs a new TypeException with the specified detail message.
     *
     * @param message the detail message describing the type violation
     */
    public TypeException(String message) {
        super(message);
    }

    /**
     * Constructs a new TypeException wrapping a TypeError.
     *
     * @param error the TypeError that caused this exception
     */
    public TypeException(TypeError error) {
        super(error.getMessage());
        this.typeError = error;
    }

    /**
     * Returns the TypeError that caused this exception.
     *
     * @return the TypeError, or null if this exception was not created from a TypeError
     */
    public TypeError getTypeError() {
        return this.typeError;
    }
}
