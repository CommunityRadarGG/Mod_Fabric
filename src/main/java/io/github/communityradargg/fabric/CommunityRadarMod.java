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
package io.github.communityradargg.fabric;

import io.github.communityradargg.fabric.commands.RadarCommand;
import io.github.communityradargg.fabric.radarlistmanager.RadarListManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

public class CommunityRadarMod implements ModInitializer {
    /** The id of the mod. */
    private static final String MOD_ID = "communityradar";
    /** The version of the mod. */
    private static final String VERSION = getModVersion(MOD_ID);
    private static final Logger logger = LogManager.getLogger(CommunityRadarMod.class);
    private static RadarListManager listManager;

	@Override
	public void onInitialize() {
        logger.info("Starting the mod '{}' with the version '{}'!", MOD_ID, VERSION);
        final File directoryPath = Paths.get(new File("")
                        .getAbsolutePath(),"communityradar", "lists")
                .toFile();
        if (!directoryPath.exists() && !directoryPath.mkdirs()) {
            logger.error("Could not create directory: {}", directoryPath);
        }

        listManager = new RadarListManager(directoryPath.getAbsolutePath() + "/");
        registerPublicLists();
        // Needs to be after loading public lists
        listManager.loadPrivateLists();
        registerCommands();
        logger.info("Successfully started the mod '{}'!", MOD_ID);
	}

    /**
     * Registers the commands.
     */
    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> RadarCommand.register(dispatcher)));
    }

    /**
     * Registers the public lists.
     */
    private void registerPublicLists() {
        if (!listManager.registerPublicList("scammer", "&7[&cScammer&7]", "https://lists.community-radar.de/versions/v1/scammer.json")) {
            logger.error("Could not register public list 'scammers'!");
        }

        if (!listManager.registerPublicList("trusted", "&7[&aTrusted&7]", "https://lists.community-radar.de/versions/v1/trusted.json")) {
            logger.error("Could not register public list 'verbvllert_trusted'!");
        }
    }

    /**
     * Gets the mod version for a given mod id.
     *
     * @param modId The mod-id to get the version for.
     * @return Returns the version in a friendly String.
     * @throws IllegalArgumentException Thrown, when the mod container cannot be got for the given mod id.
     */
    public static @NotNull String getModVersion(final @NotNull String modId) {
        final Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
        if (modContainer.isPresent()) {
            return modContainer.get().getMetadata().getVersion().getFriendlyString();
        }
        throw new IllegalArgumentException("Cannot get the version for the given mod id: " + modId);
    }

    /**
     * Gets the {@link RadarListManager} instance.
     *
     * @return Returns the radar list manager instance.
     */
    public static @NotNull RadarListManager getListManager() {
        return listManager;
    }

    /**
     * Gets the mod id.
     *
     * @return Returns the mod id.
     */
    public static @NotNull String getModId() {
        return MOD_ID;
    }

    /**
     * Gets the version.
     *
     * @return Returns the version.
     */
    public static @NotNull String getVersion() {
        return VERSION;
    }
}
