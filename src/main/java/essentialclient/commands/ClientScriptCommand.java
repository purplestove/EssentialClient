package essentialclient.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import essentialclient.clientscript.ClientScript;
import essentialclient.config.clientrule.ClientRules;
import essentialclient.utils.EssentialUtils;
import essentialclient.utils.clientscript.ScriptRepositoryManager;
import essentialclient.utils.command.CommandHelper;
import essentialclient.utils.render.ChatColour;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import static essentialclient.utils.clientscript.ScriptRepositoryManager.Category;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ClientScriptCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		CommandHelper.clientCommands.add("clientscript");

		LiteralArgumentBuilder<ServerCommandSource>
			root = literal("clientscript"),
			download = literal("download"),
			setScript = literal("setscript"),
			runFromWeb = literal("runfromweb");

		root.then(literal("run")
			.executes(context -> {
				ClientScript.getInstance().startScript();
				return 1;
			})
			.then(argument("script", StringArgumentType.greedyString())
				.executes(context -> {
					String scriptContent = StringArgumentType.getString(context, "script");
					ClientScript.getInstance().startScript("fromCommand", scriptContent);
					return 1;
				})
			)
		);

		setScript.then(argument("scriptname", StringArgumentType.string())
			.suggests((c, b) -> CommandSource.suggestMatching(ClientScript.getScriptNames(), b))
			.executes(context -> {
				String scriptName = StringArgumentType.getString(context, "scriptname");
				ClientRules.CLIENT_SCRIPT_FILENAME.setValue(scriptName);
				ClientRules.CLIENT_SCRIPT_FILENAME.run();
				EssentialUtils.sendMessage(ChatColour.GOLD + "Set current script to: " + ChatColour.GREEN + scriptName);
				return 1;
			})
		);

		for (Category category : Category.values()) {
			String categoryName = category.toString();
			download.then(literal(categoryName)
				.then(argument("scriptname", StringArgumentType.string())
					.suggests(((c, b) -> CommandSource.suggestMatching(ScriptRepositoryManager.INSTANCE.getChildrenNames(category), b)))
					.executes(context -> {
						String scriptName = StringArgumentType.getString(context, "scriptname");
						ScriptRepositoryManager.INSTANCE.downloadScript(category, scriptName, false);
						EssentialUtils.sendMessage(ChatColour.GOLD + "Successfully downloaded: " + ChatColour.GREEN + scriptName);
						return 1;
					})
					.then(argument("shouldoverwrite", BoolArgumentType.bool())
						.executes(context -> {
							String scriptName = StringArgumentType.getString(context, "scriptname");
							boolean shouldOverwrite = BoolArgumentType.getBool(context, "shouldoverwrite");
							ScriptRepositoryManager.INSTANCE.downloadScript(category, scriptName, shouldOverwrite);
							EssentialUtils.sendMessage(ChatColour.GOLD + "Successfully downloaded: " + ChatColour.GREEN + scriptName);
							return 1;
						})
					)
				)
			);
			runFromWeb.then(literal(categoryName)
				.then(argument("scriptname", StringArgumentType.string())
					.suggests(((c, b) -> CommandSource.suggestMatching(ScriptRepositoryManager.INSTANCE.getChildrenNames(category), b)))
					.executes(context -> {
						String scriptName = StringArgumentType.getString(context, "scriptname");
						String scriptContent = ScriptRepositoryManager.INSTANCE.getScriptFromWeb(category, scriptName, true);
						ClientScript.getInstance().startScript(scriptName, scriptContent);
						return 1;
					})
					.then(argument("fromcache", BoolArgumentType.bool())
						.executes(context -> {
							String scriptName = StringArgumentType.getString(context, "scriptname");
							boolean fromCache = BoolArgumentType.getBool(context, "fromcache");
							String scriptContent = ScriptRepositoryManager.INSTANCE.getScriptFromWeb(category, scriptName, fromCache);
							ClientScript.getInstance().startScript(scriptName, scriptContent);
							return 1;
						})
					)
				)
			);
		}

		dispatcher.register(root.then(download).then(setScript).then(runFromWeb));
	}
}
