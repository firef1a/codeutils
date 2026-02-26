package mia.codeutils.features;

import mia.codeutils.features.listeners.impl.AlwaysEnabled;
import mia.codeutils.features.parameters.ParameterDataField;
import mia.codeutils.features.parameters.ParameterIdentifier;
import mia.codeutils.features.parameters.impl.BooleanDataField;

import java.util.ArrayList;

public abstract class Feature {
    protected String id, name, description;
    protected Category category;
    private final ArrayList<ParameterDataField<?>> parameterDataFields;
    private final BooleanDataField enabledParameter;

    public Feature(Categories category, String name, String id, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parameterDataFields = new ArrayList<>();

        enabledParameter = new BooleanDataField("Enabled", ParameterIdentifier.of(this, "enabled"), true, false);

        category.getCategory().addFeature(this);
    }

    public ArrayList<? extends ParameterDataField<?>> getParameterDataFields() { return parameterDataFields; }
    public void addParameter(ParameterDataField<?> parameterDataField) {
        parameterDataFields.add(parameterDataField);
    }

    public void setCategory(Category category) { this.category = category; }
    public void setEnabled(boolean enabled) { this.enabledParameter.setValue(enabled); }
    public boolean getEnabled() { return getAlwaysEnabled() || this.enabledParameter.getValue(); }
    public boolean getAlwaysEnabled() { return this instanceof AlwaysEnabled; }

    public String getID() { return this.id; }
    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
}
