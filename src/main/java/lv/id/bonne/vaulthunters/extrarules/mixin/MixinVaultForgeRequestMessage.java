//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extrarules.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import iskallia.vault.config.VaultLevelsConfig;
import iskallia.vault.network.message.VaultForgeRequestCraftMessage;
import lv.id.bonne.vaulthunters.extrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthunters.extrarules.util.GameRuleHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


/**
 * This mixin changes how max level is requested. Instead of getting it from the config, it
 * takes value from the game rule.
 */
@Mixin(value = VaultForgeRequestCraftMessage.class, remap = false)
public class MixinVaultForgeRequestMessage {
    @Redirect(method = "lambda$handle$0", at = @At(value = "INVOKE", target = "Liskallia/vault/config/VaultLevelsConfig;getMaxLevel()I"))
    private static int addVaultExpFromGameRule(VaultLevelsConfig instance, @Local(ordinal = 0) ServerPlayer requester) {
        return GameRuleHelper.getRule(VaultHuntersExtraRules.MAX_PLAYER_LEVEL, requester).get();
    }
}
