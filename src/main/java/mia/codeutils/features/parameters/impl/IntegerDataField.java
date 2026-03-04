package mia.codeutils.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import mia.codeutils.features.parameters.ParameterDataField;
import mia.codeutils.features.parameters.ParameterIdentifier;
import net.minecraft.network.chat.Component;

public class IntegerDataField extends ParameterDataField<Integer> {
    public IntegerDataField(String name, ParameterIdentifier identifier, Integer defaultValue, boolean isConfig) {
        super(name, identifier, defaultValue, isConfig);
    }

    @Override
    public void serialize(JsonObject jsonObject) {
        jsonObject.addProperty(identifier.getIdentifier(), dataField);
    }

    @Override
    public Integer deserialize(JsonElement jsonObject) {
        return jsonObject.getAsInt();
    }

    @Override
    public void addYACLParameter(OptionGroup.Builder featureGroup) {
        featureGroup.option(
                Option.createBuilder(Integer.class)
                        .name(Component.literal(this.getName()))
                        .binding(
                                this.getValue(),
                                this::getValue,
                                this::setValue
                        )
                        .controller(IntegerFieldControllerBuilder::create)
                        .build()
        );
    }
}
