package dev.willowv.solcc;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static dev.willowv.solcc.SpiceOfLifeCarrotCake.MOD_ID;

public class SOLCCCommands {
    private static final String TARGETS_ARGUMENT = "targets";

    private static final Predicate<CommandSourceStack> IS_ALLOWED_TO_MUTATE = source -> source.hasPermission(2);

    private SOLCCCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
            (commandDispatcher,
             registryAccess,
             registrationEnvironment) ->
                    commandDispatcher.register(Commands.literal(MOD_ID + ":clear_history")
                    .requires(IS_ALLOWED_TO_MUTATE)
                    .executes(context ->
                        clearHistory(
                                context.getSource(),
                                Collections.singleton(context.getSource().getPlayer())
                        )
                    )
                    .then(
                        Commands.argument(TARGETS_ARGUMENT, EntityArgument.players())
                                .executes(context ->
                                        clearHistory(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, TARGETS_ARGUMENT)
                                        )
                                )
                    )));
    }

    private static int clearHistory(CommandSourceStack commandSource, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            ((IFoodHistoryManager) player).solcc$clearUniqueFoodsEaten();
            player.sendSystemMessage(Component.translatable("command.solcc.clear_history"));
        }

        commandSource.sendSuccess(() -> Component.translatable("command.solcc.clear_history_multiple", players.toString()), true);

        return players.size();
    }
}
