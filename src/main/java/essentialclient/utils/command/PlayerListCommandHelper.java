package essentialclient.utils.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import essentialclient.EssentialClient;
import essentialclient.utils.file.FileHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.JsonHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayerListCommandHelper {

    public static Map<String, String> playerListHelperMap = new HashMap<>();

    public static final Codec<String> CODEC = Codec.STRING;

    public static final Codec<Map<String, String>> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void writeSaveFile() {
        Path file = getFile();
        try(BufferedWriter writer = Files.newBufferedWriter(file)) {
            MAP_CODEC.encodeStart(JsonOps.INSTANCE, playerListHelperMap)
                    .resultOrPartial(e -> EssentialClient.LOGGER.error("Could not write /playerlist data: {}", e))
                    .ifPresent(obj -> GSON.toJson(obj, writer));
        }
        catch (IOException e) {
            e.printStackTrace();
            EssentialClient.LOGGER.error("Failed to save /playerclient data");
        }
    }

    public static void readSaveFile() {
        Path file = getFile();
        if (!Files.isRegularFile(file)) {
            playerListHelperMap = new HashMap<>();
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            playerListHelperMap = new HashMap<>(MAP_CODEC.decode(JsonOps.INSTANCE, JsonHelper.deserialize(reader))
                    .getOrThrow(false, e -> EssentialClient.LOGGER.error("Could not read /playerlist data: {}", e))
                    .getFirst());
            return;
        }
        catch (JsonParseException | IOException e) {
            e.printStackTrace();
        }
        PlayerListCommandHelper.playerListHelperMap = new HashMap<>();
    }

    private static Path getFile() {
        FileHelper.checkIfEssentialClientDirExists();
        return FabricLoader.getInstance().getConfigDir().resolve("EssentialClient").resolve("EssentialClientPlayerList.json");
    }

    public static CompletableFuture<Suggestions> suggestPlayerList(SuggestionsBuilder builder) {
        if (playerListHelperMap.isEmpty())
            return CommandSource.suggestMatching(new String[]{}, builder);
        return CommandSource.suggestMatching(playerListHelperMap.keySet(), builder);
    }

    public static int createList(CommandContext<FabricClientCommandSource> context) {
        String listName = context.getArgument("listname", String.class);
        String data = playerListHelperMap.putIfAbsent(listName, "");
        if (data != null)
            context.getSource().sendFeedback(new LiteralText("§cThere is already a list with that name"));
        else
            context.getSource().sendFeedback(new LiteralText("§6A new list has been created"));
        PlayerListCommandHelper.writeSaveFile();
        return 0;
    }

    public static int deleteList(CommandContext<FabricClientCommandSource> context) {
        String listName = context.getArgument("listname", String.class);
        String data = playerListHelperMap.remove(listName);
        if (data == null)
            context.getSource().sendFeedback(new LiteralText("§cThere is no list with that name"));
        else
            context.getSource().sendFeedback(new LiteralText("§6List has been deleted"));
        PlayerListCommandHelper.writeSaveFile();
        return 0;
    }

    public static int addToList(CommandContext<FabricClientCommandSource> context) {
        String listName = context.getArgument("listname", String.class);
        String playerList = playerListHelperMap.get(listName);
        if (playerList == null) {
            context.getSource().sendFeedback(new LiteralText("§cThere are no saved list with that name"));
            return 0;
        }
        String playerName = context.getArgument("playername", String.class);
        if (PlayerClientCommandHelper.isPlayerNull(playerName)) {
            context.getSource().sendFeedback(new LiteralText("§cThere is no saved player with that name"));
            return 0;
        }
        if (playerList.contains(playerName)) {
            context.getSource().sendFeedback(new LiteralText("§cThat player is already saved in the list"));
            return 0;
        }
        if (playerList.equals(""))
            playerList = playerName;
        else
            playerList = String.join(", ", playerList, playerName);
        playerListHelperMap.put(listName, playerList);
        context.getSource().sendFeedback(new LiteralText("§6Player has been added to the list"));
        PlayerListCommandHelper.writeSaveFile();
        return 0;
    }

    public static int spawnFromList(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        String listName = context.getArgument("listname", String.class);
        String playerList = playerListHelperMap.get(listName);
        if (playerList == null) {
            source.sendFeedback(new LiteralText("§cThere is no saved list with that name"));
            return 0;
        }
        if (playerList.equals("")) {
            source.sendFeedback(new LiteralText("§cThat list is empty!"));
            return 0;
        }
        String[] playerArray = playerList.split(", ");
        int errors = 0;
        for (String player : playerArray) {
            if (PlayerClientCommandHelper.isPlayerNull(player)) {
                errors++;
                continue;
            }
            PlayerClientCommandHelper.spawnPlayer(source.getPlayer(), player);
        }
        if (errors > 0)
            source.sendFeedback(new LiteralText("§c" + errors + " players were invalid and couldn't be spawned"));
        else
            source.sendFeedback(new LiteralText("§6All player were spawned successfully"));
        return 0;
    }
}