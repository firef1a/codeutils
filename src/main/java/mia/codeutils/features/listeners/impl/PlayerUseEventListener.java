package mia.codeutils.features.listeners.impl;

import mia.codeutils.features.listeners.AbstractEventListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public interface PlayerUseEventListener extends AbstractEventListener {
    void useBlockCallback(Player player, Level world, InteractionHand hand, HitResult hitResult);
    void useItemCallback(Player player, Level world, InteractionHand hand);
    void useEntityCallback(Player player, Level world, InteractionHand hand, Entity entity, HitResult hitResult);
}
