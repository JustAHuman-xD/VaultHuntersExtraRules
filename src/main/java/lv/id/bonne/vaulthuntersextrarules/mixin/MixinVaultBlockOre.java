//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import iskallia.vault.block.VaultOreBlock;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.util.GameRuleHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * This mixin injects into VaultOreBlock#getDrops to increase amount of loot being dropped if copiously is triggered.
 */
@Mixin(VaultOreBlock.class)
public class MixinVaultBlockOre
{
    /**
     * This method redirects to increaseLootQuantity.
     * @param originalInstance The originalInstance.
     * @param dropCopy The collection.
     * @param state The block state.
     * @param builder The loot context builder.
     */
    @Redirect(method = "getDrops",
        at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z"))
    private boolean increaseCopiouslyDrop(List<ItemStack> originalInstance,
        Collection<ItemStack> dropCopy,
        BlockState state,
        LootContext.Builder builder)
    {
        Player player = null;
        if (builder.getOptionalParameter(LootContextParams.THIS_ENTITY) instanceof Player entity) {
            player = entity;
        }


        // Get modifier value
        int modifier = GameRuleHelper.
            getRule(VaultHuntersExtraRules.COPIOUSLY_DROP, builder.getLevel(), player).get().getMultiplier();

        // Get starting list of all items.
        List<ItemStack> originalList = new ArrayList<>(originalInstance);

        for (int i = 0; i < modifier; i++)
        {
            // Create clone of original item list.
            originalInstance.addAll(originalList.stream().map(ItemStack::copy).toList());
        }

        return true;
    }
}
