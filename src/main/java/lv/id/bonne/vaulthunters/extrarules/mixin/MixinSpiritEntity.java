//
// Created by BONNe
// Copyright - 2024
//


package lv.id.bonne.vaulthunters.extrarules.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import iskallia.vault.core.vault.EntityState;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.player.Listener;
import iskallia.vault.entity.entity.SpiritEntity;
import iskallia.vault.init.ModGameRules;
import iskallia.vault.util.SidedHelper;
import iskallia.vault.world.VaultMode;
import iskallia.vault.world.data.InventorySnapshot;
import iskallia.vault.world.data.PlayerSpiritRecoveryData;
import lv.id.bonne.vaulthunters.extrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthunters.extrarules.gamerule.SpawnPointRule;
import lv.id.bonne.vaulthunters.extrarules.util.GameRuleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;


/**
 * This mixin adjusts if player needs to spawn spirit on death or not.
 */
@Mixin(SpiritEntity.class)
public abstract class MixinSpiritEntity
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


    @Redirect(method = "lambda$onPlayerDeath$2",
        at = @At(value = "INVOKE",
            target = "Liskallia/vault/entity/entity/SpiritEntity;initSpiritData(Liskallia/vault/core/vault/Vault;Lnet/minecraft/server/level/ServerPlayer;Liskallia/vault/world/data/InventorySnapshot;I)Liskallia/vault/world/data/PlayerSpiritRecoveryData$SpiritData;"),
        remap = false)
    private static PlayerSpiritRecoveryData.SpiritData improveSpiritPositionFirst(Vault vault,
        ServerPlayer player,
        InventorySnapshot invSnapshot,
        int vaultLevel,
        @Local(argsOnly = true) EntityState joinState)
    {
        VaultHuntersExtraRules.LOGGER.debug("Spirit: Multiplayer trigger");
        return rules$betterInitSpiritData(vault, player, invSnapshot, vaultLevel, joinState);
    }


    @Redirect(method = "lambda$onPlayerDeath$4",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;tell(Ljava/lang/Runnable;)V",
            ordinal = 1))
    private static void redirectSpiritDataCreation(MinecraftServer server,
        Runnable runnable,
        @Local(argsOnly = true) ServerPlayer player,
        @Local(argsOnly = true) Vault vault,
        @Local int vaultLevel,
        @Local PlayerSpiritRecoveryData data,
        @Local InventorySnapshot invSnapshot)
    {
        VaultHuntersExtraRules.LOGGER.debug("Spirit: Last player trigger");

        EntityState joinState = vault.get(Vault.LISTENERS).get(player.getUUID()).get(Listener.JOIN_STATE);

        server.tell(new TickTask(server.getTickCount() + 10,
            () -> data.putVaultSpiritData(rules$betterInitSpiritData(vault, player, invSnapshot, vaultLevel, joinState))));
    }


    @Unique
    private static PlayerSpiritRecoveryData.SpiritData rules$betterInitSpiritData(Vault vault,
        ServerPlayer player,
        InventorySnapshot invSnapshot,
        int vaultLevel,
        EntityState joinState)
    {
        SpawnPointRule respawnRule = GameRuleHelper.getRule(VaultHuntersExtraRules.SPIRIT_SPAWN_LOCATION, player).get();

        VaultHuntersExtraRules.LOGGER.debug("Spirit: Respawn Rule: " + respawnRule.name());

        GlobalPos respawnPosition = switch (respawnRule)
        {
            case ALWAYS_PORTAL -> MixinSpiritEntity.rules$getPortalPosition(joinState);
            case ALWAYS_WORLD_SPAWN -> MixinSpiritEntity.rules$getWorldSpawnPosition(player);
            default -> MixinSpiritEntity.rules$getPlayerRespawnPosition(player);
        };

         VaultHuntersExtraRules.LOGGER.debug("Spirit: Initial Position: " + respawnPosition);

        if (respawnPosition == null)
        {
            respawnPosition = switch (respawnRule)
            {
                case PORTAL -> MixinSpiritEntity.rules$getPortalPosition(joinState);
                case WORLD_SPAWN -> MixinSpiritEntity.rules$getWorldSpawnPosition(player);
                default -> MixinSpiritEntity.rules$getPlayerRespawnLogicPosition(player);
            };

             VaultHuntersExtraRules.LOGGER.debug("Spirit: Adjusted Position: " + respawnPosition);
        }

        if (respawnPosition == null)
        {
            // This will crash if something is wrong.
            respawnPosition = GlobalPos.of(player.getRespawnDimension(),
                player.getServer().getLevel(player.getRespawnDimension()).getSharedSpawnPos());

             VaultHuntersExtraRules.LOGGER.debug("Spirit: End Position: " + respawnPosition);
        }

        return new PlayerSpiritRecoveryData.SpiritData(vault.get(Vault.ID),
            player.getUUID(),
            invSnapshot,
            vaultLevel,
            SidedHelper.getVaultLevel(player),
            respawnPosition.dimension(),
            respawnPosition.pos(),
            player.getGameProfile());
    }


    @Unique
    private static GlobalPos rules$getPlayerRespawnPosition(ServerPlayer player)
    {
         VaultHuntersExtraRules.LOGGER.debug("Spirit: Check Player Respawn Test");

        BlockPos respawnPosition = player.getRespawnPosition();

        if (respawnPosition == null)
        {
            // Respawn position does not exist.
            return null;
        }

        MinecraftServer server = player.getServer();

        if (server == null)
        {
            // Should not happen
            return null;
        }

        ServerLevel level = server.getLevel(player.getRespawnDimension());

        if (level == null)
        {
            // Should not happen
            return null;
        }

        if (!(level.getBlockState(respawnPosition).is(BlockTags.BEDS)))
        {
            // Location is not bed. So return null.
            return null;
        }

        return GlobalPos.of(player.getRespawnDimension(), respawnPosition);
    }


    @Unique
    private static GlobalPos rules$getPortalPosition(EntityState joinState)
    {
         VaultHuntersExtraRules.LOGGER.debug("Spirit: Check Portal Position Test");

        if (joinState == null)
        {
            return null;
        }

        return GlobalPos.of(joinState.get(EntityState.WORLD), joinState.getBlockPos());
    }


    @Unique
    private static GlobalPos rules$getWorldSpawnPosition(ServerPlayer player)
    {
         VaultHuntersExtraRules.LOGGER.debug("Spirit: Check World Spawn Test");

        MinecraftServer server = player.getServer();

        if (server == null)
        {
            // If server is null.
            return null;
        }

        ServerLevel level = server.getLevel(player.getRespawnDimension());

        if (level == null)
        {
            // If level is null, return backup position.
            return null;
        }

        return GlobalPos.of(player.getRespawnDimension(),
            level.getSharedSpawnPos().offset(0.5, 0, 0.5));
    }


    @Unique
    private static GlobalPos rules$getPlayerRespawnLogicPosition(ServerPlayer player)
    {
         VaultHuntersExtraRules.LOGGER.debug("Spirit: Check Respawn Logic Test");

        MinecraftServer server = player.getServer();

        if (server == null)
        {
            // If server is null.
            return null;
        }

        ServerLevel level = server.getLevel(player.getRespawnDimension());

        if (level == null)
        {
            // If level is null, return backup position.
            return null;
        }

        BlockPos respawnPosition = level.getSharedSpawnPos();

        BlockPos overworldRespawnPos = PlayerRespawnLogic.getOverworldRespawnPos(level,
            respawnPosition.getX(),
            respawnPosition.getZ());

        if (overworldRespawnPos == null)
        {
            // Failed to get position from respawn logic.
            return null;
        }

        return GlobalPos.of(player.getRespawnDimension(), overworldRespawnPos);
    }
}