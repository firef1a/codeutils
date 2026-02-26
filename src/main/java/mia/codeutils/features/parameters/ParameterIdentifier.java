package mia.codeutils.features.parameters;

import mia.codeutils.features.Feature;

public record ParameterIdentifier(Feature feature, String parameter) {
    public String getIdentifier() {
        return feature.getID() + ":" + parameter;
    }
    public static ParameterIdentifier of(Feature feature, String parameter) {
        return new ParameterIdentifier(feature, parameter);
    }
}
