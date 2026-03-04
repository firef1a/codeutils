package mia.codeutils.render.util.renderstates;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mia.codeutils.render.util.ARGB;
import mia.codeutils.render.util.Point;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.NonNull;

public record ColoredQuadRenderState(Matrix3x2fc pose, Point p0, Point p1, Point p2, Point p3, ARGB color, @org.jspecify.annotations.Nullable ScreenRectangle scissorArea, @org.jspecify.annotations.Nullable ScreenRectangle bounds) implements GuiElementRenderState {
    public static RenderPipeline pipeline = RenderPipelines.DEBUG_QUADS;
    public static TextureSetup textureSetup = TextureSetup.noTexture();
    public ColoredQuadRenderState(GuiGraphics context, Matrix3x2fc matrix3x2fc, Point p0, Point p1, Point p2, Point p3, ARGB color) {
        this(matrix3x2fc, p0, p1, p2, p3, color, context.scissorStack.peek(), getBounds(p0.x(), p0.y(), p3.x(), p3.y(), matrix3x2fc, context.scissorStack.peek()));
    }

    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.p0.x(), (float)this.p0.y()).setColor(this.color.getARGB());
        vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.p1.x(), (float)this.p1.y()).setColor(this.color.getARGB());
        vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.p2.x(), (float)this.p2.y()).setColor(this.color.getARGB());
        vertexConsumer.addVertexWith2DPose(this.pose(), (float)this.p3.x(), (float)this.p3.y()).setColor(this.color.getARGB());
    }

    @Override
    public @NonNull RenderPipeline pipeline() { return pipeline; }

    @Override
    public @NonNull TextureSetup textureSetup() { return textureSetup; }

    private static @org.jspecify.annotations.Nullable ScreenRectangle getBounds(int i, int j, int k, int l, Matrix3x2fc matrix3x2fc, @org.jspecify.annotations.Nullable ScreenRectangle screenRectangle) {
        ScreenRectangle screenRectangle2 = (new ScreenRectangle(i, j, k - i, l - j)).transformMaxBounds(matrix3x2fc);
        return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
    }
}