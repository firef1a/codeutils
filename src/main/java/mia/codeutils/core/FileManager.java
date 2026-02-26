package mia.codeutils.core;

import mia.codeutils.Mod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class FileManager {
    /**
     * Verify the existence and get the mod elements folder.
     *
     * @return
     */
    public static Path Path() {
        Path path = Mod.MC.gameDirectory.toPath().resolve(Mod.MOD_ID);
        path.toFile().mkdir();
        return path;
    }

    public static void createFolder(String path) {
        Mod.MC.gameDirectory.toPath().resolve(path).toFile().mkdir();
    }

    public static File getConfigFile() {
        return new File(FabricLoader.getInstance().getConfigDir().toFile(), Mod.MOD_ID + ".json");
    }

    public static File getPath(String filePath) {
        return new File(FabricLoader.getInstance().getGameDir().toFile(), filePath);
    }
    
    

    public static void writeFile(File file, String content) throws IOException {
        boolean ignore;
        Files.deleteIfExists(file.toPath());
        Files.createFile(file.toPath());
        if (!file.exists()) ignore = file.createNewFile();
        Files.write(file.toPath(), content.getBytes(), StandardOpenOption.WRITE);
    }


    public static String readFile(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    public static String readConfig(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    public static boolean exists(String fileName) {
        return Files.exists(Path().resolve(fileName));
    }

}