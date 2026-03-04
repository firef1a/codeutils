package mia.codeutils.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import mia.codeutils.features.parameters.ParameterDataField;
import mia.codeutils.features.parameters.ParameterIdentifier;
import net.minecraft.network.chat.Component;

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

    @Override
    public void addYACLParameter(OptionGroup.Builder featureGroup) {
        featureGroup.option(
                Option.createBuilder(boolean.class)
                        .name(Component.literal(this.getName()))
                        .binding(
                                this.getValue(),
                                this::getValue,
                                this::setValue
                        )
                        .controller(TickBoxControllerBuilder::create)
                        .build()
        );
    }
}
