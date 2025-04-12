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
package io.github.communityradargg.fabric.mixin;

import io.github.communityradargg.fabric.accessors.PlayerEntityRenderStateAccessor;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import java.util.UUID;

/**
 * Mixin for the class {@link PlayerEntityRenderState}.
 */
@Mixin(PlayerEntityRenderState.class)
public class PlayerEntityRenderStateMixin implements PlayerEntityRenderStateAccessor {
    @Unique
    private UUID communityradar_fabric$playerUuid;

    @Override
    public UUID communityradar_fabric$getPlayerUuid() {
        return communityradar_fabric$playerUuid;
    }

    @Override
    public void communityradar_fabric$setPlayerUuid(final UUID playerUuid) {
        this.communityradar_fabric$playerUuid = playerUuid;
    }
}
