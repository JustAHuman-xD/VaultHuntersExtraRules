package lv.id.bonne.vaulthunters.extrarules;


import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModGameRules;
import iskallia.vault.world.VaultLoot;
import lv.id.bonne.vaulthunters.extrarules.command.ExtraRulesCommand;
import lv.id.bonne.vaulthunters.extrarules.gamerule.Locality;
import lv.id.bonne.vaulthunters.extrarules.gamerule.SpawnPointRule;
import lv.id.bonne.vaulthunters.extrarules.gamerule.VaultExperienceRule;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;


/**
 * The main class for Vault Hunters Extra Rules mod.
 */
@Mod("vault_hunters_extra_rules")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class VaultHuntersExtraRules {
    public static final Map<String, GameRules.Key<?>> GAME_RULE_ID_TO_KEY = new HashMap<>();
    public static final Map<GameRules.Key<?>, Pair<GameRules.Type<?>, Locality>> EXTRA_GAME_RULES = new HashMap<>();
    public static final Logger LOGGER = LogUtils.getLogger();

    public static GameRules.Key<VaultLoot.GameRuleValue> COIN_LOOT;
    public static GameRules.Key<VaultLoot.GameRuleValue> COPIOUSLY_DROP;
    public static GameRules.Key<VaultExperienceRule.GameRuleValue> SOUL_SHARD_DROP;

    public static GameRules.Key<VaultExperienceRule.GameRuleValue> BONUS_XP;
    public static GameRules.Key<VaultExperienceRule.GameRuleValue> COMPLETION_XP;

    public static GameRules.Key<GameRules.BooleanValue> REUSE_PEDESTALS;

    public static GameRules.Key<GameRules.BooleanValue> ALLOW_FLASK_USE;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_TRINKET_SWAP;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_CHARM_SWAP;

    public static GameRules.Key<GameRules.BooleanValue> LOCALIZED_GAMERULES;
    public static GameRules.Key<SpawnPointRule.GameRuleValue> SPIRIT_SPAWN_LOCATION;
    public static GameRules.Key<GameRules.IntegerValue> MAX_PLAYER_LEVEL;

    public VaultHuntersExtraRules() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void setupCommon(FMLCommonSetupEvent event) {
        COIN_LOOT = register("vaultExtraCoinDrops",
                GameRules.Category.MISC,
                VaultLoot.GameRuleValue.create(VaultLoot.NORMAL),
                Locality.VAULT);
        COPIOUSLY_DROP = register("vaultExtraCopiouslyDropModifier",
                GameRules.Category.MISC,
                VaultLoot.GameRuleValue.create(VaultLoot.NORMAL),
                Locality.VAULT);
        SOUL_SHARD_DROP = register("vaultExtraSoulShardDrops",
                GameRules.Category.MISC,
                VaultExperienceRule.GameRuleValue.create(VaultExperienceRule.NORMAL),
                Locality.SERVER);

        BONUS_XP = register("vaultExtraBonusExperienceModifier",
                GameRules.Category.MISC,
                VaultExperienceRule.GameRuleValue.create(VaultExperienceRule.NORMAL), Locality.PLAYER);
        COMPLETION_XP = register("vaultExtraCompletionExperienceModifier",
                GameRules.Category.MISC,
                VaultExperienceRule.GameRuleValue.create(VaultExperienceRule.NORMAL), Locality.PLAYER);

        REUSE_PEDESTALS = register("vaultExtraReusePedestals",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(false), Locality.VAULT);

        LOCALIZED_GAMERULES = register("vaultExtraLocalizedGameRules",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(false), Locality.SERVER, false);

        ALLOW_FLASK_USE = register("vaultExtraAllowFlaskUseWhilePause",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(false), Locality.VAULT);

        ALLOW_TRINKET_SWAP = register("vaultExtraAllowTrinketSwapWhilePause",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(false), Locality.VAULT);

        ALLOW_CHARM_SWAP = register("vaultExtraAllowCharmSwapWhilePause",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(false), Locality.VAULT);

        SPIRIT_SPAWN_LOCATION = register("vaultExtraSpiritSpawnLocation",
                GameRules.Category.MISC,
                SpawnPointRule.GameRuleValue.create(SpawnPointRule.DEFAULT), Locality.SERVER);

        // Register Vault Hunters rules to locality

        event.enqueueWork(() -> {
            // CRYSTAL_MODE per Player
            registerLocality(ModGameRules.CRYSTAL_MODE,
                    GameRules.GAME_RULE_TYPES.get(ModGameRules.CRYSTAL_MODE),
                    true,
                    Locality.PLAYER);

            // LOOT per Vault
            registerLocality(ModGameRules.LOOT,
                    GameRules.GAME_RULE_TYPES.get(ModGameRules.LOOT),
                    true,
                    Locality.VAULT);

            // MODE per Vault
            registerLocality(ModGameRules.MODE,
                    GameRules.GAME_RULE_TYPES.get(ModGameRules.MODE),
                    true,
                    Locality.SERVER);

            // Get max level after few ticks
            MAX_PLAYER_LEVEL = register("vaultExtraMaxPlayerLevel",
                    GameRules.Category.MISC,
                    GameRules.IntegerValue.create(ModConfigs.LEVELS_META.getMaxLevel()),
                    Locality.SERVER);
        });
    }

    public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name,
                                                                           GameRules.Category category,
                                                                           GameRules.Type<T> type,
                                                                           Locality locality) {
        return register(name, category, type, locality, true);
    }

    public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name,
                                                                           GameRules.Category category,
                                                                           GameRules.Type<T> type,
                                                                           Locality locality,
                                                                           boolean hasLocality) {
        GameRules.Key<T> key = GameRules.register(name, category, type);
        VaultHuntersExtraRules.registerLocality(key, type, hasLocality, locality);
        return key;
    }

    public static void registerLocality(GameRules.Key<?> key, GameRules.Type<?> type, boolean hasLocality, Locality locality) {
        GAME_RULE_ID_TO_KEY.put(key.getId(), key);
        if (hasLocality) {
            EXTRA_GAME_RULES.put(key, new Pair<>(type, locality));
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            ExtraRulesCommand.register(event.getDispatcher());
        }
    }

    public static Locality getDefaultLocality(GameRules.Key<?> ruleKey) {
        return EXTRA_GAME_RULES.getOrDefault(ruleKey, new Pair<>(null, Locality.SERVER)).getSecond();
    }
}
