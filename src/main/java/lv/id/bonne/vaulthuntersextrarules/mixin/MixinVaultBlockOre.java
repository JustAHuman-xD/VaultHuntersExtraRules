//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.util.ArrayList;
import java.util.List;

import iskallia.vault.block.VaultOreBlock;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;


/**
 * This mixin injects into VaultOreBlock#getDrops to increase amount of loot being dropped if copiously is triggered.
 */
@Mixin(VaultOreBlock.class)
public class MixinVaultBlockOre
{
    @Inject(method = "getDrops",
        at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        remap = false)
    private void increaseLootQuantity(BlockState state,
        LootContext.Builder builder,
        CallbackInfoReturnable<List<ItemStack>> cir,
        List<ItemStack> drops,
        ItemStack stack,
        float chance,
        Entity player,
        BlockPos pos)
    {
        int modifier = player.getLevel().getGameRules().
            getRule(VaultHuntersExtraRules.COPIOUSLY_LOOT).get().getMultiplier();

        // Do something only if there is a point to do something.
        if (modifier > 1)
        {
            // get original drop
            List<ItemStack> originalDrops = new ArrayList<>(drops.size() / 2);

            for (int i = 0; i < drops.size() / 2; i++)
            {
                originalDrops.add(drops.get(i));
            }

            // Add all missing drops to the list.
            for (int i = 1; i < modifier; i++)
            {
                drops.addAll(originalDrops);
            }
        }
    }
}
