package lv.id.bonne.vaulthunters.extrarules.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lv.id.bonne.vaulthunters.extrarules.VaultHuntersExtraRules;
import lv.id.bonne.vaulthunters.extrarules.data.WorldSettings;
import lv.id.bonne.vaulthunters.extrarules.data.storage.PlayerSettings;
import lv.id.bonne.vaulthunters.extrarules.gamerule.Locality;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Optional;


/**
 * This class adds extra rules command for clients
 */
public class ExtraRulesCommand {
    /**
     * Registers the command.
     *
     * @param dispatcher The command dispatcher.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("extra_rules");
        LiteralArgumentBuilder<CommandSourceStack> set = Commands.literal("set");

        LiteralArgumentBuilder<CommandSourceStack> localAllowed =
                Commands.literal("local_allowed").requires(stack -> stack.hasPermission(2));

        VaultHuntersExtraRules.EXTRA_GAME_RULES.forEach((key, pair) ->
        {
            GameRules.Type<?> type = pair.getFirst();

            set.then(Commands.literal(key.getId()).executes(ctx -> queryRule(ctx.getSource(), key)).
                    then(type.createArgument("value").executes(ctx -> setRule(ctx, key, type))).
                    then(Commands.literal("default").executes(ctx -> defaultRule(ctx, key))));

            localAllowed.then(Commands.literal(key.getId()).executes(ctx -> queryAllowed(ctx.getSource(), key)).
                    then(Commands.argument("value", EnumArgument.enumArgument(Locality.class)).
                            executes(ctx -> setAllowed(ctx, key))));
        });

        dispatcher.register(baseLiteral.then(set).then(localAllowed));
    }


    /**
     * Sets the allowed value.
     *
     * @param ctx     The command context.
     * @param ruleKey The rule key.
     * @return The command result.
     */
    private static int setAllowed(CommandContext<CommandSourceStack> ctx, GameRules.Key<?> ruleKey) {
        CommandSourceStack stack = ctx.getSource();
        WorldSettings settings = WorldSettings.get(stack.getLevel());

        Locality locality = ctx.getArgument("value", Locality.class);

        settings.setGameRuleLocality(ruleKey, locality);

        stack.sendSuccess(new TranslatableComponent("commands.gamerule.set", ruleKey.getId(), locality), true);

        return 1;
    }


    /**
     * Queries the allowed value.
     *
     * @param stack   The command source stack.
     * @param ruleKey The rule key.
     * @return The command result.
     * @throws CommandSyntaxException
     */
    private static int queryAllowed(CommandSourceStack stack, GameRules.Key<?> ruleKey) throws CommandSyntaxException {
        WorldSettings settings = WorldSettings.get(stack.getLevel());

        Locality locality = settings.getGameRuleLocality(ruleKey);

        stack.sendSuccess(new TranslatableComponent("commands.gamerule.query", ruleKey.getId(), locality), false);

        return 1;
    }


    /**
     * Sets the rule.
     *
     * @param ctx      The command context.
     * @param ruleKey  The rule key.
     * @param ruleType The rule type.
     * @param <T>      The type of the rule.
     * @return The command result.
     * @throws CommandSyntaxException
     */
    static <T extends GameRules.Value<T>> int setRule(CommandContext<CommandSourceStack> ctx,
                                                      GameRules.Key<T> ruleKey,
                                                      GameRules.Type<?> ruleType) throws CommandSyntaxException {
        CommandSourceStack stack = ctx.getSource();

        if (WorldSettings.get(stack.getLevel()).getGameRuleLocality(ruleKey) == Locality.SERVER) {
            stack.sendFailure(new TextComponent("Per-player configuration is not enabled for this gamerule!"));
            return 0;
        }

        if (!stack.getLevel().getGameRules().getRule(VaultHuntersExtraRules.LOCALIZED_GAMERULES).get()) {
            stack.sendFailure(new TextComponent("vaultExtraLocalizedGameRules is not enabled on this server!"));
            return 0;
        }

        WorldSettings settings = WorldSettings.get(stack.getLevel());
        ServerPlayer player = stack.getPlayerOrException();
        PlayerSettings playerSettings = settings.getPlayerSettings(player.getUUID());

        Optional<T> tOptional = playerSettings.get(ruleKey);

        if (tOptional.isPresent()) {
            tOptional.get().setFromArgument(ctx, "value");
            stack.sendSuccess(new TranslatableComponent("commands.gamerule.set",
                    ruleKey.getId(),
                    tOptional.get().toString()), true);
            // trigger save
            settings.setDirty();

            VaultHuntersExtraRules.LOGGER.info("GameRule " + ruleKey.getId() + " for " +
                    player.getDisplayName().getString() +
                    " changed.");

            return tOptional.get().getCommandResult();
        }

        GameRules.Value<T> value = (GameRules.Value<T>) ruleType.createRule();
        value.setFromArgument(ctx, "value");

        playerSettings.put(ruleKey, value);

        stack.sendSuccess(new TranslatableComponent("commands.gamerule.set", ruleKey.getId(), value.toString()), true);

        VaultHuntersExtraRules.LOGGER.info("GameRule " + ruleKey.getId() + " for " +
                player.getDisplayName().getString() +
                " changed to " + value.serialize());

        return value.getCommandResult();
    }


    /**
     * Sets the default rule.
     *
     * @param ctx     The command context.
     * @param ruleKey The rule key.
     * @param <T>     The type of the rule.
     * @return The command result.
     * @throws CommandSyntaxException
     */
    static <T extends GameRules.Value<T>> int defaultRule(CommandContext<CommandSourceStack> ctx,
                                                          GameRules.Key<T> ruleKey) throws CommandSyntaxException {
        CommandSourceStack stack = ctx.getSource();

        WorldSettings settings = WorldSettings.get(stack.getLevel());
        ServerPlayer player = stack.getPlayerOrException();
        PlayerSettings playerSettings = settings.getPlayerSettings(player.getUUID());

        playerSettings.remove(ruleKey);

        VaultHuntersExtraRules.LOGGER.info("GameRule " + ruleKey.getId() + " for " +
                player.getDisplayName().getString() +
                " changed to default.");

        stack.sendSuccess(new TranslatableComponent("commands.gamerule.set", ruleKey.getId(), "default"), true);
        return 0;
    }


    /**
     * Queries the rule.
     *
     * @param stack   The command source stack.
     * @param ruleKey The rule key.
     * @param <T>     The type of the rule.
     * @return The command result.
     * @throws CommandSyntaxException
     */
    static <T extends GameRules.Value<T>> int queryRule(CommandSourceStack stack, GameRules.Key<T> ruleKey)
            throws CommandSyntaxException {
        if (WorldSettings.get(stack.getLevel()).getGameRuleLocality(ruleKey) == Locality.SERVER) {
            stack.sendFailure(new TextComponent("Per-player configuration is not enabled for this gamerule!"));
            return 0;
        }

        if (!stack.getLevel().getGameRules().getRule(VaultHuntersExtraRules.LOCALIZED_GAMERULES).get()) {
            stack.sendFailure(new TextComponent("vaultExtraLocalizedGameRules is not enabled on this server!"));
            return 0;
        }

        WorldSettings settings = WorldSettings.get(stack.getLevel());
        ServerPlayer player = stack.getPlayerOrException();
        PlayerSettings playerSettings = settings.getPlayerSettings(player.getUUID());

        Optional<T> tOptional = playerSettings.get(ruleKey);
        if (tOptional.isPresent()) {
            stack.sendSuccess(new TranslatableComponent("commands.gamerule.query",
                    ruleKey.getId(),
                    tOptional.get().toString()), false);
            return tOptional.get().getCommandResult();
        }

        stack.sendSuccess(new TranslatableComponent("commands.gamerule.query", ruleKey.getId(), "default"), false);

        return 0;
    }
}
