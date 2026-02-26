package mia.codeutils.features.impl.development;

import mia.codeutils.Mod;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.listeners.impl.PacketListener;
import mia.codeutils.features.listeners.impl.RenderHUD;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CPUDisplay extends Feature implements RenderHUD, PacketListener {
    private double currentCPU = 0;
    private double displayCPU = 0;
    private long overlayTimeoutTimestamp = 0L;

    private static final double animationSpeed = 0.075;
    private double animation;

    public CPUDisplay(Categories category) {
        super(category, "CPU Display", "cpuwheel", "its a wheel");
        animation = 0;
    }


    @Override
    public void renderHUD(GuiGraphics context, DeltaTracker tickCounter) {
    }
    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {
        if (Mod.MC.player == null) return;
        Matcher matcher;
        boolean found = false;

        if (packet instanceof ClientboundSetActionBarTextPacket(Component text)) {
            Pattern pattern = Pattern.compile("CPU Usage: \\[▮{20}] \\((\\d+\\.\\d+)%\\)");
            matcher = pattern.matcher(text.getString());

            if (matcher.find()) {
                currentCPU = Double.parseDouble(matcher.group(1)) / 100.0;
                ci.cancel();
                found = true;
            }
        }

        if (found) {
            overlayTimeoutTimestamp = System.currentTimeMillis() + 1500L;
        }
    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {

    }
}
