package mia.codeutils.features.parameters;

import java.util.LinkedHashMap;

public abstract class FeatureParameter {
    private ParameterDataField dataField;

    public abstract void save(LinkedHashMap<String, ParameterDataField> config);
}
