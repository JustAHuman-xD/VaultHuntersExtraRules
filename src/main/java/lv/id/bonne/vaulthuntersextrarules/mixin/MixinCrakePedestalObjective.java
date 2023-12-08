//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import iskallia.vault.core.event.common.BlockUseEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.CrakePedestalObjective;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.util.GameRuleHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import iskallia.vault.block.CrakePedestalBlock;
import iskallia.vault.core.event.common.BlockUseEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.objective.CrakePedestalObjective;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;



/**
 * This mixin allows to inject code into CrakePedestalObjective to allow multiple clicks on
 * crake if game rule is set.
 */
@Mixin(value = CrakePedestalObjective.class, remap = false)
public class MixinCrakePedestalObjective
{
    /**
     * Replace block with a new crake pedestal. Other methods with assigning parameter value did not work.
     * @param vault Vault.
     * @param data Data.
     * @param cir Callback info.
     */
    @Inject(method = "lambda$initServer$3",
        at = @At(value = "INVOKE",
            target = "Liskallia/vault/core/event/common/BlockUseEvent$Data;setResult(Lnet/minecraft/world/InteractionResult;)V",
            ordinal = 1))
    private void replaceCake(Vault vault,
        BlockUseEvent.Data data,
        CallbackInfo cir)
    {
        if (GameRuleHelper.getRule(VaultHuntersExtraRules.REUSE_PEDESTALS, data.getPlayer()).get())
        {
            data.getWorld().setBlock(data.getPos(), data.getState().setValue(CrakePedestalBlock.CONSUMED, false), 3);
        }
    }
}
