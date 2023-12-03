//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import iskallia.vault.core.event.common.BlockUseEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.LodestoneObjective;
import iskallia.vault.core.world.storage.VirtualWorld;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.util.GameRuleHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * This mixin allows to inject code into LodestoneObjective to allow multiple clicks on
 * lodestone if game rule is set.
 */
@Mixin(value = LodestoneObjective.class, remap = false)
public class MixinLodestoneObjective
{
    /**
     * Sets the vault_hunters_extra_rules$world variable value.
     * @param vault The vault.
     * @param world The virtual world.
     * @param data The block use event data.
     * @param ci Callback info.
     */
    @Inject(method = "lambda$initServer$3",
        at = @At(value = "HEAD"))
    public void injectVariableAssign(Vault vault, VirtualWorld world, BlockUseEvent.Data data, CallbackInfo ci)
    {
        vault_hunters_extra_rules$player = data.getPlayer();
    }


    /**
     * This method replaced false parameter with game rule value.
     * @param consumed The parameter.
     * @return The game rule value.
     */
    @ModifyArg(method = "lambda$initServer$3",
        at = @At(value = "INVOKE",
            target = "iskallia/vault/block/entity/LodestoneTileEntity.setConsumed(Z)V"),
        index = 0)
    private boolean addReuseLodestone(boolean consumed)
    {
        return vault_hunters_extra_rules$player == null ||
            !GameRuleHelper.
                getRule(VaultHuntersExtraRules.REUSE_PEDESTALS, vault_hunters_extra_rules$player).
                get();
    }


    /**
     * Removes the vault_hunters_extra_rules$world value.
     * @param vault The vault.
     * @param world The virtual world.
     * @param data The block use event data.
     * @param ci Callback info.
     */
    @Inject(method = "lambda$initServer$3",
        at = @At(value = "RETURN"))
    public void injectVariableRemove(Vault vault, VirtualWorld world, BlockUseEvent.Data data, CallbackInfo ci)
    {
        vault_hunters_extra_rules$player = null;
    }

    /**
     * The player variable.
     */
    @Unique
    private static Player vault_hunters_extra_rules$player;
}
