package mia.codeutils.render.util;

import com.mojang.blaze3d.systems.RenderSystem;
import mia.codeutils.Mod;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.DeltaTracker;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class HudMatrixRegistry {
    private static DeltaTracker renderHUDTickCounter;
    public static Matrix4f modelViewMatrix;
    public static Matrix4f projectionMatrix;
    public static Matrix4f positionMatrix;
    public static final int[] lastViewport = new int[4];

    public static void register(WorldRenderContext context) {
        if (getRenderHUDTickCounter() == null) return;
        // It's crucial to copy the matrices here as they are managed by the rendering engine
        // and can be modified or popped from the stack after this event.
        double currentFov = RenderContextHelper.getFov(getRenderHUDTickCounter().getGameTimeDeltaPartialTick(true));

        modelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix());
        projectionMatrix = Mod.MC.gameRenderer.getProjectionMatrix((float) currentFov);
        positionMatrix = new Matrix4f(context.matrices().last().pose());
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, lastViewport);

    }

    public static void setRenderHUDTickCounter(DeltaTracker renderTickCounter) {
        renderHUDTickCounter = renderTickCounter;
    }

    public static DeltaTracker getRenderHUDTickCounter() {
        return renderHUDTickCounter;
    }
}
