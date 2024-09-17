//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extrarules.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import iskallia.vault.entity.entity.SpiritEntity;
import iskallia.vault.init.ModGameRules;
import iskallia.vault.world.VaultMode;
import lv.id.bonne.vaulthunters.extrarules.util.GameRuleHelper;
import net.minecraft.server.level.ServerPlayer;


/**
 * This mixin adjusts if player needs to spawn spirit on death or not.
 */
@Mixin(SpiritEntity.class)
public class MixinSpiritEntity
{
    /**
     * This method changes gamerule return value based on player vault mode.
     * @param instance The original gamerule instance.
     * @return The adjusted gamemode value.
     */
    @Redirect(method = "lambda$onPlayerDeath$4",
        at = @At(value = "INVOKE", target = "Liskallia/vault/world/VaultMode$GameRuleValue;get()Liskallia/vault/world/VaultMode;"),
        remap = false)
    private static VaultMode onPlayerDeath(VaultMode.GameRuleValue instance,
        @Local(ordinal = 0, argsOnly = true) ServerPlayer player)
    {
        return GameRuleHelper.getRule(ModGameRules.MODE, player).get();
    }
}
