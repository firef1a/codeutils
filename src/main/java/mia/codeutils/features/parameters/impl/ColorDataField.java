package mia.codeutils.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import mia.codeutils.features.parameters.ParameterDataField;
import mia.codeutils.features.parameters.ParameterIdentifier;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class ColorDataField extends ParameterDataField<Color> {
    public ColorDataField(String name, ParameterIdentifier identifier, Color defaultValue, boolean isConfig) {
        super(name, identifier, defaultValue, isConfig);
    }

    public int getRGB() {
        return getValue().getRGB();
    }

    @Override
    public void serialize(JsonObject jsonObject) {
        jsonObject.addProperty(identifier.getIdentifier(), dataField.getRGB());
    }

    @Override
    public Color deserialize(JsonElement jsonObject) {
        return new Color(jsonObject.getAsInt());
    }

    @Override
    public void addYACLParameter(OptionGroup.Builder featureGroup) {
        featureGroup.option(
                Option.createBuilder(Color.class)
                        .name(Component.literal(this.getName()))
                        .binding(
                                this.getValue(),
                                this::getValue,
                                this::setValue
                        )
                        .controller(ColorControllerBuilder::create)
                        .build()
        );
    }
}
