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

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.communityradargg.fabric.accessors.AvatarRenderStateAccessor;
import io.github.communityradargg.fabric.utils.Utils;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;

/**
 * An abstract Mixin class for {@link EntityRenderer}.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    /**
     * Modifies the player name tag. This gets called once every tick with the original non-modified prefix.
     *
     * @param state The original state.
     * @param poseStack The original pose stack.
     * @param submitNodeCollector The original submit node collector.
     * @param camera The original camera.
     * @param offset The original offset.
     * @param ci The callback info.
     */
    @Inject(
            method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
            at = @At("HEAD")
    )
    private void modifySubmitNameDisplay(final EntityRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera, final int offset, CallbackInfo ci) {
        if (state.nameTag == null || !(state instanceof AvatarRenderState avatarRenderState)) {
            return;
        }

        final UUID uuid = ((AvatarRenderStateAccessor) avatarRenderState).communityradar_fabric$getPlayerUuid();
        if (uuid == null || !Utils.isOnGrieferGames()) {
            return;
        }

        state.nameTag = Utils.includePrefixComponent(uuid, state.nameTag);
    }
}
