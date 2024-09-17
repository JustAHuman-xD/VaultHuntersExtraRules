//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthunters.extrarules.mixin;


import iskallia.vault.core.event.common.ListenerJoinEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.StatsCollector;
import iskallia.vault.core.world.storage.VirtualWorld;
import lv.id.bonne.vaulthunters.extrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthunters.extrarules.util.GameRuleHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


/**
 * This mixin injects into StatsCollector to fix bonus multiplier and use it as separate
 * game rule.
 */
@Mixin(value = StatsCollector.class, remap = false)
public class MixinStatsCollector
{
    @Inject(method = "lambda$initServer$4",
            at = @At(value = "HEAD"))
    private void injectVariableAssign(Vault vault, VirtualWorld world, ListenerJoinEvent.Data data, CallbackInfo ci)
    {
        vault_hunters_extra_rules$player = data.getListener().getPlayer().orElse(null);
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
        cir.setReturnValue(m * GameRuleHelper.getRule(VaultHuntersExtraRules.COMPLETION_XP, vault_hunters_extra_rules$player).get().getMultiplier());
        cir.cancel();
    }

    /**
     * Change bonus multiplier to use custom game rule.
     * @param world The world.
     * @param m The player multiplier.
     * @param cir Callback info.
     */
    @Inject(method = "lambda$initServer$1",
            at = @At(value = "HEAD"),
            cancellable = true)
    private static void fixBonusMultiplier(VirtualWorld world, Float m, CallbackInfoReturnable<Float> cir)
    {
        cir.setReturnValue(m * GameRuleHelper.getRule(VaultHuntersExtraRules.BONUS_XP, world, vault_hunters_extra_rules$player).get().getMultiplier());
        cir.cancel();
    }

    @Inject(method = "lambda$initServer$4",
            at = @At(value = "RETURN"))
    private void injectVariableRemove(Vault vault, VirtualWorld world, ListenerJoinEvent.Data data, CallbackInfo ci)
    {
        vault_hunters_extra_rules$player = null;
    }

    @Unique
    private static ServerPlayer vault_hunters_extra_rules$player = null;
}
