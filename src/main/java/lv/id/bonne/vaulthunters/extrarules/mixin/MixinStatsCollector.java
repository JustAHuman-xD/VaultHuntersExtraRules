//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthunters.extrarules.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.core.data.DataObject;
import iskallia.vault.core.data.key.FieldKey;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.stat.StatsCollector;
import lv.id.bonne.vaulthunters.extrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthunters.extrarules.util.GameRuleHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;
import java.util.function.UnaryOperator;


/**
 * This mixin injects into StatsCollector to fix bonus multiplier and use it as separate
 * game rule.
 */
@Mixin(value = StatsCollector.class, remap = false)
public class MixinStatsCollector {

    @Redirect(method = "lambda$initServer$5", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/stat/StatCollector;modify(Liskallia/vault/core/data/key/FieldKey;Ljava/util/function/UnaryOperator;)Liskallia/vault/core/data/DataObject;", ordinal = 0))
    private static <T, D extends DataObject<D>> D fixBonusMultiplier(StatCollector instance, FieldKey<T> fieldKey, UnaryOperator<T> unaryOperator, @Local(argsOnly = true) UUID _uuid) {
        ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(_uuid);
        if (player == null) {
            VaultHuntersExtraRules.LOGGER.warn("Player with UUID {} not found while trying to modify bonus exp.", _uuid);
        } else if (fieldKey != StatCollector.BONUS_EXP_MULTIPLIER) {
            VaultHuntersExtraRules.LOGGER.warn("Unexpected field key {} while trying to modify bonus exp for player {}.", fieldKey, player.getGameProfile().getName());
        } else {
            return (D) instance.modify(StatCollector.BONUS_EXP_MULTIPLIER, m -> m * GameRuleHelper.getRule(VaultHuntersExtraRules.BONUS_XP, player).get().getMultiplier());
        }
        return (D) instance.modify(fieldKey, unaryOperator);
    }

    @Redirect(method = "lambda$initServer$5", at = @At(value = "INVOKE", target = "Liskallia/vault/core/vault/stat/StatCollector;modify(Liskallia/vault/core/data/key/FieldKey;Ljava/util/function/UnaryOperator;)Liskallia/vault/core/data/DataObject;", ordinal = 0))
    private static <T, D extends DataObject<D>> D fixObjectiveMultiplier(StatCollector instance, FieldKey<T> fieldKey, UnaryOperator<T> unaryOperator, @Local(argsOnly = true) UUID _uuid) {
        ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(_uuid);
        if (player == null) {
            VaultHuntersExtraRules.LOGGER.warn("Player with UUID {} not found while trying to modify objective exp.", _uuid);
        } else if (fieldKey != StatCollector.OBJECTIVE_EXP_MULTIPLIER) {
            VaultHuntersExtraRules.LOGGER.warn("Unexpected field key {} while trying to objective exp for player {}.", fieldKey, player.getGameProfile().getName());
        } else {
            return (D) instance.modify(StatCollector.OBJECTIVE_EXP_MULTIPLIER, m -> m * GameRuleHelper.getRule(VaultHuntersExtraRules.COMPLETION_XP, player).get().getMultiplier());
        }
        return (D) instance.modify(fieldKey, unaryOperator);
    }

}
