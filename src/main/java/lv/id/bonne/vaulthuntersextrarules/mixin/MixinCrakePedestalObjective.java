//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import iskallia.vault.core.event.common.BlockUseEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.CrakePedestalObjective;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import net.minecraft.world.level.Level;


/**
 * This mixin allows to inject code into CrakePedestalObjective to allow multiple clicks on
 * crake if game rule is set.
 */
@Mixin(value = CrakePedestalObjective.class, remap = false)
public class MixinCrakePedestalObjective
{
    /**
     * Sets the vault_hunters_extra_rules$world for variable.
     * @param vault The vault.
     * @param data The block use event data.
     * @param ci Callback info.
     */
    @Inject(method = "lambda$initServer$3",
        at = @At(value = "HEAD"))
    public void injectVariableAssign(Vault vault, BlockUseEvent.Data data, CallbackInfo ci)
    {
        vault_hunters_extra_rules$world = data.getWorld();
    }


    /**
     * This method replaced false parameter with game rule value.
     * @param par2 The parameter.
     * @return The game rule value.
     */
    @ModifyArg(method = "lambda$initServer$3",
        at = @At(value = "INVOKE",
            target = "net/minecraft/world/level/block/state/BlockState.setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"),
        index = 1)
    private Comparable<?> addReusePedestal(Comparable<?> par2)
    {
        return vault_hunters_extra_rules$world == null ||
            !vault_hunters_extra_rules$world.getGameRules().
                getRule(VaultHuntersExtraRules.REUSE_PEDESTALS).
                get();
    }


    /**
     * Removes the vault_hunters_extra_rules$world value.
     * @param vault The vault.
     * @param data The block use event data.
     * @param ci Callback info.
     */
    @Inject(method = "lambda$initServer$3",
        at = @At(value = "RETURN"))
    public void injectVariableRemove(Vault vault, BlockUseEvent.Data data, CallbackInfo ci)
    {
        vault_hunters_extra_rules$world = null;
    }


    /**
     * The world variable.
     */
    @Unique
    private static Level vault_hunters_extra_rules$world;
}
