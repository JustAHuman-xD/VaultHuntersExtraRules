//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extrarules.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.entity.entity.SpiritEntity;
import iskallia.vault.init.ModGameRules;
import iskallia.vault.world.VaultMode;
import lv.id.bonne.vaulthunters.extrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthunters.extrarules.util.GameRuleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;


/**
 * This mixin adjusts if player needs to spawn spirit on death or not.
 */
@Mixin(SpiritEntity.class)
public class MixinSpiritEntity
{
    /**
     * This method changes gamerule return value based on player vault mode.
     * @param instance The original gamerule instance.
     * @return The adjusted gamemode value.
     */
    @Redirect(method = "lambda$onPlayerDeath$4",
        at = @At(value = "INVOKE", target = "Liskallia/vault/world/VaultMode$GameRuleValue;get()Liskallia/vault/world/VaultMode;"),
        remap = false)
    private static VaultMode onPlayerDeath(VaultMode.GameRuleValue instance,
        @Local(ordinal = 0, argsOnly = true) ServerPlayer player)
    {
        return GameRuleHelper.getRule(ModGameRules.MODE, player).get();
    }


    /**
     * This method changes spirit spawn position based on gamerule value.
     * @param respawnPos original spawn position.
     * @param vault The vault instance.
     * @param player The player instance.
     * @return new spirit spawn position.
     */
    @ModifyArg(method = "initSpiritData",
        at = @At(value = "INVOKE",
            target = "Liskallia/vault/world/data/PlayerSpiritRecoveryData$SpiritData;<init>(Ljava/util/UUID;Ljava/util/UUID;Liskallia/vault/world/data/InventorySnapshot;IILnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;Lcom/mojang/authlib/GameProfile;)V"),
        index = 6,
        remap = false)
    private static BlockPos changeLocation(BlockPos respawnPos,
        @Local(argsOnly = true) Vault vault,
        @Local(argsOnly = true) ServerPlayer player)
    {
        return switch (GameRuleHelper.getRule(VaultHuntersExtraRules.SPIRIT_SPAWN_LOCATION, player).get())
        {
            case DEFAULT -> respawnPos;
            case PORTAL ->
            {
                if (player.getRespawnPosition() != null)
                {
                    yield respawnPos;
                }

                yield MixinSpiritEntity.getVaultPortal(vault, player, respawnPos);
            }
            case ALWAYS_PORTAL -> MixinSpiritEntity.getVaultPortal(vault, player, respawnPos);
            case WORLD_SPAWN ->
            {
                if (player.getRespawnPosition() != null)
                {
                    yield respawnPos;
                }

                yield MixinSpiritEntity.getWorldSpawn(player, respawnPos);
            }
            case ALWAYS_WORLD_SPAWN -> MixinSpiritEntity.getWorldSpawn(player, respawnPos);
        };
    }


    /**
     * This method gets world spawn position.
     * @param player The player who's spirit is spawned.
     * @param backup Backup location if world does not have spawn point.
     * @return World Shared Spawn Point or backup.
     */
    @Unique
    private static BlockPos getWorldSpawn(ServerPlayer player, BlockPos backup)
    {
        MinecraftServer server = player.getServer();

        if (server == null)
        {
            // If server is null, return backup position.
            return backup;
        }

        ServerLevel level = server.getLevel(player.getRespawnDimension());

        if (level == null)
        {
            // If level is null, return backup position.
            return backup;
        }

        return level.getSharedSpawnPos();
    }


    /**
     * This method gets portal position.
     * @param vault The vault which player was running.
     * @param player The player who's spirit is spawned.
     * @param backup Backup location if world does not have spawn point.
     * @return Portal location or backup.
     */
    @Unique
    private static BlockPos getVaultPortal(Vault vault, ServerPlayer player, BlockPos backup)
    {
        Listener listener = vault.get(Vault.LISTENERS).get(player.getUUID());

        if (listener == null)
        {
            return backup;
        }

        return listener.get(Listener.JOIN_STATE).getBlockPos();
    }
}
