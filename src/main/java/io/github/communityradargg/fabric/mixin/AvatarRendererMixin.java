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

import io.github.communityradargg.fabric.accessors.AvatarRenderStateAccessor;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * An abstract Mixin class for {@link AvatarRenderer}.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    /**
     * Modifies the player entity render state to set the self added uuid field.
     *
     * @param avatar The avatar as the source for the uuid.
     * @param avatarRenderState The avatar render state to set the uuid.
     * @param f The float f.
     * @param ci The callback info.
     */
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At(value = "TAIL"))
    private void modifyExtractRenderState(final Avatar avatar, final AvatarRenderState avatarRenderState, final float f, final CallbackInfo ci) {
        ((AvatarRenderStateAccessor) avatarRenderState).communityradar_fabric$setPlayerUuid(avatar.getUUID());
    }
}
