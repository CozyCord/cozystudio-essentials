package net.syrupstudios.syrupessentials.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class CommandUtil {
    private static String commandPrefix = "/";

    public static void setNamespaced(boolean namespaced) {
        commandPrefix = namespaced ? "/syrupessentials " : "/";
    }

    public static String commandPath(String command) {
        return commandPrefix + command;
    }

    public static void commandSuccess(String message, CommandContext<CommandSourceStack> context){
        context.getSource().sendSuccess(() -> Component.literal(message), false);
    }

    public static void commandFailure(String message, CommandContext<CommandSourceStack> context) {
        Objects.requireNonNull(context.getSource().getPlayer()).sendSystemMessage(Component.literal(message));
    }
}
