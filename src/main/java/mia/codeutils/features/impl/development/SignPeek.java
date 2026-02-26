package mia.codeutils.features.impl.development;

import mia.codeutils.ColorBank;
import mia.codeutils.Mod;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.impl.internal.mode.LocationAPI;
import mia.codeutils.features.listeners.impl.RenderHUD;
import mia.codeutils.render.util.DrawContextHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SignPeek extends Feature implements RenderHUD {
    public SignPeek(Categories category) {
        super( category, "Sign Peek", "signpeek", "sign that ur gay");
    }

    @Override
    public void renderHUD(GuiGraphics context, DeltaTracker tickCounter) {
        if (!LocationAPI.getMode().canViewCode()) return;
        if (Mod.MC.player == null) return;
        if (Mod.MC.level == null) return;

        HitResult hitResult = Mod.MC.player.pick(4.5, 0, false);
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos blockPos = blockHitResult.getBlockPos();

            BlockEntity blockState = Mod.MC.level.getBlockEntity(blockPos);
            if (blockState instanceof SignBlockEntity signBlockEntity) {
                ArrayList<String> messages = new ArrayList<>(Arrays.stream(signBlockEntity.getFrontText().getMessages(true)).map(Component::getString).toList());
                ArrayList<String> validHeaders = new ArrayList<>(List.of("PLAYER EVENT", "ENTITY EVENT", "GAME EVENT", "FUNCTION", "CALL FUNCTION", "START PROCESS", "PROCESS"));

                String header = messages.get(0);
                String body = messages.get(1);
                if (validHeaders.contains(header)) {
                    if (!body.isEmpty()) {
                        DrawContextHelper.drawTooltip(
                                context,
                                List.of(
                                        Component.literal(header.strip()).withColor(ColorBank.MC_GRAY),
                                        Component.literal(body).withColor(0xd6d6d6)
                                ),
                                Mod.getScaledWindowWidth() / 2,
                                Mod.getScaledWindowHeight() / 2,
                                true
                        );
                    }
                }
            }
        }
    }
}
