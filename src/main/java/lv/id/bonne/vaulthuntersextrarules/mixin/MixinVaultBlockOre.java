//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import iskallia.vault.block.VaultOreBlock;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.util.GameRuleHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;


/**
 * This mixin injects into VaultOreBlock#getDrops to increase amount of loot being dropped if copiously is triggered.
 */
@Mixin(VaultOreBlock.class)
public class MixinVaultBlockOre
{
    /**
     * Redirects the amount of loot being dropped.
     * @param builder The loot context builder.
     * @param cir Callback info.
     */
    @Inject(method = "getCopiouslyChance", at = @At("TAIL"), cancellable = true)
    private static void increaseCopiouslyDrop(LootContext.Builder builder, CallbackInfoReturnable<Float> cir)
    {
        Player player = null;

        if (builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player entity)
        {
            player = entity;
        }

        // Get modifier value
        cir.setReturnValue(cir.getReturnValue() *
            GameRuleHelper.getRule(VaultHuntersExtraRules.COPIOUSLY_DROP,
                builder.getLevel(), player).get().getMultiplier());
    }
}
