//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import iskallia.vault.block.entity.CoinPilesTileEntity;
import iskallia.vault.core.data.key.LootTableKey;
import iskallia.vault.core.event.common.CoinStacksGenerationEvent;
import iskallia.vault.core.world.loot.generator.LootTableGenerator;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.util.GameRuleHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;


/**
 * This mixin injects into CoinPilesTileEntity#generateLoot to increase amount of loot being dropped when coin pile is broken.
 */
@Mixin(CoinPilesTileEntity.class)
public class MixinCoinPilesTileEntity
{
    @Inject(method = "generateLoot",
        at = @At(value = "INVOKE", target = "Liskallia/vault/core/world/loot/generator/LootTableGenerator;generate(Liskallia/vault/core/random/RandomSource;)V"),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        remap = false)
    private void increaseLootQuantity(ServerPlayer player,
        CallbackInfoReturnable<List> cir,
        List loot,
        CoinStacksGenerationEvent.Data data,
        LootTableKey key,
        LootTableGenerator generator)
    {
        generator.itemQuantity = GameRuleHelper.getRule(VaultHuntersExtraRules.COIN_LOOT, player).get().getMultiplier() - 1;
    }
}
