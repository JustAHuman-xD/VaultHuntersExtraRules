package lv.id.bonne.vaulthuntersextrarules;


import com.mojang.datafixers.util.Pair;
import iskallia.vault.world.VaultLoot;
import lv.id.bonne.vaulthuntersextrarules.command.ExtraRulesCommand;
import lv.id.bonne.vaulthuntersextrarules.gamerule.Locality;
import lv.id.bonne.vaulthuntersextrarules.gamerule.VaultExperienceRule;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.HashMap;
import java.util.Map;


/**
 * The main class for Vault Hunters Extra Rules mod.
 */
@Mod("vault_hunters_extra_rules")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class VaultHuntersExtraRules
{
    /**
     * The main class initialization.
     */
    public VaultHuntersExtraRules()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }


    /**
     * Initializes custom GameRules.
     *
     * @param event the FMLCommonSetupEvent event
     */
    @SubscribeEvent
    public static void setupCommon(FMLCommonSetupEvent event) {
        COIN_LOOT = register("vaultExtraCoinDrops",
            GameRules.Category.MISC,
            VaultLoot.GameRuleValue.create(VaultLoot.NORMAL), Locality.SERVER);
        COPIOUSLY_DROP = register("vaultExtraCopiouslyDropModifier",
            GameRules.Category.MISC,
            VaultLoot.GameRuleValue.create(VaultLoot.NORMAL), Locality.SERVER);

        BONUS_XP = register("vaultExtraBonusExperienceModifier",
            GameRules.Category.MISC,
            VaultExperienceRule.GameRuleValue.create(VaultExperienceRule.NORMAL), Locality.PLAYER);
        COMPLETION_XP = register("vaultExtraCompletionExperienceModifier",
            GameRules.Category.MISC,
            VaultExperienceRule.GameRuleValue.create(VaultExperienceRule.NORMAL), Locality.PLAYER);

        REUSE_PEDESTALS = register("vaultExtraReusePedestals",
            GameRules.Category.MISC,
            GameRules.BooleanValue.create(false), Locality.VAULT);
        SKIP_ALTAR_RETURNING = register("vaultExtraSkipAltarReturning",
            GameRules.Category.MISC,
            GameRules.BooleanValue.create(false), Locality.VAULT);

        LOCALIZED_GAMERULES = register("vaultExtraLocalizedGameRules",
                GameRules.Category.MISC,
                GameRules.BooleanValue.create(false), Locality.SERVER, false);
    }


    /**
     * Accessor for fetching the default locality for a gamerule
     * @param ruleKey Gamerule Key
     * @return Gamerule's default locality
     */
    public static Locality getDefaultLocality(GameRules.Key<?> ruleKey)
    {
        return extraGameRules.getOrDefault(ruleKey, new Pair<>(null, Locality.SERVER)).getSecond();
    }


    /**
     * A simple method that initializes game rule.
     * @param name The name of the game rule.
     * @param category The category of the game rule.
     * @param type The type of the game rule.
     * @param locality They type of locality the GameRules has
     * @param hasLocality Controls whether the GameRule has locality
     * @return The game rule key.
     * @param <T> The type of the game rule.
     */
    public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type, Locality locality, boolean hasLocality)
    {
        GameRules.Key<T> key = GameRules.register(name, category, type);
        gameRuleIdToKey.put(key.getId(), key);
        if (hasLocality)
            extraGameRules.put(key, new Pair<>(type, locality));
        return key;
    }


    /**
     * A simple method that initializes game rule.
     * @param name The name of the game rule.
     * @param category The category of the game rule.
     * @param type The type of the game rule.
     * @return The game rule key.
     * @param <T> The type of the game rule.
     */
    public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name,
        GameRules.Category category,
        GameRules.Type<T> type,
        Locality locality)
    {
        return register(name, category, type, locality, true);
    }


    /**
     * The Coin Loot GameRule.
     */
    public static GameRules.Key<VaultLoot.GameRuleValue> COIN_LOOT;

    /**
     * The Copiously Drop GameRule.
     */
    public static GameRules.Key<VaultLoot.GameRuleValue> COPIOUSLY_DROP;

    /**
     * The Vault Bonus Experience GameRule.
     */
    public static GameRules.Key<VaultExperienceRule.GameRuleValue> BONUS_XP;

    /**
     * The Vault Completion Experience GameRule.
     */
    public static GameRules.Key<VaultExperienceRule.GameRuleValue> COMPLETION_XP;

    /**
     * The GameRule that allows users to reuse cake pedestals and tic tac obelisks.
     */
    public static GameRules.Key<GameRules.BooleanValue> REUSE_PEDESTALS;

    /**
     * The GameRule that allows users to do not return to god altar after completing its challenge.
     */
    public static GameRules.Key<GameRules.BooleanValue> SKIP_ALTAR_RETURNING;

    /**
     * The GameRule that allows users to have local customization
     */
    public static GameRules.Key<GameRules.BooleanValue> LOCALIZED_GAMERULES;

    /**
     * Map of game rule id to key.
     */
    public static final Map<String, GameRules.Key<?>> gameRuleIdToKey = new HashMap<>();

    /**
     * Map of game rule id to key.
     */
    public static final Map<GameRules.Key<?>, Pair<GameRules.Type<?>, Locality>> extraGameRules = new HashMap<>();

    /**
     * Forge Event Bus
     */
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents
    {
        /**
         * Registers the mod's commands
         * @param event The event holding the command dispatcher
         */
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event)
        {
            ExtraRulesCommand.register(event.getDispatcher());
        }
    }
}
