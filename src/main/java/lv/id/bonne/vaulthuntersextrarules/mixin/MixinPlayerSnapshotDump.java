//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import iskallia.vault.config.VaultLevelsConfig;
import iskallia.vault.dump.PlayerSnapshotDump;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.util.GameRuleHelper;
import net.minecraft.server.level.ServerPlayer;


/**
 * This mixin changes how max level is requested. Instead of getting it from the config, it
 * takes value from the game rule.
 */
@Mixin(value = PlayerSnapshotDump.class, remap = false)
public class MixinPlayerSnapshotDump
{
    @Redirect(method = "createSnapshot",
        at = @At(value = "INVOKE", target = "Liskallia/vault/config/VaultLevelsConfig;getMaxLevel()I"))
    private static int addVaultExpFromGameRule(VaultLevelsConfig instance,
        @Local(ordinal = 0, argsOnly = true) ServerPlayer requester)
    {
        return GameRuleHelper.getRule(VaultHuntersExtraRules.MAX_PLAYER_LEVEL, requester).get();
    }
}
