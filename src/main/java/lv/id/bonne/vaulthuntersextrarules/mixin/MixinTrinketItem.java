//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthuntersextrarules.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.stat.StatCollector;
import iskallia.vault.core.vault.time.TickTimer;
import iskallia.vault.core.vault.time.modifier.TrinketExtension;
import iskallia.vault.gear.trinket.effects.VaultExperienceTrinket;
import iskallia.vault.gear.trinket.effects.VaultTimeExtensionTrinket;
import iskallia.vault.item.gear.TrinketItem;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.expertise.type.TrinketerExpertise;
import iskallia.vault.world.data.PlayerExpertisesData;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.util.GameRuleHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;


/**
 * This mixin injects into TrinketItem class to allow its swapping in first room.
 */
@Mixin(value = TrinketItem.class, remap = false)
public class MixinTrinketItem
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

            if (GameRuleHelper.getRule(VaultHuntersExtraRules.ALLOW_TRINKET_SWAP, level, owner).get())
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
     * This method injects when player equip trinket. If we detect that vault is in pause state,
     * this method adds current vault to the used vaults.
     * @param slotContext The use context
     * @param prevStack The previous item stack
     * @param stack The new item stack
     * @param ci Callback Info object.
     */
    @Inject(method = "onEquip", at = @At(value = "HEAD"))
    public void injectAddingVaultId(SlotContext slotContext, ItemStack prevStack, ItemStack stack, CallbackInfo ci)
    {
        if (TrinketItem.isIdentified(stack))
        {
            if (slotContext.entity() instanceof Player player)
            {
                if (!GameRuleHelper.getRule(VaultHuntersExtraRules.ALLOW_TRINKET_SWAP, player).get())
                {
                    return;
                }

                ServerVaults.get(player.level).ifPresent(vault -> {
                    if (vault.has(Vault.CLOCK) && vault.get(Vault.CLOCK).has(TickTimer.PAUSED))
                    {
                        if (!TrinketItem.isUsableInVault(stack, vault.get(Vault.ID)))
                        {
                            double damageAvoidanceChance = PlayerExpertisesData.
                                get((ServerLevel) player.level).
                                getExpertises(player).
                                getAll(TrinketerExpertise.class, Skill::isUnlocked).
                                stream().
                                mapToDouble(TrinketerExpertise::getDamageAvoidanceChance).
                                sum();

                            if (player.level.random.nextDouble() < damageAvoidanceChance)
                            {
                                TrinketItem.addFreeUsedVault(stack, vault.get(Vault.ID));
                            }
                            else
                            {
                                TrinketItem.addUsedVault(stack, vault.get(Vault.ID));
                            }
                        }

                        TrinketItem.getTrinket(stack).ifPresent(effect ->
                        {
                            if (effect.isUsable(stack, player))
                            {
                                if (effect instanceof VaultTimeExtensionTrinket trinket)
                                {
                                    // Add time to the vault
                                    vault.get(Vault.CLOCK).addModifier(new TrinketExtension(player,
                                        trinket.getConfig().getTimeAdded()));
                                }
                                else if (effect instanceof VaultExperienceTrinket trinket)
                                {
                                    // Add stats values
                                    vault.getOptional(Vault.STATS).
                                        map(stats -> stats.get(player.getUUID())).
                                        ifPresent(stats ->
                                        {
                                            float multiplier = 1.0F + trinket.getConfig().getExperienceIncrease();

                                            // Multiply XP by multiplier
                                            stats.modify(StatCollector.OBJECTIVE_EXP_MULTIPLIER, value -> value * multiplier);
                                            stats.modify(StatCollector.BONUS_EXP_MULTIPLIER, value -> value * multiplier);
                                        });
                                }
                            }
                        });
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

            if (GameRuleHelper.getRule(VaultHuntersExtraRules.ALLOW_TRINKET_SWAP, level, owner).get())
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
     * This method injects when player unequip trinket. If we detect that vault is in pause state,
     * this method fixes some modifiers that vault has added.
     * @param slotContext The use context
     * @param prevStack The previous item stack
     * @param stack The new item stack
     * @param ci Callback Info object.
     */
    @Inject(method = "onUnequip", at = @At(value = "HEAD"))
    public void injectUnequipTasks(SlotContext slotContext, ItemStack prevStack, ItemStack stack, CallbackInfo ci)
    {
        if (TrinketItem.isIdentified(stack))
        {
            if (slotContext.entity() instanceof Player player)
            {
                if (!GameRuleHelper.getRule(VaultHuntersExtraRules.ALLOW_TRINKET_SWAP, player).get())
                {
                    return;
                }

                ServerVaults.get(player.level).ifPresent(vault -> {
                    if (vault.has(Vault.CLOCK) && vault.get(Vault.CLOCK).has(TickTimer.PAUSED))
                    {
                        TrinketItem.getTrinket(stack).ifPresent(effect ->
                        {
                            if (effect instanceof VaultTimeExtensionTrinket timeTrinket)
                            {
                                // Remove time to the vault
                                vault.get(Vault.CLOCK).addModifier(new TrinketExtension(player,
                                    -timeTrinket.getConfig().getTimeAdded()));
                            }
                            else if (effect instanceof VaultExperienceTrinket trinket)
                            {
                                // Remove stats values
                                vault.getOptional(Vault.STATS).
                                    map(stats -> stats.get(player.getUUID())).
                                    ifPresent(stats ->
                                    {
                                        float multiplier = 1.0F + trinket.getConfig().getExperienceIncrease();

                                        // Divide XP by multiplier
                                        stats.modify(StatCollector.OBJECTIVE_EXP_MULTIPLIER, value -> value / multiplier);
                                        stats.modify(StatCollector.BONUS_EXP_MULTIPLIER, value -> value / multiplier);
                                    });
                            }
                        });
                    }
                });
            }
        }
    }
}
