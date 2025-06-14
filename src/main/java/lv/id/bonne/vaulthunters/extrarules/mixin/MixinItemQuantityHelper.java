//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extrarules.mixin;


import iskallia.vault.init.ModGameRules;
import iskallia.vault.util.calc.ItemQuantityHelper;
import iskallia.vault.world.VaultLoot;
import lv.id.bonne.vaulthunters.extrarules.util.GameRuleHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


/**
 * This mixin allows to change the behavior of the getItemQuantity method in ItemQuantityHelper
 * class by allowing to define loot mode per vault.
 */
@Mixin(value = ItemQuantityHelper.class, remap = false)
public class MixinItemQuantityHelper {
    @Redirect(method = "getItemQuantity",
            at = @At(value = "INVOKE", target = "Liskallia/vault/world/VaultLoot$GameRuleValue;get()Liskallia/vault/world/VaultLoot;"))
    private static VaultLoot getItemQuantity(VaultLoot.GameRuleValue instance, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            return GameRuleHelper.getRule(ModGameRules.LOOT, player).get();
        } else {
            return instance.get();
        }
    }
}
