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
package io.github.communityradargg.fabric.accessors;

import java.util.UUID;

/**
 * Serves as accessor for the self added field in the {@link net.minecraft.client.renderer.entity.player.AvatarRenderer} class.
 */
public interface AvatarRenderStateAccessor {
    /**
     * Gets the player uuid field value.
     *
     * @return Returns the player uuid field value.
     */
    UUID communityradar_fabric$getPlayerUuid();

    /**
     * Sets the player uuid field value.
     *
     * @param uuid The player uuid.
     */
    void communityradar_fabric$setPlayerUuid(final UUID uuid);
}
