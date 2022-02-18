package essentialclient.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import essentialclient.clientscript.core.ClientScript;
import essentialclient.clientscript.core.ClientScriptInstance;
import essentialclient.utils.EssentialUtils;
import essentialclient.utils.clientscript.ScriptRepositoryManager;
import essentialclient.utils.command.CommandHelper;
import essentialclient.utils.command.EnumArgumentType;
import essentialclient.utils.render.ChatColour;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

import static essentialclient.utils.clientscript.ScriptRepositoryManager.Category;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ClientScriptCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		CommandHelper.clientCommands.add("clientscript");

		LiteralArgumentBuilder<ServerCommandSource>
			root = literal("clientscript"),
			download = literal("download"),
			runFromWeb = literal("runfromweb");

		root.then(literal("run")
			.then(argument("scriptName", StringArgumentType.string())
				.suggests((c, b) -> CommandSource.suggestMatching(List.of("exampleName", "scriptFromCommand"), b))
				.then(argument("script", StringArgumentType.greedyString())
					.executes(context -> {
						String scriptName = StringArgumentType.getString(context, "scriptName");
						String scriptContent = StringArgumentType.getString(context, "script");
						ClientScriptInstance.runFromContent(scriptName, scriptContent);
						return 1;
					})
				)
			)
		);

		root.then(literal("stopall")
			.executes(context -> {
				ClientScript.INSTANCE.stopAllInstances();
				return 1;
			})
		);

		download.then(argument("category", EnumArgumentType.enumeration(Category.class))
			.then(argument("scriptname", StringArgumentType.string())
				.suggests(((c, b) -> CommandSource.suggestMatching(
					ScriptRepositoryManager.INSTANCE.getChildrenNames(EnumArgumentType.getEnumeration(c, "category", Category.class)), b))
				)
				.executes(context -> {
					Category category = EnumArgumentType.getEnumeration(context, "category", Category.class);
					String scriptName = StringArgumentType.getString(context, "scriptname");
					ScriptRepositoryManager.INSTANCE.downloadScript(category, scriptName, false);
					EssentialUtils.sendMessage(ChatColour.GOLD + "Successfully downloaded: " + ChatColour.GREEN + scriptName);
					return 1;
				})
				.then(argument("shouldoverwrite", BoolArgumentType.bool())
					.executes(context -> {
						Category category = EnumArgumentType.getEnumeration(context, "category", Category.class);
						String scriptName = StringArgumentType.getString(context, "scriptname");
						boolean shouldOverwrite = BoolArgumentType.getBool(context, "shouldoverwrite");
						ScriptRepositoryManager.INSTANCE.downloadScript(category, scriptName, shouldOverwrite);
						EssentialUtils.sendMessage(ChatColour.GOLD + "Successfully downloaded: " + ChatColour.GREEN + scriptName);
						return 1;
					})
				)
			)
		);

		runFromWeb.then(argument("category", EnumArgumentType.enumeration(Category.class))
			.then(argument("scriptname", StringArgumentType.string())
				.suggests(((c, b) -> CommandSource.suggestMatching(
					ScriptRepositoryManager.INSTANCE.getChildrenNames(EnumArgumentType.getEnumeration(c, "category", Category.class)), b))
				)
				.executes(context -> {
					Category category = EnumArgumentType.getEnumeration(context, "category", Category.class);
					String scriptName = StringArgumentType.getString(context, "scriptname");
					String scriptContent = ScriptRepositoryManager.INSTANCE.getScriptFromWeb(category, scriptName, true);
					ClientScriptInstance.runFromContent(scriptName, scriptContent);
					return 1;
				})
				.then(argument("fromcache", BoolArgumentType.bool())
					.executes(context -> {
						Category category = EnumArgumentType.getEnumeration(context, "category", Category.class);
						String scriptName = StringArgumentType.getString(context, "scriptname");
						boolean fromCache = BoolArgumentType.getBool(context, "fromcache");
						String scriptContent = ScriptRepositoryManager.INSTANCE.getScriptFromWeb(category, scriptName, fromCache);
						ClientScriptInstance.runFromContent(scriptName, scriptContent);
						return 1;
					})
				)
			)
		);

		dispatcher.register(root.then(download).then(runFromWeb));
	}
}
