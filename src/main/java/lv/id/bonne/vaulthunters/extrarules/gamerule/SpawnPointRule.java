package lv.id.bonne.vaulthunters.extrarules.gamerule;


import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.context.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.server.command.EnumArgument;


/**
 * The rules for spawn
 */
public enum SpawnPointRule implements StringRepresentable
{
    DEFAULT("default"),
    PORTAL("portal"),
    ALWAYS_PORTAL("always_portal"),
    WORLD_SPAWN("world_spawn"),
    ALWAYS_WORLD_SPAWN("always_world_spawn");

    private final String name;


    SpawnPointRule(String name)
    {
        this.name = name;
    }


    @Override
    @NotNull
    public String getSerializedName()
    {
        return name;
    }


    private static final Map<String, SpawnPointRule> NAME_VALUES;

    static
    {
        ImmutableMap.Builder<String, SpawnPointRule> builder = new ImmutableMap.Builder<>();

        for (SpawnPointRule value : SpawnPointRule.values())
        {
            builder.put(value.getSerializedName(), value);
        }

        NAME_VALUES = builder.build();
    }


    /**
     * From name spawn point rule.
     *
     * @param name the name
     * @return the spawn point rule
     */
    public static SpawnPointRule fromName(String name)
    {
        return NAME_VALUES.getOrDefault(name, DEFAULT);
    }


    public static class GameRuleValue extends GameRules.Value<SpawnPointRule.GameRuleValue>
    {

        private SpawnPointRule mode = SpawnPointRule.DEFAULT;


        public GameRuleValue(GameRules.Type<SpawnPointRule.GameRuleValue> type)
        {
            super(type);
        }


        public GameRuleValue(GameRules.Type<SpawnPointRule.GameRuleValue> type, SpawnPointRule mode)
        {
            super(type);
            this.mode = mode;
        }


        public static GameRules.Type<SpawnPointRule.GameRuleValue> create(SpawnPointRule defaultValue)
        {
            return new GameRules.Type<>(() -> EnumArgument.enumArgument(SpawnPointRule.class),
                type -> new GameRuleValue(type, defaultValue), (s, v) ->
            {
            }, (v, k, t) ->
            {
            });
        }


        @Override
        protected void updateFromArgument(CommandContext<CommandSourceStack> context, @NotNull String paramName)
        {
            this.mode = context.getArgument(paramName, SpawnPointRule.class);
        }


        @Override
        protected void deserialize(@NotNull String value)
        {
            this.mode = SpawnPointRule.fromName(value);
        }


        @Override
        @NotNull
        public String serialize()
        {
            return this.mode.getSerializedName();
        }


        @Override
        public int getCommandResult()
        {
            return this.mode.getSerializedName().hashCode();
        }


        @Override
        @NotNull
        protected SpawnPointRule.GameRuleValue getSelf()
        {
            return this;
        }


        @Override
        @NotNull
        protected SpawnPointRule.GameRuleValue copy()
        {
            return new SpawnPointRule.GameRuleValue(this.type, this.mode);
        }


        @Override
        public void setFrom(SpawnPointRule.GameRuleValue value, @Nullable MinecraftServer pServer)
        {
            this.mode = value.mode;
            this.onChanged(pServer);
        }


        public SpawnPointRule get()
        {
            return this.mode;
        }
    }
}