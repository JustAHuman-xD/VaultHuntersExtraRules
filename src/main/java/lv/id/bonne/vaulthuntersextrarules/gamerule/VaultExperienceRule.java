package lv.id.bonne.vaulthuntersextrarules.gamerule;


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
 * This is copy-paste and adjust a bit from VaultLoot.java. // sorry devs.
 */
public enum VaultExperienceRule implements StringRepresentable
{
    NONE("none", 0.0F),
    TURTLE("turtle", 0.33F),
    SLOW("slow", 0.5F),
    NORMAL("normal", 1.0F),
    PLENTY("plenty", 2.0F),
    EXTREME("extreme", 3.0F);

    private final String name;

    private final float multiplier;

    private static final Map<String, VaultExperienceRule> NAME_VALUES;


    VaultExperienceRule(String name, float multiplier)
    {
        this.name = name;
        this.multiplier = multiplier;
    }


    @NotNull
    public String getSerializedName()
    {
        return this.name;
    }


    public float getMultiplier()
    {
        return this.multiplier;
    }


    public static VaultExperienceRule fromName(String name)
    {
        return NAME_VALUES.getOrDefault(name, NORMAL);
    }


    static
    {
        ImmutableMap.Builder<String, VaultExperienceRule> builder = new ImmutableMap.Builder<>();
        VaultExperienceRule[] var1 = values();

        for (VaultExperienceRule value : var1)
        {
            builder.put(value.getSerializedName(), value);
        }

        NAME_VALUES = builder.build();
    }


    public static class GameRuleValue extends GameRules.Value<VaultExperienceRule.GameRuleValue>
    {
        private VaultExperienceRule mode;


        public GameRuleValue(GameRules.Type<VaultExperienceRule.GameRuleValue> type)
        {
            super(type);
            this.mode = VaultExperienceRule.NORMAL;
        }


        public GameRuleValue(GameRules.Type<VaultExperienceRule.GameRuleValue> type, VaultExperienceRule mode)
        {
            super(type);
            this.mode = VaultExperienceRule.NORMAL;
        }


        public static GameRules.Type<VaultExperienceRule.GameRuleValue> create(VaultExperienceRule defaultValue)
        {
            return new GameRules.Type<>(
                () -> EnumArgument.enumArgument(VaultExperienceRule.class),
                (type) -> new GameRuleValue(type, defaultValue),
                (s, v) -> {},
                (v, k, t) -> {}
            );
        }


        protected void updateFromArgument(CommandContext<CommandSourceStack> context, String paramName)
        {
            this.mode = context.getArgument(paramName, VaultExperienceRule.class);
        }


        protected void deserialize(String value)
        {
            this.mode = VaultExperienceRule.fromName(value);
        }


        public String serialize()
        {
            return this.mode.getSerializedName();
        }


        public int getCommandResult()
        {
            return this.mode.getSerializedName().hashCode();
        }


        protected VaultExperienceRule.GameRuleValue getSelf()
        {
            return this;
        }


        protected VaultExperienceRule.GameRuleValue copy()
        {
            return new VaultExperienceRule.GameRuleValue(this.type, this.mode);
        }


        public void setFrom(VaultExperienceRule.GameRuleValue value, @Nullable MinecraftServer pServer)
        {
            this.mode = value.mode;
            this.onChanged(pServer);
        }


        public VaultExperienceRule get()
        {
            return this.mode;
        }
    }
}

