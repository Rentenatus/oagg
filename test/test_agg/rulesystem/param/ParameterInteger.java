/**
 * <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package test_agg.rulesystem.param;

/**
 *
 * @author Janusch Rentenatus
 */
public class ParameterInteger extends Parameter<Integer> {

    public ParameterInteger(String paramName) {
        super(paramName);
    }

    public ParameterInteger(String paramName, Integer value) {
        super(paramName, value);
    }

    @Override
    public Object getParamTypeName() {
        return "int";
    }
}
