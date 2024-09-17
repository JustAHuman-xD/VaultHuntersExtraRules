//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extrarules.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import iskallia.vault.config.altar.VaultAltarIngredientsConfig;
import iskallia.vault.init.ModGameRules;
import iskallia.vault.world.VaultCrystalMode;
import lv.id.bonne.vaulthunters.extrarules.util.GameRuleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;


/**
 * This mixin allows to change the behavior of the getIngredients method in VaultAltarIngredientsConfig
 * class by allowing to define crystal mode per player.
 */
@Mixin(value = VaultAltarIngredientsConfig.class, remap = false)
public class MixinVaultAltarIngredientsConfig
{
    @Redirect(method = "getIngredients",
        at = @At(value = "INVOKE", target = "Liskallia/vault/world/VaultCrystalMode$GameRuleValue;get()Liskallia/vault/world/VaultCrystalMode;"))
    private VaultCrystalMode getCrystalMode(VaultCrystalMode.GameRuleValue instance,
        ServerPlayer player,
        BlockPos pos)
    {
        return GameRuleHelper.getRule(ModGameRules.CRYSTAL_MODE, player).get();
    }
}
