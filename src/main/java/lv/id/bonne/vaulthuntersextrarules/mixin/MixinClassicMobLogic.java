//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import iskallia.vault.core.vault.ClassicMobLogic;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.util.GameRuleHelper;
import net.minecraft.server.level.ServerPlayer;


/**
 * This mixin injects into ClassicMobLogic#ENTITY_DROPS to increase amount of shards being dropped when entity is killed.
 */
@Mixin(ClassicMobLogic.class)
public class MixinClassicMobLogic
{
    /**
     * This mixin changes shard count return by multiplier from game rule.
     * @param shardCount The original shard count
     * @param serverPlayer The server player who triggers it
     * @return Adjusted shard value.
     */
    @ModifyVariable(method = "lambda$initServer$6", at = @At("STORE"), ordinal = 0, remap = false)
    private static int injected(int shardCount, @Local(ordinal = 0) ServerPlayer serverPlayer)
    {
        return Math.round(shardCount *
            GameRuleHelper.getRule(VaultHuntersExtraRules.SOUL_SHARD_DROP, serverPlayer).get().getMultiplier());
    }
}
