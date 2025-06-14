//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthunters.extrarules.mixin;


import iskallia.vault.block.CrakePedestalBlock;
import iskallia.vault.core.event.common.BlockUseEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.CrakePedestalObjective;
import iskallia.vault.core.world.storage.VirtualWorld;
import lv.id.bonne.vaulthunters.extrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthunters.extrarules.util.GameRuleHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * This mixin allows to inject code into CrakePedestalObjective to allow multiple clicks on
 * crake if game rule is set.
 */
@Mixin(value = CrakePedestalObjective.class, remap = false)
public class MixinCrakePedestalObjective {
    @Inject(method = "lambda$initServer$2", at = @At(value = "INVOKE", target = "Liskallia/vault/core/event/common/BlockUseEvent$Data;setResult(Lnet/minecraft/world/InteractionResult;)V", ordinal = 1))
    private void replaceCake(Vault vault, VirtualWorld world, BlockUseEvent.Data data, CallbackInfo ci) {
        if (GameRuleHelper.getRule(VaultHuntersExtraRules.REUSE_PEDESTALS, data.getPlayer()).get()) {
            data.getWorld().setBlock(data.getPos(), data.getState().setValue(CrakePedestalBlock.CONSUMED, false), 3);
        }
    }
}
