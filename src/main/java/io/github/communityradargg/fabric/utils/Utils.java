/*
 * Copyright 2024 - present CommunityRadarGG <https://community-radar.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.communityradargg.fabric.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.communityradargg.fabric.CommunityRadarMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class with some util methods.
 */
public class Utils {
    private static final Logger logger = LogManager.getLogger(Utils.class);
    private static final String MOJANG_API_NAME_TO_UUID = "https://api.mojang.com/users/profiles/minecraft/";
    private static final Pattern UUID_MOJANG_API_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    private static final Pattern CHAT_PLAYER_NAME = Pattern.compile("[A-Za-z\\-+]+\\s\\u2503\\s(~?!?\\w{1,16})");
    private static final DateTimeFormatter readableDateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final Map<String, UUID> uuidNameCache = new HashMap<>();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    /**
     * Tries to get the uuid to the player name from the world.
     *
     * @param playerName The player name to get the corresponding uuid.
     * @return Returns a CompletableFuture with an optional with the player uuid.
     */
    public static @NotNull CompletableFuture<Optional<UUID>> getUUID(final @NotNull String playerName) {
        final ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
        if (clientPacketListener == null) {
            // user has to be in a world
            return CompletableFuture.completedFuture(Optional.empty());
        }

        if (uuidNameCache.containsKey(playerName)) {
            // if the uuid has been cached, returning from the map
            return CompletableFuture.completedFuture(Optional.of(uuidNameCache.get(playerName)));
        }

        // checking if there is a player with same name in the loaded world. If so, returning the uuid from the profile
        for (final PlayerInfo playerInfo : clientPacketListener.getOnlinePlayers()) {
            if (playerInfo.getProfile().name().equalsIgnoreCase(playerName)) {
                uuidNameCache.put(playerName, playerInfo.getProfile().id());
                return CompletableFuture.completedFuture(Optional.of(playerInfo.getProfile().id()));
            }
        }

        if (playerName.startsWith("!") || playerName.startsWith("~")) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        // if no player with same name is in the world, try fetching the uuid from the Mojang-API.
        return requestUuidForName(playerName);
    }

    /**
     * Requests an uuid to a player name, from the Mojang API.
     *
     * @param playerName The player name to get the uuid for.
     * @return Returns a CompletableFuture with an optional with the requested uuid, it will be empty if an error occurred on requesting.
     */
    private static @NotNull CompletableFuture<Optional<UUID>> requestUuidForName(final @NotNull String playerName) {
        final String uriText = MOJANG_API_NAME_TO_UUID + playerName;
        final URI uri = URI.create(uriText);
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(3))
                .header("User-Agent", CommunityRadarMod.getModId() + "/" + CommunityRadarMod.getVersion())
                .GET()
                .build();

        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        logger.warn("Requesting data from '{}' resulted in following status code: {}", uriText, response.statusCode());
                        return Optional.<UUID>empty();
                    }

                    final JsonObject json = new Gson().fromJson(response.body(), JsonObject.class);
                    if (json == null || !json.has("id") || !json.has("name")) {
                        return Optional.<UUID>empty();
                    }

                    final UUID uuid = UUID.fromString(UUID_MOJANG_API_PATTERN.matcher(json.get("id").getAsString()).replaceAll("$1-$2-$3-$4-$5"));
                    uuidNameCache.put(playerName, uuid);
                    return Optional.of(uuid);
                })
                .exceptionally(e -> {
                    logger.error("Trying to request data from '{}' resulted in an exception", uriText, e);
                    return Optional.empty();
                });
    }

    /**
     * Formats a given date time in a human-readable form.
     *
     * @param localDateTime The local date time to format.
     * @return Returns the formatted date time.
     */
    public static @NotNull String formatDateTime(final @NotNull LocalDateTime localDateTime) {
        return localDateTime.format(readableDateTimeFormatter);
    }

    /**
     * Checks if a given hostname is a hostname of GrieferGames.
     * <br><br>
     * Following domains are taken into account:
     * <br>
     * - griefergames.net
     * <br>
     * - griefergames.de
     * <br>
     * - griefergames.live
     *
     * @param hostName The hostname to check.
     * @return Returns, whether the given hostname is one of the GrieferGames hostnames.
     */
    private static boolean isGrieferGamesHostName(final @NotNull String hostName) {
        final String filteredHostName = Optional.of(hostName)
                .filter(host -> host.endsWith("."))
                .map(host -> host.substring(0, host.length() - 1).toLowerCase(Locale.ENGLISH))
                .orElse(hostName.toLowerCase(Locale.ENGLISH));
        return filteredHostName.endsWith("griefergames.net") || filteredHostName.endsWith("griefergames.de") || filteredHostName.endsWith("griefergames.live");
    }

    /**
     * Check if the player is connected to GrieferGames.
     *
     * @return Returns, whether the player is connected to GrieferGames.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // better readable this way
    public static boolean isOnGrieferGames() {
        final ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
        if (clientPacketListener == null) {
            return false;
        }

        final Connection connection = clientPacketListener.getConnection();
        if (connection.isMemoryConnection() || !(connection.getRemoteAddress() instanceof InetSocketAddress inetSocketAddress)) {
            return false;
        }
        return isGrieferGamesHostName(inetSocketAddress.getHostName());
    }

    /**
     * Builds the new component including the radar prefix.
     *
     * @param playerUuid The uuid of the player to get the component with the radar prefix for.
     * @param oldNameTagComponent The old component that should be extended, if needed.
     * @return The new component including the radar prefix.
     */
    public static Component includePrefixComponent(final @NotNull UUID playerUuid, final @NotNull Component oldNameTagComponent) {
        final String addonPrefix = CommunityRadarMod.getListManager()
                .getPrefix(playerUuid)
                .replace("&", "§");

        if (!addonPrefix.isEmpty()) {
            return Component.empty().append(addonPrefix + " ").append(oldNameTagComponent);
        }
        return oldNameTagComponent;
    }

    /**
     * Searches the uuid of a player sending a message in the chat.
     *
     * @param chatMessage The chat message to search for the player.
     * @return Returns a CompletableFuture with an optional of type uuid.
     */
    public static @NotNull CompletableFuture<Optional<UUID>> getChatMessagePlayer(final @NotNull String chatMessage) {
        final Matcher playerNameMatcher = CHAT_PLAYER_NAME.matcher(chatMessage);
        if (!playerNameMatcher.find()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        final String playerName = playerNameMatcher.group(1);
        if (playerName.startsWith("~")) {
            // nicked player
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return Utils.getUUID(playerName);
    }
}
