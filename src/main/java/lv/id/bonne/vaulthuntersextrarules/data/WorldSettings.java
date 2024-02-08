package lv.id.bonne.vaulthuntersextrarules.data;

import lv.id.bonne.vaulthuntersextrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthuntersextrarules.data.storage.PlayerSettings;
import lv.id.bonne.vaulthuntersextrarules.gamerule.Locality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;


/**
 * This class is used to store world settings.
 */
public class WorldSettings extends SavedData
{
    /**
     * Map of player settings.
     */
    private final Map<UUID, PlayerSettings> playerSettings = new HashMap<>();

    /**
     * Map of game rule locality.
     */
    private final Map<GameRules.Key<?>, Locality> gameRuleLocality = new HashMap<>();

    /**
     * Default client settings
     */
    private static final WorldSettings client_settings = WorldSettings.create();


    /**
     * Creates a WorldSettings instance
     *
     * @return Default WorldSettings instance
     */
    public static WorldSettings create()
    {
        return new WorldSettings();
    }


    /**
     * Writes our settings onto an NBT tag
     *
     * @param tag Tag we wish to write our settings onto
     * @return Modified NBT tag with our settings
     */
    @Override
    @NotNull
    public CompoundTag save(CompoundTag tag)
    {
        tag.put("playerSettings", serializePlayerSettings());
        tag.put("gameRuleLocality", serializeGameRuleLocalitySettings());
        return tag;
    }


    /**
     * Method for fetching the settings from a Level
     *
     * @param level Preferably a ServerLevel, but not strictly required.
     * @return A populated WorldSettings instance
     */
    public static WorldSettings get(Level level)
    {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER &&
            level instanceof ServerLevel serverLevel)
        {
            return serverLevel.getServer().overworld().getDataStorage().computeIfAbsent(WorldSettings.load(serverLevel),
                WorldSettings::create,
                "vault_hunters_extra_rules_WorldSettings");
        }
        else
        {
            return client_settings; // Should hopefully not be returned 🤞
        }
    }


    /**
     * Generates a function for loading settings from NBT
     *
     * @param level Preferably the ServerLevel we load our data onto. In most cases the overworld since it is always
     * partially loaded.
     * @return Function for loading settings from NBT
     */
    public static Function<CompoundTag, WorldSettings> load(ServerLevel level)
    {
        return (tag) ->
        {
            WorldSettings data = create();

            if (tag.contains("playerSettings"))
            {
                CompoundTag playersCompound = tag.getCompound("playerSettings");
                playersCompound.getAllKeys().forEach(playerUUID ->
                {
                    data.playerSettings.put(UUID.fromString(playerUUID),
                        new PlayerSettings(data, playersCompound.getCompound(playerUUID)));
                });
            }

            if (tag.contains("gameRuleLocality"))
            {
                CompoundTag gameRulesCompound = tag.getCompound("gameRuleLocality");
                gameRulesCompound.getAllKeys().forEach(keyName ->
                {
                    if (VaultHuntersExtraRules.GAME_RULE_ID_TO_KEY.containsKey(keyName))
                    {
                        data.setGameRuleLocality(VaultHuntersExtraRules.GAME_RULE_ID_TO_KEY.get(keyName),
                            Locality.valueOf(gameRulesCompound.getString(keyName)));
                    }
                });
            }

            return data;
        };
    }


    /**
     * Creates a NBT tag with UUID-PlayerSettings key-value pairs.
     *
     * @return A compound tag
     */
    public CompoundTag serializePlayerSettings()
    {
        CompoundTag tag = new CompoundTag();
        playerSettings.forEach((key, value) -> tag.put(key.toString(), value.serialize()));
        return tag;
    }


    /**
     * Creates a NBT tag with GameRuleID-Locality key-value pairs
     *
     * @return A compound tag
     */
    public CompoundTag serializeGameRuleLocalitySettings()
    {
        CompoundTag tag = new CompoundTag();
        gameRuleLocality.forEach(((key, locality) -> tag.putString(key.getId(), locality.name())));
        return tag;
    }


    /**
     * Accessor method for obtaining a player's settings<br/> Creates a key and value if they don't already exist
     *
     * @param uuid UUID of the player you would like to access
     * @return PlayerSettings object
     */
    public PlayerSettings getPlayerSettings(UUID uuid)
    {
        return this.playerSettings.computeIfAbsent(uuid, uid -> new PlayerSettings(this));
    }


    /**
     * Fetch locality for Gamerule
     *
     * @param ruleKey Target gamerule
     * @return The gamerule's locality
     */
    public Locality getGameRuleLocality(GameRules.Key<?> ruleKey)
    {
        return this.gameRuleLocality.getOrDefault(ruleKey, VaultHuntersExtraRules.getDefaultLocality(ruleKey));
    }


    /**
     * Set Gamerule locality and mark as dirty
     *
     * @param ruleKey Target gamerule
     * @param locality Locality
     */
    public void setGameRuleLocality(GameRules.Key<?> ruleKey, Locality locality)
    {
        this.gameRuleLocality.put(ruleKey, locality);
        this.setDirty();
    }
}
