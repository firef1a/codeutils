package mia.codeutils.features.parameters.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumDropdownControllerBuilder;
import mia.codeutils.features.parameters.ParameterDataField;
import mia.codeutils.features.parameters.ParameterIdentifier;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
public class EnumDataField<T extends Enum> extends ParameterDataField<T> {
    public EnumDataField(String name, ParameterIdentifier identifier, T defaultValue, boolean isConfig) {
        super(name, identifier, defaultValue, isConfig);
    }

    @Override
    public void serialize(@NotNull JsonObject jsonObject) {
        jsonObject.addProperty(identifier.getIdentifier(), dataField.toString());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(@NotNull JsonElement jsonObject) {
        try {
            return (T) Enum.valueOf(this.classType, jsonObject.getAsString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setValue(Enum value) {
        this.dataField = (T) value;
    }

    @Override
    public T getValue() {
        return this.dataField;
    }

    @Override
    public void addYACLParameter(OptionGroup.Builder featureGroup) {
        featureGroup.option(
                Option.createBuilder(Enum.class)
                        .name(Component.literal(this.getName()))
                        .binding(
                                this.getValue(),
                                this::getValue,
                                this::setValue
                        )
                        .controller(EnumDropdownControllerBuilder::create)
                        .build()
        );
    }
}
