package mia.modmod.features.impl.moderation.tracker;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mia.modmod.Mod;
import mia.modmod.features.Categories;
import mia.modmod.features.Feature;
import mia.modmod.features.FeatureManager;
import mia.modmod.features.impl.internal.staff.VanishTracker;
import mia.modmod.features.parameters.ParameterIdentifier;
import mia.modmod.features.parameters.impl.FloatDataField;
import mia.modmod.features.parameters.impl.IntegerDataField;
import mia.modmod.mixin.render.RenderTypeAccessor;
import mia.modmod.render.util.ARGB;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.awt.*;
import java.util.HashMap;
import java.util.Locale;

public final class HitRange extends Feature {
    private final IntegerDataField segmentParameter;
    private final FloatDataField rangeThicknessParameter;

    public HitRange(Categories category) {
        super(category, "Hit Range", "hitrange", "shows player hit range");

        segmentParameter = new IntegerDataField("Segments", new ParameterIdentifier(this, "segments"), 100, true);
        rangeThicknessParameter = new FloatDataField("Thickness", new ParameterIdentifier(this, "thickness"), 0.055f, true);
    }

    public static final RenderType QUADS = RenderTypeAccessor.of(
            RenderPipelines.DEBUG_QUADS.getClass().getSimpleName().toLowerCase(Locale.ROOT),
            RenderSetup.builder(RenderPipelines.DEBUG_QUADS)
                .sortOnUpload()
                .useLightmap()
                .useOverlay()
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .createRenderSetup()
        );

    public static void drawCircle(PoseStack.Pose entry, VertexConsumer vertices, AvatarRenderState state) {
        if (Mod.MC.player == null) return;
        if (Mod.MC.level == null) return;

        HashMap<Integer, Player> playerIds = new HashMap<>();
        for (String trackedName : PlayerTracker.getTrackerPlayers()) {
            for (Player player : Mod.MC.level.players()) {
                if (player.nameAndId().name().equals(trackedName)) {
                    playerIds.put(player.getId(), player);
                }
            }
        }

        int playerId = state.id;
        if (!playerIds.containsKey(playerId)) return;

        Player player = playerIds.get(playerId);

        Vec3 playerEyePos = player.getEyePosition();
        float playerRange = (float) player.entityInteractionRange();

        boolean isInRange = false;
        for (Player eachPlayer : Mod.MC.level.players()) {
            if (eachPlayer.getId() != playerId && (!eachPlayer.isSpectator())) {
                if (eachPlayer.getId() == Mod.MC.player.getId() && FeatureManager.getFeature(VanishTracker.class).isInModVanish()) continue;

                AABB boundingBox = eachPlayer.getBoundingBox();
                Vec3 closestPosition = new Vec3(
                        Math.clamp(playerEyePos.x, boundingBox.minX, boundingBox.maxX),
                        Math.clamp(playerEyePos.y, boundingBox.minY, boundingBox.maxY),
                        Math.clamp(playerEyePos.z, boundingBox.minZ, boundingBox.maxZ)
                );
                float distance = (float) closestPosition.distanceTo(playerEyePos);
                if (distance <= playerRange) {
                    isInRange = true;
                    break;
                }
            }
        }

        int color = ARGB.getARGB(isInRange ? 0x7aff5c : 0xff473d, 1f);

        renderCircle(
                entry,
                vertices,
                FeatureManager.getFeature(HitRange.class).segmentParameter.getValue(),
                player.entityInteractionRange(),
                FeatureManager.getFeature(HitRange.class).rangeThicknessParameter.getValue(),
                color
        );
    }

    private static void renderCircle( PoseStack.Pose pose, VertexConsumer vertexConsumer, int segments, double radius, double thickness, int color) {
        float dy = 0.125f;

        for (int i = 0; i < segments; i++) {
            double angle0 = ((i + 0f) / segments) * Math.PI * 2;
            double angle1 = ((i + 1f) / segments) * Math.PI * 2;

            Quad quad = new AngleQuad(angle0, angle1, radius, thickness, dy).generateQuad();

            for (Vector3f vector3f : quad.getVertices()) {
                vertexConsumer.addVertex(pose, vector3f).setColor(color).setNormal(pose, new Vector3f(0, 1, 0));
            }
        }
    }

    private record AngleQuad(double angle0, double angle1, double radius, double thickness, float dy) {
        public Quad generateQuad() {
            double innerRadius = radius - thickness;
            return new Quad(
                    dy,
                    (float) (Math.cos(angle1) * radius),
                    (float) (Math.sin(angle1) * radius),


                    (float) (Math.cos(angle1) * innerRadius),
                    (float) (Math.sin(angle1) * innerRadius),

                    (float) (Math.cos(angle0) * innerRadius),
                    (float) (Math.sin(angle0) * innerRadius),

                    (float) (Math.cos(angle0) * radius),
                    (float) (Math.sin(angle0) * radius)
            );
        }
    }

    private record Quad(float dy, float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        public Vector3f getTL()  {return new Vector3f(x0, dy, y0); }
        public Vector3f getBL()  {return new Vector3f(x1, dy, y1); }
        public Vector3f getBR()  {return new Vector3f(x2, dy, y2); }
        public Vector3f getTR()  {return new Vector3f(x3, dy, y3); }

        public Vector3f[] getVertices() {
            return new Vector3f[]{
                    getTL(),
                    getBL(),
                    getBR(),
                    getTR()
            };
        }
    };
}
