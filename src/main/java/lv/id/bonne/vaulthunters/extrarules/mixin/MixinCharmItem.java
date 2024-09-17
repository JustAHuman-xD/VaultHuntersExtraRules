//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extrarules.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Optional;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.time.TickTimer;
import iskallia.vault.item.gear.CharmItem;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthunters.extrarules.util.GameRuleHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

/**
 * This mixin injects into CharmItem class to allow its swapping in first room.
 */
@Mixin(value = CharmItem.class, remap = false)
public class MixinCharmItem
{
    /**
     * Redirects a ServerVaults#get(Level) method results to return it empty if vault clock has not started yet.
     * The redirect is necessary, so it would avoid getting into exit when vault is found.
     * @param level The server level that need to be checked.
     * @return Optional of Vault for given Level, or empty optional
     */
    @Redirect(method = "canEquip",
        at = @At(value = "INVOKE", target = "Liskallia/vault/world/data/ServerVaults;get(Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"))
    public Optional<Vault> redirectCanEquip(Level level)
    {
        Optional<Vault> optionalVault = ServerVaults.get(level);

        if (optionalVault.isPresent() &&
            optionalVault.get().has(Vault.CLOCK) &&
            optionalVault.get().get(Vault.CLOCK).has(TickTimer.PAUSED))
        {
            Player owner = level.getPlayerByUUID(optionalVault.get().get(Vault.OWNER));

            if (GameRuleHelper.getRule(VaultHuntersExtraRules.ALLOW_CHARM_SWAP, level, owner).get())
            {
                return Optional.empty();
            }
            else
            {
                return optionalVault;
            }
        }
        else
        {
            return optionalVault;
        }
    }


    /**
     * This method injects when player equip charm. If we detect that vault is in pause state,
     * this method adds current vault to the used vaults.
     * @param slotContext The use context
     * @param prevStack The previous item stack
     * @param stack The new item stack
     * @param ci Callback Info object.
     */
    @Inject(method = "onEquip", at = @At(value = "HEAD"))
    public void injectAddingVaultId(SlotContext slotContext, ItemStack prevStack, ItemStack stack, CallbackInfo ci)
    {
        if (CharmItem.isIdentified(stack))
        {
            if (slotContext.entity() instanceof Player player)
            {
                if (!GameRuleHelper.getRule(VaultHuntersExtraRules.ALLOW_CHARM_SWAP, player).get())
                {
                    return;
                }

                ServerVaults.get(player.level).ifPresent(vault -> {
                    if (vault.has(Vault.CLOCK) && vault.get(Vault.CLOCK).has(TickTimer.PAUSED))
                    {
                        if (!CharmItem.isUsableInVault(stack, vault.get(Vault.ID)))
                        {
                            CharmItem.addUsedVault(stack, vault.get(Vault.ID));
                        }
                    }
                });
            }
        }
    }


    /**
     * Redirects a ServerVaults#get(Level) method results to return it empty if vault clock has not started yet.
     * The redirect is necessary, so it would avoid getting into exit when vault is found.
     * @param level The server level that need to be checked.
     * @return Optional of Vault for given Level, or empty optional
     */
    @Redirect(method = "canUnequip",
        at = @At(value = "INVOKE", target = "Liskallia/vault/world/data/ServerVaults;get(Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"))
    public Optional<Vault> redirectCanUnequip(Level level)
    {
        Optional<Vault> optionalVault = ServerVaults.get(level);

        if (optionalVault.isPresent() &&
            optionalVault.get().has(Vault.CLOCK) &&
            optionalVault.get().get(Vault.CLOCK).has(TickTimer.PAUSED))
        {
            Player owner = level.getPlayerByUUID(optionalVault.get().get(Vault.OWNER));

            if (GameRuleHelper.getRule(VaultHuntersExtraRules.ALLOW_CHARM_SWAP, level, owner).get())
            {
                return Optional.empty();
            }
            else
            {
                return optionalVault;
            }
        }
        else
        {
            return optionalVault;
        }
    }
}
