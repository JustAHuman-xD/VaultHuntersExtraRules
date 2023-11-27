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

import iskallia.vault.block.base.GodAltarTileEntity;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;


/**
 * This mixin allows to inject code into GodAltarTileEntity to allow finishing challenge without
 * returning on harder difficulties.
 */
@Mixin(value = GodAltarTileEntity.class, remap = false)
public class MixinGodAltarTileEntity
{
    /**
     * Sets the vault_hunters_extra_rules$world value.
     * @param world The world.
     * @param player The player.
     * @param ci Callback info.
     */
    @Inject(method = "onClick",
        at = @At(value = "HEAD"))
    public void injectVariableAssign(ServerLevel world, ServerPlayer player, CallbackInfo ci)
    {
        vault_hunters_extra_rules$world = world;
    }


    /**
     * Replaces the requirement on hard/impossible/fragged returning to god altar with gamerule value.
     * @param requiresDraining The parameter.
     * @return The game rule value.
     */
    @ModifyArg(method = "onClick",
        at = @At(value = "INVOKE",
            target = "Liskallia/vault/task/CompleteGodAltarTask;<init>(Ljava/util/UUID;Lnet/minecraft/resources/ResourceLocation;Z)V"),
        index = 2)
    private boolean skipAltarReturning(boolean requiresDraining)
    {
        return requiresDraining &&
            (vault_hunters_extra_rules$world == null ||
                !vault_hunters_extra_rules$world.getGameRules().
                    getRule(VaultHuntersExtraRules.SKIP_ALTAR_RETURNING).
                    get());
    }


    /**
     * Removes the vault_hunters_extra_rules$world value.
     * @param world The world.
     * @param player The player.
     * @param ci Callback info.
     */
    @Inject(method = "onClick",
        at = @At(value = "RETURN"))
    public void injectVariableRemove(ServerLevel world, ServerPlayer player, CallbackInfo ci)
    {
        vault_hunters_extra_rules$world = null;
    }


    /**
     * The world variable.
     */
    @Unique
    private static Level vault_hunters_extra_rules$world;
}
