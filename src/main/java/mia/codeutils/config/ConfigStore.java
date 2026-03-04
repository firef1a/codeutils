package mia.codeutils.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import mia.codeutils.Mod;
import mia.codeutils.core.FileManager;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Category;
import mia.codeutils.features.Feature;
import mia.codeutils.features.FeatureManager;
import mia.codeutils.features.parameters.ParameterDataField;
import mia.codeutils.features.parameters.ParameterIdentifier;
import mia.codeutils.features.parameters.impl.InternalDataField;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public final class ConfigStore {
    private static JsonObject configData;

    public static boolean hasParameter(ParameterIdentifier identifier) { return configData.has(identifier.getIdentifier()); }

    public static <T> T getParameter(ParameterDataField<T> parameterDataField, T defaultValue) {
        String id = parameterDataField.getIdentifier().getIdentifier();
        T ret = configData.has(id) ? parameterDataField.deserialize(configData.get(id)) : defaultValue;
        return ret == null ? defaultValue : ret;
    }

    public static <T extends Object> void saveParameter(ParameterDataField<T> parameterDataField) {
        parameterDataField.serialize(configData);
    }

    public static void load() {
        // load elements here
        try {
            configData = new JsonParser().parse(FileManager.readConfig(FileManager.getConfigFile())).getAsJsonObject();
        } catch (Exception exception) {
            configData = new JsonObject();
            Mod.error("Config didn't load: " + exception);
        }
    }

    public static void save() {
        for (Feature feature : FeatureManager.getFeatures()) {
            feature.getParameterDataFields().forEach(ConfigStore::saveParameter);
        }

        try {
            FileManager.writeFile(FileManager.getConfigFile(), Mod.gson.toJson(configData));
            Mod.log("Saved config: " + FileManager.getConfigFile().getName());
        } catch (Exception e) {
            Mod.error("Couldn't save config: " + e);
        }
    }

    public static YetAnotherConfigLib getLibConfig() {
        YetAnotherConfigLib.Builder yacl =
                YetAnotherConfigLib.createBuilder()
                        .title(Component.literal("Used for narration. Could be used to render a title in the future."));

        for (Category category : Categories.getCategories()) {
            ConfigCategory.Builder configBuilder = ConfigCategory.createBuilder()
                    .name(Component.literal(category.getName()))
                    .tooltip(Component.literal(category.getDescription()));





            for (Feature feature : category.getFeatures()) {
                ArrayList<? extends ParameterDataField<?>> dataFields = feature.getParameterDataFields();
                Option featureOption = Option.createBuilder(boolean.class)
                        .name(Component.literal(feature.getName()))
                        .description(OptionDescription.createBuilder()
                                .text(Component.literal(feature.getDescription()))
                                .build())
                        .binding(
                                feature.getEnabled(),
                                feature::getEnabled,
                                feature::setEnabled
                        )
                        .controller(TickBoxControllerBuilder::create)
                        .build();

                ArrayList<ParameterDataField<?>> nonInternalParamFields = new ArrayList<>();
                for (ParameterDataField<?> dataField : dataFields) {
                    if (!(dataField instanceof InternalDataField)) nonInternalParamFields.add(dataField);
                }

                if (nonInternalParamFields.isEmpty()) {
                    configBuilder.option(featureOption);
                } else {
                    OptionGroup.Builder featureGroup = OptionGroup.createBuilder()
                            .name(Component.literal(feature.getName()));

                    featureGroup.option(featureOption);
                    for (ParameterDataField dataField : nonInternalParamFields) {

                        dataField.addYACLParameter(featureGroup);
                    }

                    configBuilder.group(featureGroup.build());

                }
            }
            yacl.category(configBuilder.build());
        }


        return yacl.save(ConfigStore::save).build();
    }
}
