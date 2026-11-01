/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package agg.xt_basis;

/**
 * Factory for creating type validation strategies based on validation level.
 *
 * @author Janusch Rentenatus
 */
public final class TypeValidationStrategyFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private TypeValidationStrategyFactory() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a type validation strategy for the specified level.
     *
     * @param level the validation level (from TypeSet constants)
     * @return the appropriate validation strategy
     */
    public static TypeValidationStrategy createStrategy(int level) {
        switch (level) {
            case TypeSet.DISABLED:
                return TypeValidationDisabledStrategy.INSTANCE;
            case TypeSet.ENABLED_INHERITANCE:
                return TypeValidationInheritanceStrategy.INSTANCE;
            case TypeSet.ENABLED:
                return TypeValidationEnabledStrategy.INSTANCE;
            case TypeSet.ENABLED_MAX:
                return TypeValidationEnabledMaxStrategy.INSTANCE;
            case TypeSet.ENABLED_MAX_MIN:
                return TypeValidationEnabledMaxMinStrategy.INSTANCE;
            case TypeSet.ENABLED_MIN:
                return TypeValidationEnabledMinStrategy.INSTANCE;
            default:
                // Default to disabled for unknown levels
                return TypeValidationDisabledStrategy.INSTANCE;
        }
    }
}
