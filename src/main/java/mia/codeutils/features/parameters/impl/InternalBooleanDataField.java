package mia.codeutils.features.parameters.impl;

import mia.codeutils.features.parameters.ParameterIdentifier;

public class InternalBooleanDataField extends BooleanDataField implements InternalDataField {
    public InternalBooleanDataField(String name, ParameterIdentifier identifier, Boolean defaultValue, boolean isConfig) {
        super(name, identifier, defaultValue, isConfig);
    }
}
