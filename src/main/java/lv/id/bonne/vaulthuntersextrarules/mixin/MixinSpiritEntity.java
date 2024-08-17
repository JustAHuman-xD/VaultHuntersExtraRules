//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import iskallia.vault.entity.entity.SpiritEntity;
import iskallia.vault.init.ModGameRules;
import iskallia.vault.world.VaultMode;
import lv.id.bonne.vaulthuntersextrarules.util.GameRuleHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;


/**
 * This mixin adjusts if player needs to spawn spirit on death or not.
 */
@Mixin(SpiritEntity.class)
public class MixinSpiritEntity
{
    /**
     * Sets the vault_hunters_extra_rules$player variable value.
     * @param event The event for player death.
     * @param ci Callback info.
     */
    @Inject(method = "onPlayerDeath", at = @At(value = "HEAD"), remap = false)
    private static void injectVariableAssign(LivingDeathEvent event, CallbackInfo ci)
    {
        if (event.getEntity() instanceof ServerPlayer player)
        {
            vault_hunters_extra_rules$player = player;
        }
    }


    /**
     * This method changes gamerule return value based on player vault mode.
     * @param instance The original gamerule instance.
     * @return The adjusted gamemode value.
     */
    @Redirect(method = "lambda$onPlayerDeath$4",
        at = @At(value = "INVOKE", target = "Liskallia/vault/world/VaultMode$GameRuleValue;get()Liskallia/vault/world/VaultMode;"),
        remap = false)
    private static VaultMode onPlayerDeath(VaultMode.GameRuleValue instance)
    {
        return GameRuleHelper.getRule(ModGameRules.MODE, vault_hunters_extra_rules$player).get();
    }


    /**
     * Removes the vault_hunters_extra_rules$player value.
     * @param event The event for player death.
     * @param ci Callback info.
     */
    @Inject(method = "onPlayerDeath", at = @At(value = "RETURN"), remap = false)
    private static void injectVariableRemove(LivingDeathEvent event, CallbackInfo ci)
    {
        vault_hunters_extra_rules$player = null;
    }

    /**
     * The player variable.
     */
    @Unique
    private static Player vault_hunters_extra_rules$player;
}
