package lv.id.bonne.vaulthuntersextrarules.data.storage;

import lv.id.bonne.vaulthuntersextrarules.mixin.InvokerGameRulesValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameRules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerSettings {
    private static final Map<String, GameRules.Key<?>> idToKey = new HashMap<>();
    private static final Map<GameRules.Key<?>, GameRules.Type<?>> keyToType = new HashMap<>();
    private final Map<GameRules.Key<?>, GameRules.Value<?>> gameRules = new HashMap<>();

    public PlayerSettings() {
    }

    public PlayerSettings(CompoundTag tag) {
        this.deserialize(tag);
    }

    public static void prepare() {
        idToKey.clear();
        keyToType.clear();

        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                idToKey.put(key.getId(), key);
                keyToType.put(key, type);
            }
        });
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
        return (T) gameRules.put(ruleKey, ruleValue);
    }

    @Nullable
    public <T extends GameRules.Value<T>> T remove(GameRules.Key<T> ruleKey) {
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
            if (idToKey.containsKey(gameRuleKeyId)) {
                GameRules.Key<?> key = idToKey.get(gameRuleKeyId);
                GameRules.Value<?> value = keyToType.get(key).createRule();
                ((InvokerGameRulesValue)value).invokeDeserialize(tag.getString(gameRuleKeyId));

                gameRules.put(key, value);
            }
        });
    }
}
