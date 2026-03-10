package mia.codeutils.features.impl.development;

import mia.codeutils.ColorBank;
import mia.codeutils.Mod;
import mia.codeutils.core.MathUtils;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.listeners.impl.PacketListener;
import mia.codeutils.features.listeners.impl.RenderHUD;
import mia.codeutils.render.util.*;
import mia.codeutils.render.util.Point;
import mia.codeutils.render.util.elements.DrawRect;
import mia.codeutils.render.util.elements.DrawText;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CPUDisplay extends Feature implements RenderHUD, PacketListener {
    private double currentCPU = 0;
    private double displayCPU = 0;
    private long overlayTimeoutTimestamp = 0L;
    private double animation;

    public DrawRect container = new DrawRect(new Point(10,20), new Point(200,7), 0, new ARGB(0x080808, 0.0f));
    public DrawText cpuText = new DrawText(new Point(1,-1), Component.empty(), 0, 0, true);

    public CPUDisplay(Categories category) {
        super(category, "CPU Display", "cpuwheel", "Displays CPU % as a solid bar, requires other CPU HUDs from other mods (such as CodeClient) to be disabled first.");
        cpuText.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));
    }

    public boolean isDisplayed() { return animation > 0; };

    @Override
    public void renderHUD(GuiGraphics context, DeltaTracker tickCounter) {
        //context.scissorStack.push(new ScreenRectangle(0,0,500,500));
        container.clearDrawables();

        double percentage = 100f * displayCPU;// * (Mth.clamp(displayCPU, 0f, 1f));

        double easing = MathUtils.easeInOutSine(animation);
        double alpha = 0.8f * easing;
        int usedColor = ARGB.lerpColor(0x9aff75, 0xff2b2b, (float) displayCPU);
        DrawRect usedCPU = new DrawRect(new Point(0,0), new Point((int) (Mth.clamp(displayCPU, 0f, 1f) * container.getWidth()), container.getHeight()), 0, new ARGB(usedColor, alpha), container);
        DrawRect unusedCPU = new DrawRect(new Point(usedCPU.getWidth(), 0), new Point(container.getWidth() - usedCPU.getWidth(),container.getHeight()), 0, new ARGB(0x080808, alpha), container);

        //DrawRect bottomLiner = new DrawOutlineRect(new Point(0,0), new Point(container.getWidth(), 1), 0, new ARGB(0x383838, 0.8f), container);
        //bottomLiner.setParentBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));

        cpuText = new DrawText(cpuText.getRawPosition(), Component.literal("CPU: ").withColor(ColorBank.WHITE).append(Component.literal(MathUtils.roundToDecimalPlaces(percentage, 3) + "%").withColor(ColorBank.MC_GRAY)), 0, (float) easing, true, container);
        cpuText.setSelfBinding(new DrawBinding(AxisBinding.NONE, AxisBinding.FULL));

        if (System.currentTimeMillis() < overlayTimeoutTimestamp) {
            animation = Mth.clamp(animation + 0.2, 0, 1);
        } else {
            animation = Mth.clamp(animation - 0.2, 0, 1);
        }
        if (animation > 0) container.render(context, 0, 0);

        displayCPU = displayCPU + ((currentCPU - displayCPU) / 4);
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
            overlayTimeoutTimestamp = System.currentTimeMillis() + 2000L;
        }
    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) { }
}

