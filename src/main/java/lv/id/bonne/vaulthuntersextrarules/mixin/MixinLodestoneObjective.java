//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import com.llamalad7.mixinextras.sugar.Local;

import iskallia.vault.core.event.common.BlockUseEvent;
import iskallia.vault.core.vault.objective.LodestoneObjective;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.util.GameRuleHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


/**
 * This mixin allows to inject code into LodestoneObjective to allow multiple clicks on
 * lodestone if game rule is set.
 */
@Mixin(value = LodestoneObjective.class, remap = false)
public class MixinLodestoneObjective
{
    /**
     * This method replaced false parameter with game rule value.
     * @param consumed The parameter.
     * @return The game rule value.
     */
    @ModifyArg(method = "lambda$initServer$3",
        at = @At(value = "INVOKE",
            target = "iskallia/vault/block/entity/LodestoneTileEntity.setConsumed(Z)V"),
        index = 0)
    private boolean addReuseLodestone(boolean consumed, @Local(ordinal = 0, argsOnly = true) BlockUseEvent.Data data)
    {
        return !GameRuleHelper.
            getRule(VaultHuntersExtraRules.REUSE_PEDESTALS, data.getPlayer()).
            get();
    }
}
