//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.world.storage.VirtualWorld;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.util.GameRuleHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


/**
 * This mixin injects into StatsCollector to fix bonus multiplier and use it as separate
 * game rule.
 */
@Mixin(value = StatsCollector.class, remap = false)
public class MixinStatsCollector
{
    /**
     * Change bonus multiplier to use custom game rule.
     * @param world The world.
     * @param m The player multiplier.
     * @param cir Callback info.
     */
    @Inject(method = "lambda$initServer$1",
        at = @At(value = "HEAD"),
        cancellable = true)
    private static void fixBonusMultiplier(VirtualWorld world,
        Float m,
        CallbackInfoReturnable<Float> cir)
    {
        cir.setReturnValue(m * GameRuleHelper.getRule(VaultHuntersExtraRules.BONUS_XP, world).get().getMultiplier());
        cir.cancel();
    }


    /**
     * Change objective multiplier to use custom game rule.
     * @param world The world.
     * @param m The player multiplier.
     * @param cir Callback info.
     */
    @Inject(method = "lambda$initServer$2",
        at = @At(value = "HEAD"),
        cancellable = true)
    private static void fixObjectiveMultiplier(VirtualWorld world,
        Float m,
        CallbackInfoReturnable<Float> cir)
    {
        cir.setReturnValue(m * GameRuleHelper.getRule(VaultHuntersExtraRules.COMPLETION_XP, world).get().getMultiplier());
        cir.cancel();
    }
}
