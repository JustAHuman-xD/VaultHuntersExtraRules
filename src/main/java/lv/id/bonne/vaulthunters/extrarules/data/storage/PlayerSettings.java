package lv.id.bonne.vaulthunters.extrarules.data.storage;

import lv.id.bonne.vaulthunters.extrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthunters.extrarules.mixin.InvokerGameRulesValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * This class is used to store player settings.
 */
public class PlayerSettings {
    /**
     * Map of game rules.
     */
    private final Map<GameRules.Key<?>, GameRules.Value<?>> gameRules = new HashMap<>();

    /**
     * Parent SavedData.
     */
    private final SavedData parent;


    /**
     * Constructor for PlayerSettings.
     *
     * @param parent Parent SavedData.
     */
    public PlayerSettings(SavedData parent) {
        this.parent = parent;
    }


    /**
     * Constructor for PlayerSettings.
     *
     * @param parent Parent SavedData.
     * @param tag    CompoundTag to deserialize.
     */
    public PlayerSettings(SavedData parent, CompoundTag tag) {
        this.parent = parent;
        this.deserialize(tag);
    }


    /**
     * Gets the game rule.
     *
     * @param ruleKey The rule key.
     * @param <T>     The type of the game rule.
     * @return The game rule.
     */
    public <T extends GameRules.Value<T>> Optional<T> get(GameRules.Key<T> ruleKey) {
        if (this.gameRules.containsKey(ruleKey)) {
            return Optional.of((T) (this.gameRules.get(ruleKey)));
        } else {
            return Optional.empty();
        }
    }


    /**
     * Puts the game rule.
     *
     * @param ruleKey   The rule key.
     * @param ruleValue The rule value.
     * @param <T>       The type of the game rule.
     * @return The game rule.
     */
    @Nullable
    public <T extends GameRules.Value<T>> T put(GameRules.Key<T> ruleKey, GameRules.Value<T> ruleValue) {
        parent.setDirty();
        return (T) gameRules.put(ruleKey, ruleValue);
    }


    /**
     * Removes the game rule.
     *
     * @param ruleKey The rule key.
     * @param <T>     The type of the game rule.
     * @return The game rule.
     */
    @Nullable
    public <T extends GameRules.Value<T>> T remove(GameRules.Key<T> ruleKey) {
        parent.setDirty();
        return (T) gameRules.remove(ruleKey);
    }


    /**
     * Serializes the game rules.
     *
     * @return The serialized game rules.
     */
    @Nonnull
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        gameRules.forEach((key, value) -> tag.putString(key.getId(), value.serialize()));
        return tag;
    }


    /**
     * Deserializes the game rules.
     *
     * @param tag The tag to deserialize.
     */
    public void deserialize(CompoundTag tag) {
        tag.getAllKeys().forEach(gameRuleKeyId ->
        {
            if (VaultHuntersExtraRules.GAME_RULE_ID_TO_KEY.containsKey(gameRuleKeyId)) {
                GameRules.Key<?> key = VaultHuntersExtraRules.GAME_RULE_ID_TO_KEY.get(gameRuleKeyId);
                GameRules.Value<?> value = VaultHuntersExtraRules.EXTRA_GAME_RULES.get(key).getFirst().createRule();
                ((InvokerGameRulesValue) value).invokeDeserialize(tag.getString(gameRuleKeyId));

                gameRules.put(key, value);
            }
        });
    }
}
