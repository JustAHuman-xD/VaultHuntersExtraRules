package lv.id.bonne.vaulthuntersextrarules.data;

import lv.id.bonne.vaulthuntersextrarules.data.storage.PlayerSettings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class WorldSettings extends SavedData {
    private final Map<UUID, PlayerSettings> playerSettings = new HashMap<>();
    private static final WorldSettings client_settings = WorldSettings.create();

    /**
     * Creates a WorldSettings instance
     * @return Default WorldSettings instance
     */
    public static WorldSettings create() {
        return new WorldSettings();
    }

    /**
     * Writes our settings onto an NBT tag
     * @param tag Tag we wish to write our settings onto
     * @return Modified NBT tag with our settings
     */
    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("playerSettings", serializePlayerSettings());

        return tag;
    }

    /**
     * Method for fetching the settings from a Level
     * @param level Preferably a ServerLevel, but not strictly required.
     * @return A populated WorldSettings instance
     */
    public static WorldSettings get(Level level) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER && level instanceof ServerLevel serverLevel) {
            return serverLevel.getServer().overworld().getDataStorage().computeIfAbsent(WorldSettings.load(serverLevel), WorldSettings::create, "vault_hunters_extra_rules_WorldSettings");
        }
        else {
            return client_settings; // Should hopefully not be returned 🤞
        }
    }

    /**
     * Generates a function for loading settings from NBT
     * @param level Preferably the ServerLevel we load our data onto.
     *              In most cases the overworld since it is always partially loaded.
     * @return Function for loading settings from NBT
     */
    public static Function<CompoundTag, WorldSettings> load(ServerLevel level) {
        return (tag) -> {
            WorldSettings data = create();

            // Should only be called once.
            PlayerSettings.prepare();

            if (tag.contains("playerSettings")) {
                CompoundTag playersCompound = tag.getCompound("playerSettings");
                playersCompound.getAllKeys().forEach(playerUUID -> {
                    data.playerSettings.put(UUID.fromString(playerUUID), new PlayerSettings(playersCompound.getCompound(playerUUID)));
                });
            }

            return data;
        };
    }

    /**
     * Creates a NBT tag with UUID-PlayerSettings key-value pairs.
     * @return A compound tag
     */
    public CompoundTag serializePlayerSettings() {
        CompoundTag tag = new CompoundTag();
        playerSettings.forEach((key, value) -> {
            tag.put(key.toString(), value.serialize());
        });
        return tag;
    }

    /**
     * Accessor method for obtaining a player's settings
     * Creates a key and value if they don't already exist
     * @param uuid UUID of the player you would like to access
     * @return PlayerSettings object
     */
    public PlayerSettings getPlayerSettings(UUID uuid) {
        return this.playerSettings.computeIfAbsent(uuid, uid -> new PlayerSettings());
    }
}
