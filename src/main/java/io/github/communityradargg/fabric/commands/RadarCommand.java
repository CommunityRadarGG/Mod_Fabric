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
package io.github.communityradargg.fabric.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.communityradargg.fabric.CommunityRadarMod;
import io.github.communityradargg.fabric.radarlistmanager.RadarList;
import io.github.communityradargg.fabric.radarlistmanager.RadarListEntry;
import io.github.communityradargg.fabric.radarlistmanager.RadarListManager;
import io.github.communityradargg.fabric.radarlistmanager.RadarListVisibility;
import io.github.communityradargg.fabric.utils.Messages;
import io.github.communityradargg.fabric.utils.RadarMessage;
import io.github.communityradargg.fabric.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RadarCommand {
    private static final String COMMAND_NAME = "radar";
    private static final List<String> COMMAND_ALIASES = List.of("communityradar", "scammer", "trustedmm", "mm");
    private static final int REQUIRED_PERMISSION_LEVEL = 0;

    public static void register(final @NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final LiteralCommandNode<FabricClientCommandSource> mainCommand = dispatcher.register(ClientCommandManager.literal(COMMAND_NAME)
                .requires(source -> source.getPlayer().hasPermissionLevel(REQUIRED_PERMISSION_LEVEL))
                .then(ClientCommandManager.literal("help")
                        .executes(context -> handleHelpSubcommand(context.getSource()))
                )
                .then(ClientCommandManager.literal("lists")
                        .executes(context -> {
                            handleListsSubcommand(context.getSource());
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(ClientCommandManager.literal("check")
                        .then(ClientCommandManager.argument("player", StringArgumentType.string())
                                .executes(context -> {
                                    final String player = StringArgumentType.getString(context, "player");
                                    return handleCheckSubcommand(context.getSource(), player);
                                }))
                        .executes(context -> handleMissingArgs(context.getSource()))
                )
                .then(ClientCommandManager.literal("player")
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("namespace", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("player", StringArgumentType.string())
                                                .then(ClientCommandManager.argument("cause", StringArgumentType.greedyString())
                                                        .executes(context -> {
                                                            final String namespace = StringArgumentType.getString(context, "namespace");
                                                            final String player = StringArgumentType.getString(context, "player");
                                                            final String cause = StringArgumentType.getString(context, "cause");
                                                            handlePlayerAddSubcommand(context.getSource(), namespace, player, cause);
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                                .executes(context -> handleMissingArgs(context.getSource()))
                                        )
                                        .executes(context -> handleMissingArgs(context.getSource()))
                                )
                                .executes(context -> handleMissingArgs(context.getSource()))
                        )
                        .then(ClientCommandManager.literal("remove")
                                .then(ClientCommandManager.argument("namespace", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("player", StringArgumentType.string())
                                                .executes(context -> {
                                                    final String namespace = StringArgumentType.getString(context, "namespace");
                                                    final String player = StringArgumentType.getString(context, "player");
                                                    handlePlayerRemoveSubcommand(context.getSource(), namespace, player);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                        .executes(context -> handleMissingArgs(context.getSource()))
                                )
                                .executes(context -> handleMissingArgs(context.getSource()))
                        )
                        .executes(context -> handleHelpSubcommand(context.getSource()))
                )
                .then(ClientCommandManager.literal("list")
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("namespace", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("prefix", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    final String namespace = StringArgumentType.getString(context, "namespace");
                                                    final String prefix = StringArgumentType.getString(context, "prefix");
                                                    handleListAddSubcommand(context.getSource(), namespace, prefix);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                        .executes(context -> handleMissingArgs(context.getSource()))
                                )
                                .executes(context -> handleMissingArgs(context.getSource()))
                        )
                        .then(ClientCommandManager.literal("delete")
                                .then(ClientCommandManager.argument("namespace", StringArgumentType.string())
                                        .executes(context -> {
                                            final String namespace = StringArgumentType.getString(context, "namespace");
                                            handleListDeleteSubcommand(context.getSource(), namespace);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .executes(context -> handleMissingArgs(context.getSource()))
                        )
                        .then(ClientCommandManager.literal("show")
                                .then(ClientCommandManager.argument("namespace", StringArgumentType.string())
                                        .executes(context -> {
                                            final String namespace = StringArgumentType.getString(context, "namespace");
                                            handleListShowSubcommand(context.getSource(), namespace);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .executes(context -> handleMissingArgs(context.getSource()))
                        )
                        .then(ClientCommandManager.literal("prefix")
                                .then(ClientCommandManager.argument("namespace", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("prefix", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    final String namespace = StringArgumentType.getString(context, "namespace");
                                                    final String prefix = StringArgumentType.getString(context, "prefix");
                                                    handleListPrefixSubcommand(context.getSource(), namespace, prefix);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                        .executes(context -> handleMissingArgs(context.getSource()))
                                )
                                .executes(context -> handleMissingArgs(context.getSource()))
                        )
                        .executes(context -> handleHelpSubcommand(context.getSource()))
                )
                .executes(context -> handleHelpSubcommand(context.getSource())));
        COMMAND_ALIASES.forEach(alias -> dispatcher.register(ClientCommandManager.literal(alias).redirect(mainCommand)));
    }

    /**
     * Handles the missing argument case.
     *
     * @param source The command source, which executed the subcommand.
     * @return Returns {@link Command#SINGLE_SUCCESS}.
     */
    private static int handleMissingArgs(final @NotNull FabricClientCommandSource source) {
        source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.MISSING_ARGS)
                .build().toText());
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Handles the help subcommand.
     *
     * @param source The command source, which executed the subcommand.
     * @return Returns {@link Command#SINGLE_SUCCESS}.
     */
    private static int handleHelpSubcommand(final @NotNull FabricClientCommandSource source) {
        source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.HELP)
                .replace("{code_version}", CommunityRadarMod.getVersion())
                .excludePrefix().build().toText());
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Handles the lists subcommand.
     *
     * @param source The command source, which executed the subcommand.
     */
    private static void handleListsSubcommand(final @NotNull FabricClientCommandSource source) {
        final StringBuilder listsText = new StringBuilder();
        for (final String namespace : CommunityRadarMod.getListManager().getNamespaces()) {
            CommunityRadarMod.getListManager().getRadarList(namespace)
                    .ifPresent(radarList -> listsText.append("§e").append(namespace).append(" §7(§c")
                            .append(radarList.getRadarListVisibility() == RadarListVisibility.PRIVATE ? Messages.Lists.PRIVATE : Messages.Lists.PUBLIC)
                            .append("§7)").append(", "));
        }

        if (!listsText.isEmpty()) {
            // players on the list
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Lists.FOUND)
                    .replace("{lists}", listsText.substring(0, listsText.length() - 2))
                    .build().toText());
        } else {
            // list is empty
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Lists.EMPTY)
                    .build().toText());
        }
    }

    /**
     * Handles the check subcommand.
     *
     * @param source The command source, which executed the subcommand.
     * @param playerArgument The given player argument to check.
     * @return Returns the result of the command.
     */
    private static int handleCheckSubcommand(final @NotNull FabricClientCommandSource source, final @NotNull String playerArgument) {
        if ("*".equals(playerArgument)) {
            handleCheckAllSubcommand(source);
        }
        handleCheckPlayerSubcommand(source, playerArgument);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Handles the check - player subcommand.
     *
     * @param source The command source, which executed the subcommand.
     * @param playerArgument The given player argument to check.
     */
    private static void handleCheckPlayerSubcommand(final @NotNull FabricClientCommandSource source, final @NotNull String playerArgument) {
        source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.INPUT_PROCESSING)
                .build().toText());

        Utils.getUUID(playerArgument).thenAccept(checkPlayerOptional -> {
            if (checkPlayerOptional.isEmpty()) {
                // player uuid could not be fetched
                source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Check.FAILED)
                        .build().toText());
                return;
            }

            final Optional<RadarListEntry> entryOptional = CommunityRadarMod.getListManager().getRadarListEntry(checkPlayerOptional.get());
            if (entryOptional.isEmpty()) {
                // player uuid is on no list
                source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Check.FAILED)
                        .build().toText());
                return;
            }

            final RadarListEntry entry = entryOptional.get();
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Check.FOUND + "\n" + Messages.Check.CHECK_ENTRY)
                    .replaceWithColorCodes("{prefix}", CommunityRadarMod.getListManager().getPrefix(entry.uuid()))
                    .replace("{name}", entry.name())
                    .replace("{cause}", entry.cause())
                    .replace("{entryCreationDate}", Utils.formatDateTime(entry.entryCreationDate()))
                    .replace("{entryUpdateDate}", Utils.formatDateTime(entry.entryUpdateDate()))
                    .build().toText());
        });
    }

    /**
     * Handles the check - all subcommand.
     *
     * @param source The command source, which executed the subcommand.
     */
    private static void handleCheckAllSubcommand(final @NotNull FabricClientCommandSource source) {
        final ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) {
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Check.NOT_FOUND)
                    .build().toText());
            return;
        }

        boolean anyPlayerFound = false;
        for (final PlayerListEntry player : networkHandler.getPlayerList()) {
            if (player.getProfile().getId() == null) {
                continue;
            }

            final Optional<RadarListEntry> listEntryOptional = CommunityRadarMod.getListManager()
                    .getRadarListEntry(player.getProfile().getId());
            if (listEntryOptional.isEmpty()) {
                // player uuid is on no list
                continue;
            }

            if (!anyPlayerFound) {
                source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Check.EVERYONE)
                        .build().toText());
                anyPlayerFound = true;
            }

            final RadarListEntry entry = listEntryOptional.get();
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Check.CHECK_ENTRY)
                    .replaceWithColorCodes("{prefix}", CommunityRadarMod.getListManager().getPrefix(entry.uuid()))
                    .replace("{name}", entry.name())
                    .replace("{cause}", entry.cause())
                    .replace("{entryCreationDate}", Utils.formatDateTime(entry.entryCreationDate()))
                    .replace("{entryUpdateDate}", Utils.formatDateTime(entry.entryUpdateDate()))
                    .build().toText());
        }

        if (!anyPlayerFound) {
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Check.NOT_FOUND)
                    .build().toText());
        }
    }


    /**
     * Handles the player - add subcommand.
     *
     * @param source The command source, which executed the subcommand.
     */
    private static void handlePlayerAddSubcommand(final @NotNull FabricClientCommandSource source, final @NotNull String namespace, final @NotNull String player, final @NotNull String cause) {
        final RadarListManager listManager = CommunityRadarMod.getListManager();
        final Optional<RadarList> listOptional = listManager.getRadarList(namespace);
        if (listOptional.isEmpty()) {
            // list not existing
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Player.ADD_FAILED)
                    .build().toText());
            return;
        }

        source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.INPUT_PROCESSING)
                .build().toText());
        Utils.getUUID(player).thenAccept(uuidOptional -> {
            if (uuidOptional.isEmpty()) {
                // player uuid could not be fetched
                source.sendFeedback(new RadarMessage.RadarMessageBuilder(player.startsWith("!") ? Messages.Player.NAME_INVALID_BEDROCK : Messages.Player.NAME_INVALID)
                        .build().toText());
                return;
            }

            final UUID uuid = uuidOptional.get();
            if (listOptional.get().isInList(uuid)) {
                // player already on list
                source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Player.ADD_IN_LIST)
                        .build().toText());
                return;
            }

            if (!CommunityRadarMod.getListManager().addRadarListEntry(namespace, uuid, player, cause)) {
                // list is not private
                source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Player.ADD_FAILED)
                        .build().toText());
                return;
            }

            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Player.ADD_SUCCESS)
                    .build().toText());
        });
    }

    /**
     * Handles the player - remove subcommand.
     *
     * @param source The command source, which executed the subcommand.
     */
    private static void handlePlayerRemoveSubcommand(final @NotNull FabricClientCommandSource source, final @NotNull String namespace, final @NotNull String player) {
        final RadarListManager listManager =  CommunityRadarMod.getListManager();
        final Optional<RadarList> listOptional = listManager.getRadarList(namespace);
        if (listOptional.isEmpty()) {
            // list is not existing
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Player.REMOVE_FAILED)
                    .build().toText());
            return;
        }

        source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.INPUT_PROCESSING)
                .build().toText());
        final RadarList list = listOptional.get();
        Utils.getUUID(player).thenAccept(uuidOptional -> {
            if (uuidOptional.isEmpty()) {
                // player uuid could not be fetched
                source.sendFeedback(new RadarMessage.RadarMessageBuilder(player.startsWith("!") ? Messages.Player.NAME_INVALID_BEDROCK : Messages.Player.NAME_INVALID)
                        .build().toText());
                return;
            }

            final UUID uuid = uuidOptional.get();
            if (!list.isInList(uuid)) {
                // player uuid not on list
                source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Player.REMOVE_NOT_IN_LIST)
                        .build().toText());
                return;
            }

            list.getPlayerMap().remove(uuid);
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.Player.REMOVE_SUCCESS)
                    .build().toText());
        });
    }

    /**
     * Handles the list - add subcommand.
     *
     * @param source The command source, which executed the subcommand.
     */
    private static void handleListAddSubcommand(final @NotNull FabricClientCommandSource source, final @NotNull String namespace, final @NotNull String prefix) {
        if (CommunityRadarMod.getListManager().getRadarList(namespace).isPresent()) {
            // list already existing
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.List.CREATE_FAILED)
                    .build().toText());
            return;
        }

        if (!CommunityRadarMod.getListManager().registerPrivateList(namespace, prefix)) {
            // list could not be registered
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.List.CREATE_FAILED)
                    .build().toText());
            return;
        }

        source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.List.CREATE_SUCCESS)
                .build().toText());
    }

    /**
     * Handles the list - delete subcommand.
     *
     * @param source The command source, which executed the subcommand.
     */
    private static void handleListDeleteSubcommand(final @NotNull FabricClientCommandSource source, final @NotNull String namespace) {
        final RadarListManager listManager = CommunityRadarMod.getListManager();
        if (!listManager.unregisterList(namespace)) {
            // list is not existing, list is not private, file cannot be deleted
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.List.DELETE_FAILED)
                    .build().toText());
            return;
        }

        source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.List.DELETE_SUCCESS)
                .build().toText());
    }

    /**
     * Handles the list - show subcommand.
     *
     * @param source The command source, which executed the subcommand.
     */
    private static void handleListShowSubcommand(final @NotNull FabricClientCommandSource source, final @NotNull String namespace) {
        final Optional<RadarList> listOptional = CommunityRadarMod.getListManager().getRadarList(namespace);
        if (listOptional.isEmpty()) {
            // list is not existing
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.List.SHOW_FAILED)
                    .build().toText());
            return;
        }

        final RadarList list = listOptional.get();
        if (list.getPlayerMap().isEmpty()) {
            // list is empty
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.List.SHOW_EMPTY)
                    .build().toText());
            return;
        }

        final StringBuilder players = new StringBuilder();
        list.getPlayerMap().values().forEach(value -> players.append(value.name()).append(", "));
        source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.List.SHOW_SUCCESS)
                .replace("{list}", list.getNamespace())
                .replaceWithColorCodes("{prefix}", listOptional.get().getPrefix())
                .replace("{players}", players.substring(0, players.length() - 2))
                .build().toText());
    }

    /**
     * Handles the list - prefix subcommand.
     *
     * @param source The command source, which executed the subcommand.
     */
    private static void handleListPrefixSubcommand(final @NotNull FabricClientCommandSource source, final @NotNull String namespace, final @NotNull String prefix) {
        final RadarListManager listManager = CommunityRadarMod.getListManager();
        final Optional<RadarList> listOptional = listManager.getRadarList(namespace);
        if (listOptional.isEmpty()) {
            // list is not existing
            source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.List.PREFIX_FAILED)
                    .build().toText());
            return;
        }

        final RadarList list = listOptional.get();
        list.setPrefix(prefix);
        list.saveList();

        source.sendFeedback(new RadarMessage.RadarMessageBuilder(Messages.List.PREFIX_SUCCESS)
                .replaceWithColorCodes("{prefix}", prefix)
                .build().toText());
    }
}
