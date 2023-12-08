package lv.id.bonne.vaulthuntersextrarules.data.storage;

import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.mixin.InvokerGameRulesValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerSettings {
    private final Map<GameRules.Key<?>, GameRules.Value<?>> gameRules = new HashMap<>();
    private final SavedData parent;

    public PlayerSettings(SavedData parent) {
        this.parent = parent;
    }

    public PlayerSettings(SavedData parent, CompoundTag tag) {
        this.parent = parent;
        this.deserialize(tag);
    }

    public <T extends GameRules.Value<T>> Optional<T> get(GameRules.Key<T> ruleKey) {
        if (this.gameRules.containsKey(ruleKey)) {
            return Optional.of((T)(this.gameRules.get(ruleKey)));
        }
        else {
            return Optional.empty();
        }
    }

    @Nullable
    public <T extends GameRules.Value<T>> T put(GameRules.Key<T> ruleKey, GameRules.Value<T> ruleValue) {
        parent.setDirty();
        return (T) gameRules.put(ruleKey, ruleValue);
    }

    @Nullable
    public <T extends GameRules.Value<T>> T remove(GameRules.Key<T> ruleKey) {
        parent.setDirty();
        return (T) gameRules.remove(ruleKey);
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        gameRules.forEach((key, value) ->  {
            tag.putString(key.getId(), value.serialize());
        });
        return tag;
    }

    public void deserialize(CompoundTag tag) {
        tag.getAllKeys().forEach(gameRuleKeyId -> {
            if (VaultHuntersExtraRules.gameRuleIdToKey.containsKey(gameRuleKeyId)) {
                GameRules.Key<?> key = VaultHuntersExtraRules.gameRuleIdToKey.get(gameRuleKeyId);
                GameRules.Value<?> value = VaultHuntersExtraRules.extraGameRules.get(key).getFirst().createRule();
                ((InvokerGameRulesValue)value).invokeDeserialize(tag.getString(gameRuleKeyId));

                gameRules.put(key, value);
            }
        });
    }
}
