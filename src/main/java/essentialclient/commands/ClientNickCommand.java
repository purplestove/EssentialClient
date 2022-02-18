package essentialclient.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import essentialclient.clientrule.ClientRules;
import essentialclient.utils.EssentialUtils;
import essentialclient.utils.command.CommandHelper;
import essentialclient.utils.config.ConfigClientNick;
import essentialclient.utils.render.ChatColour;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ClientNickCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

		if (!ClientRules.COMMAND_CLIENT_NICK.getValue()) {
			return;
		}

		CommandHelper.clientCommands.add("clientnick");

		dispatcher.register(literal("clientnick")
			.then(literal("set")
				.then(argument("playername1", StringArgumentType.string())
					.suggests((context, builder) -> CommandHelper.suggestOnlinePlayers(builder))
					.then(argument("playername2", StringArgumentType.greedyString())
						.executes(context -> {
							String playerName1 = context.getArgument("playername1", String.class);
							String playerName2 = context.getArgument("playername2", String.class);
							playerName2 = playerName2.replaceAll("&", "§") + "§r";
							ConfigClientNick.INSTANCE.set(playerName1, playerName2);
							EssentialUtils.sendMessage("%s%s will now be displayed as %s".formatted(ChatColour.GREEN, playerName1, playerName2));
							return 0;
						})
					)
				)
			)
			.then(literal("delete")
				.then(argument("playername", StringArgumentType.string())
					.suggests((context, builder) -> ConfigClientNick.INSTANCE.suggestPlayerRename(builder))
					.executes(context -> {
						String playerName = context.getArgument("playername", String.class);
						String name = ConfigClientNick.INSTANCE.remove(playerName);
						if (name == null) {
							EssentialUtils.sendMessage("%s%s was not renamed".formatted(ChatColour.RED, playerName));
						}
						else {
							EssentialUtils.sendMessage("%s%s will no longer be renamed".formatted(ChatColour.GOLD, playerName));
						}
						return 0;
					})
				)
			)
			.then(literal("get")
				.then(argument("playername", StringArgumentType.string())
					.suggests((context, builder) -> ConfigClientNick.INSTANCE.suggestPlayerRename(builder))
					.executes(context -> {
						String playerName = context.getArgument("playername", String.class);
						String name = ConfigClientNick.INSTANCE.get(playerName);
						if (name == null) {
							EssentialUtils.sendMessage("%s%s is not renamed".formatted(ChatColour.RED, playerName));
						}
						else {
							EssentialUtils.sendMessage("%s%s is renamed to %s".formatted(ChatColour.GOLD, playerName, name));
						}
						return 0;
					})
				)
			)
		);
	}
}
