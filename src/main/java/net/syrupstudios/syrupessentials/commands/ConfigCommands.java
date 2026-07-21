package net.syrupstudios.syrupessentials.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.syrupstudios.syrupessentials.config.SyrupEssentialsConfig;

public final class ConfigCommands {
    private ConfigCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("syrupessentials")
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ConfigCommands::reload)));
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        SyrupEssentialsConfig.LoadResult result = SyrupEssentialsConfig.load();
        if (!result.successful()) {
            context.getSource().sendFailure(Component.literal(
                    "Unable to reload Syrup Essentials config: " + result.error()
            ));
            return 0;
        }

        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            context.getSource().getServer().getCommands().sendCommands(player);
        }

        String message = "Reloaded Syrup Essentials config from " + SyrupEssentialsConfig.getPath()
                + ". Command namespace settings take effect after a restart";
        if (!result.warnings().isEmpty()) {
            message += " with " + result.warnings().size() + " warning(s); check the server log";
        }
        String successMessage = message;
        context.getSource().sendSuccess(() -> Component.literal(successMessage), true);
        return 1;
    }
}
