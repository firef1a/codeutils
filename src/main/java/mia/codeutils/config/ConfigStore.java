package mia.codeutils.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mia.codeutils.Mod;
import mia.codeutils.core.FileManager;
import mia.codeutils.features.Feature;
import mia.codeutils.features.FeatureManager;
import mia.codeutils.features.parameters.ParameterDataField;
import mia.codeutils.features.parameters.ParameterIdentifier;

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
}
