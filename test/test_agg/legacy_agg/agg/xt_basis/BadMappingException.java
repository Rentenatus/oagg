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
 * Exception thrown by methods of the class {@link OrdinaryMorphism}, especially by
 * {@link OrdinaryMorphism#addMapping}, to indicate a violation of some morphism
 * property. A more detailed description of the violation cause will be given in
 * the exception's detail message.
 *
 * @see OrdinaryMorphism
 * @see OrdinaryMorphism#addMapping
 */
@SuppressWarnings("serial")
public class BadMappingException extends RuntimeException {

    /**
     * Constructs a new exception without any detail message.
     */
    public BadMappingException() {
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message describing the violation cause
     */
    public BadMappingException(String message) {
        super(message);
    }
}
