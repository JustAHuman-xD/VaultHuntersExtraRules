//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extrarules.mixin;


import iskallia.vault.VaultMod;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.core.vault.player.ClassicListenersLogic;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModGameRules;
import lv.id.bonne.vaulthunters.extrarules.util.GameRuleHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;


/**
 * This mixin adds/removes casual difficulty if player ModGameRules.MODE gamerule locally is requesting that.
 */
@Mixin(value = ClassicListenersLogic.class, remap = false)
public class MixinClassicListenerLogic {
    @Inject(method = "onJoin",
            at = @At(value = "INVOKE",
                    target = "Liskallia/vault/core/vault/player/ClassicListenersLogic;set(Liskallia/vault/core/data/key/FieldKey;)Liskallia/vault/core/data/DataObject;"))
    private void addCasualOnJoin(VirtualWorld world, Vault vault, Listener listener, CallbackInfoReturnable<Boolean> cir) {
        vault.ifPresent(Vault.OWNER, uuid -> {
            Player player = world.getServer().getPlayerList().getPlayer(uuid);

            if (player != null) {
                VaultModifier<?> modifier = VaultModifierRegistry.getOrDefault(VaultMod.id("casual"), null);

                if (modifier != null) {
                    switch (GameRuleHelper.getRule(ModGameRules.MODE, player).get()) {
                        case CASUAL -> {
                            vault.ifPresent(Vault.MODIFIERS, modifiers -> {
                                if (modifiers.getModifiers().stream().
                                        noneMatch(m -> m.getId().equals(VaultMod.id("casual")))) {
                                    // If there is no casual then add casual
                                    Modifiers effect = modifiers.addModifier(modifier, 1, false, JavaRandom.ofNanoTime());
                                    modifier.onListenerAdd(world, vault, effect.getContexts(modifier).findAny().orElse(null), listener);
                                }
                            });
                        }
                        case NORMAL, HARDCORE -> {
                            vault.ifPresent(Vault.MODIFIERS, modifiers -> {
                                if (modifiers.getModifiers().stream().
                                        anyMatch(m -> m.getId().equals(VaultMod.id("casual")))) {
                                    // Remove casual from normal and hardcore vaults.
                                    Optional<Modifiers.Entry> any = modifiers.getEntries().stream().
                                            filter(entry -> entry.getModifier().isPresent() &&
                                                    entry.getModifier().get().getId().equals(VaultMod.id("casual"))).
                                            findAny();

                                    if (any.isPresent()) {
                                        modifiers.getEntries().remove(any.get());
                                        modifier.onListenerRemove(world, vault, any.get().getContext(), listener);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }
}
