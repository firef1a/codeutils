package mia.codeutils.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mia.codeutils.features.parameters.ParameterDataField;
import mia.codeutils.features.parameters.ParameterIdentifier;

public class DoubleDataField extends ParameterDataField<Double> {
    public DoubleDataField(String name, ParameterIdentifier identifier, Double defaultValue, boolean isConfig) {
        super(name, identifier, defaultValue, isConfig);
    }

    @Override
    public void serialize(JsonObject jsonObject) {
        jsonObject.addProperty(identifier.getIdentifier(), dataField);
    }

    @Override
    public Double deserialize(JsonElement jsonObject) {
        return jsonObject.getAsDouble();
    }
}
