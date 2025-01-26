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
public class ParameterString extends Parameter<String> {

    public ParameterString(String paramName) {
        super(paramName);
    }

    public ParameterString(String paramName, String value) {
        super(paramName, value);
    }

    @Override
    public Object getParamTypeName() {
        return "String";
    }
}
