//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extrarules.mixin;

import iskallia.vault.config.VaultLevelsConfig;
import iskallia.vault.skill.PlayerVaultStats;
import lv.id.bonne.vaulthunters.extrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthunters.extrarules.util.GameRuleHelper;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * This mixin changes how max level is requested. Instead of getting it from the config, it
 * takes value from the game rule.
 */
@Mixin(value = PlayerVaultStats.class, remap = false)
public class MixinPlayerVaultStats {
    @Shadow @Final private UUID uuid;

    @Redirect(method = "addVaultExp",
            at = @At(value = "INVOKE", target = "Liskallia/vault/config/VaultLevelsConfig;getMaxLevel()I"))
    private int getExpNeededToNextLevelFromGameRule(VaultLevelsConfig instance) {
        GameRules.IntegerValue rule =
                GameRuleHelper.getRule(VaultHuntersExtraRules.MAX_PLAYER_LEVEL, uuid);

        return rule != null ? rule.get() : instance.getMaxLevel();
    }
}
