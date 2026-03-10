package mia.codeutils.features.impl.development.scanner;

import net.minecraft.core.Vec3i;

import java.io.File;
import java.util.Random;

public class PlotData {
    public String owner, plotName, node;
    public int plotId;
    public Vec3i plotBase;
    public String uuid;

    public PlotData(String owner, String plotName, int plotId, String node) {
        this.owner = owner;
        this.plotName = plotName;
        this.plotId = plotId;
        this.node = node;
        this.uuid = generateUUID();
    }

    private String generateUUID() {
        StringBuilder uuid = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            uuid.append(generateRandomNumber());
        }
        return uuid.toString();
    }

    private String generateRandomNumber() {
        Random random = new Random();
        return "" + random.nextInt(10);
    }

    public String getSaveFileName() {
        String sanitizedPlotName = plotName
                .replace(".", "_")
                .replace(" ", "_")
                .replace("/", "_");
        return String.format("%s_ID-%s_UUID-%s", sanitizedPlotName, plotId, uuid);
    }

    public void setPlotBase(Vec3i plotBase) {
        this.plotBase = plotBase;
    }
}