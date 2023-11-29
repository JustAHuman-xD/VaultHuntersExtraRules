package lv.id.bonne.vaulthuntersextrarules.util;

import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.world.data.ServerVaults;
import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.data.WorldSettings;
import lv.id.bonne.vaulthuntersextrarules.data.storage.PlayerSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class GameRuleHelper {

    /**
     * Helper method to simplify obtaining GameRule values.
     * Handles localized game rules.
     *
     * @param ruleKey Key of the GameRule you would like to access
     * @param anyLevel Any server-side level
     * @param playerUUID The UUID of the player settings we wish to use. If null, reverts to default GameRule handling.
     * @return The GameRule value wrapper
     */
    public static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> ruleKey, Level anyLevel, @Nullable UUID playerUUID) {
        boolean localizedGameRules = anyLevel.getGameRules().getRule(VaultHuntersExtraRules.LOCALIZED_GAMERULES).get();

        if (localizedGameRules && playerUUID != null) {
            WorldSettings settings = WorldSettings.get(anyLevel);

            PlayerSettings ownerSettings = settings.getPlayerSettings(playerUUID);
            Optional<T> optionalRule = ownerSettings.get(ruleKey);

            if (optionalRule.isPresent()) {
                return optionalRule.get();
            }
        }

        return anyLevel.getGameRules().getRule(ruleKey);
    }

    /**
     * Helper method to simplify obtaining GameRule values.
     * Handles localized game rules.
     *
     * @param ruleKey Key of the GameRule you would like to access
     * @param anyLevel Any server-side level
     * @param vault The vault that the player is currently in. If null, reverts to default GameRule handling.
     * @return The GameRule value wrapper
     */
    public static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> ruleKey, Level anyLevel, @Nullable Vault vault) {
        if (vault != null) {
            return getRule(ruleKey, anyLevel, vault.get(Vault.OWNER));
        }
        return getRule(ruleKey, anyLevel, (UUID) null);
    }

    /**
     * Helper method to simplify obtaining GameRule values.
     * Handles localized game rules.
     *
     * @param ruleKey Key of the GameRule you would like to access
     * @param anyLevel Any server-side level
     * @param eventLevel The level that the event took place in.
     * @return The GameRule value wrapper
     */
    public static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> ruleKey, Level anyLevel, Level eventLevel) {
        Optional<Vault> optionalVault = eventLevel.isClientSide ? ClientVaults.getActive() : ServerVaults.get(eventLevel);

        return getRule(ruleKey, anyLevel, optionalVault.orElse(null));
    }

    /**
     * Helper method to simplify obtaining GameRule values.
     * Handles localized game rules.
     * <br/><b>NOTE</b>: The UUID of the passed player is <em>NOT</em> used. Rather
     * the player's level is used to identify the vault.
     *
     * @param ruleKey Key of the GameRule you would like to access
     * @param anyLevel Any server-side level
     * @param player The player's level is fetched and used as the eventLevel.
     * @return The GameRule value wrapper
     */
    public static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> ruleKey, Level anyLevel, ServerPlayer player) {
        return getRule(ruleKey, anyLevel, player.getLevel());
    }

    /**
     * Helper method to simplify obtaining GameRule values.
     * Handles localized game rules.
     *
     * @param ruleKey Key of the GameRule you would like to access
     * @param eventLevel The level that the event took place in.
     * @return The GameRule value wrapper
     */
    public static <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> ruleKey, Level eventLevel) {
        return getRule(ruleKey, eventLevel, eventLevel);
    }

}
