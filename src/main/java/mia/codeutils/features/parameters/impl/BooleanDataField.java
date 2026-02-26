package mia.codeutils.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mia.codeutils.features.parameters.ParameterDataField;
import mia.codeutils.features.parameters.ParameterIdentifier;

public class BooleanDataField extends ParameterDataField<Boolean> {
    public BooleanDataField(String name, ParameterIdentifier identifier, Boolean defaultValue, boolean isConfig) {
        super(name, identifier, defaultValue, isConfig);
    }

    @Override
    public void serialize(JsonObject jsonObject) {
        jsonObject.addProperty(identifier.getIdentifier(), dataField);
    }

    @Override
    public Boolean deserialize(JsonElement jsonObject) {
        return jsonObject.getAsBoolean();
    }
}
