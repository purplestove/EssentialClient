package essentialclient.commands;

import com.mojang.brigadier.CommandDispatcher;
import essentialclient.utils.command.CommandHelper;
import net.minecraft.server.command.ServerCommandSource;

public class CommandRegister {
	public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		CommandHelper.clearClientCommands();
		PlayerClientCommand.register(dispatcher);
		PlayerListCommand.register(dispatcher);
		RegionCommand.register(dispatcher);
		TravelCommand.register(dispatcher);
		MusicCommand.register(dispatcher);
		ClientNickCommand.register(dispatcher);
		UpdateClientCommand.register(dispatcher);
		AlternateDimensionCommand.register(dispatcher);
		ClientScriptCommand.register(dispatcher);
		CommandHelper.registerFunctionCommands(dispatcher);
	}
}
