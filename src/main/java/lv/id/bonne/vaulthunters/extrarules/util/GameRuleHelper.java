package lv.id.bonne.vaulthunters.extrarules.util;

import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthunters.extrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthunters.extrarules.data.WorldSettings;
import lv.id.bonne.vaulthunters.extrarules.data.storage.PlayerSettings;
import lv.id.bonne.vaulthunters.extrarules.gamerule.Locality;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;


/**
 * This class is used to help with game rules.
 */
public class GameRuleHelper {

    /**
     * <b>Use {@link GameRuleHelper#getRule(GameRules.Key, Level, Player)} or
     * {@link GameRuleHelper#getRule(GameRules.Key, Player)} instead!</b><br/><br/>
     * <p>
     * Helper method to simplify obtaining GameRule values.<br/> Handles localized game rules.<br/>
     *
     * @param ruleKey    Key of the GameRule you would like to access
     * @param anyLevel   Any server-side level
     * @param playerUUID The UUID of the player settings we wish to use. If null, reverts to default GameRule handling.
     * @return The GameRule value wrapper
     */
    private static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> ruleKey,
                                                            Level anyLevel,
                                                            @Nullable UUID playerUUID) {
        boolean localizedGameRules = anyLevel.getGameRules().getRule(VaultHuntersExtraRules.LOCALIZED_GAMERULES).get();

        if (localizedGameRules && playerUUID != null) {
            WorldSettings settings = WorldSettings.get(anyLevel);

            if (settings.getGameRuleLocality(ruleKey) != Locality.SERVER) { // Only if allowed to be local by server owner
                PlayerSettings ownerSettings = settings.getPlayerSettings(playerUUID);
                Optional<T> optionalRule = ownerSettings.get(ruleKey);

                if (optionalRule.isPresent()) {
                    return optionalRule.get();
                }
            }
        }

        return anyLevel.getGameRules().getRule(ruleKey);
    }


    /**
     * <b>Use {@link GameRuleHelper#getRule(GameRules.Key, Level, Player)} or
     * {@link GameRuleHelper#getRule(GameRules.Key, Player)} instead!</b><br/><br/>
     * <p>
     * Helper method to simplify obtaining GameRule values.<br/> Handles localized game rules.<br/>
     *
     * @param ruleKey  Key of the GameRule you would like to access
     * @param anyLevel Any server-side level
     * @param vault    The vault that the player is currently in. If null, reverts to default GameRule handling.
     * @return The GameRule value wrapper
     */
    private static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> ruleKey,
                                                            Level anyLevel,
                                                            @Nullable Vault vault) {
        if (vault != null) {
            return getRule(ruleKey, anyLevel, vault.get(Vault.OWNER));
        }

        return getRule(ruleKey, anyLevel, (UUID) null);
    }


    /**
     * <b>Use {@link GameRuleHelper#getRule(GameRules.Key, Level, Player)} or
     * {@link GameRuleHelper#getRule(GameRules.Key, Player)} instead!</b><br/><br/>
     * <p>
     * Helper method to simplify obtaining GameRule values.<br/> Handles localized game rules.<br/>
     *
     * @param ruleKey    Key of the GameRule you would like to access
     * @param anyLevel   Any server-side level
     * @param eventLevel The level that the event took place in.
     * @return The GameRule value wrapper
     */
    private static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> ruleKey, Level anyLevel, Level eventLevel) {
        Optional<Vault> optionalVault = eventLevel.isClientSide ?
                ClientVaults.getActive() : ServerVaults.get(eventLevel);

        return getRule(ruleKey, anyLevel, optionalVault.orElse(null));
    }


    /**
     * <b>Use {@link GameRuleHelper#getRule(GameRules.Key, Level, Player)} or
     * {@link GameRuleHelper#getRule(GameRules.Key, Player)} instead!</b><br/><br/>
     * <p>
     * Helper method to simplify obtaining GameRule values.<br/> Handles localized game rules.<br/>
     *
     * @param ruleKey    Key of the GameRule you would like to access
     * @param eventLevel The level that the event took place in.
     * @return The GameRule value wrapper
     */
    private static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> ruleKey, Level eventLevel) {
        return getRule(ruleKey, eventLevel, eventLevel);
    }


    /**
     * Helper method to simplify obtaining GameRule values.<br/> Handles localized game rules.<br/>
     *
     * @param ruleKey Key of the GameRule you would like to access
     * @param player  The player who is performing the event
     * @return The GameRule value wrapper
     */
    public static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> ruleKey, Player player) {
        WorldSettings settings = WorldSettings.get(player.getLevel());
        return settings.getGameRuleLocality(ruleKey) == Locality.PLAYER
                ? getRule(ruleKey, player.getLevel(), player.getUUID())
                : getRule(ruleKey, player.getLevel());
    }


    /**
     * Helper method to simplify obtaining GameRule values.<br/> Handles localized game rules.<br/>
     *
     * @param ruleKey    Key of the GameRule you would like to access
     * @param playerUUID The player who is performing the event
     * @return The GameRule value wrapper
     */
    @Nullable
    public static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> ruleKey, UUID playerUUID) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : GameRuleHelper.getRule(ruleKey, server.overworld(), playerUUID);
    }


    /**
     * Helper method to simplify obtaining GameRule values.<br/> Handles localized game rules.<br/>
     *
     * @param ruleKey    Key of the GameRule you would like to access
     * @param eventLevel The level that the event took place in.
     * @param player     The player who is performing the event
     * @return The GameRule value wrapper
     */
    public static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> ruleKey,
                                                           Level eventLevel,
                                                           @Nullable Player player) {
        if (player != null) {
            return getRule(ruleKey, player);
        } else {
            return getRule(ruleKey, eventLevel);
        }
    }
}
