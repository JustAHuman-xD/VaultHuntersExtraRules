package lv.id.bonne.vaulthuntersextrarules;


import iskallia.vault.world.VaultLoot;
import lv.id.bonne.vaulthuntersextrarules.gamerule.VaultExperienceRule;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;


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
    public static void setupCommon(FMLCommonSetupEvent event)
    {
        COIN_LOOT = register("vaultExtraCoinDrops",
            GameRules.Category.MISC,
            VaultLoot.GameRuleValue.create(VaultLoot.NORMAL));
        COPIOUSLY_DROP = register("vaultExtraCopiouslyDropModifier",
            GameRules.Category.MISC,
            VaultLoot.GameRuleValue.create(VaultLoot.NORMAL));

        BONUS_XP = register("vaultExtraBonusExperienceModifier",
            GameRules.Category.MISC,
            VaultExperienceRule.GameRuleValue.create(VaultExperienceRule.NORMAL));
        COMPLETION_XP = register("vaultExtraCompletionExperienceModifier",
            GameRules.Category.MISC,
            VaultExperienceRule.GameRuleValue.create(VaultExperienceRule.NORMAL));
    }


    /**
     * A simple method that initializes game rule.
     * @param name The name of the game rule.
     * @param category The category of the game rule.
     * @param type The type of the game rule.
     * @return The game rule key.
     * @param <T> The type of the game rule.
     */
    public static <T extends GameRules.Value<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type)
    {
        return GameRules.register(name, category, type);
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
}
