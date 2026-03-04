package mia.codeutils.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import mia.codeutils.features.parameters.ParameterDataField;
import mia.codeutils.features.parameters.ParameterIdentifier;
import net.minecraft.network.chat.Component;

public class StringDataField extends ParameterDataField<String> {
    public StringDataField(String name, ParameterIdentifier identifier, String defaultValue, boolean isConfig) {
        super(name, identifier, defaultValue, isConfig);
    }

    @Override
    public void serialize(JsonObject jsonObject) {
        jsonObject.addProperty(identifier.getIdentifier(), dataField);
    }

    @Override
    public String deserialize(JsonElement jsonObject) {
        return jsonObject.getAsString();
    }

    @Override
    public void addYACLParameter(OptionGroup.Builder featureGroup) {
        featureGroup.option(
                Option.createBuilder(String.class)
                        .name(Component.literal(this.getName()))
                        .binding(
                                this.getValue(),
                                this::getValue,
                                this::setValue
                        )
                        .controller(StringControllerBuilder::create)
                        .build()
        );
    }
}
