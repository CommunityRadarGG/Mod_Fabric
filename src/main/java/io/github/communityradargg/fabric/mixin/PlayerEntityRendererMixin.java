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

import com.llamalad7.mixinextras.sugar.Local;
import io.github.communityradargg.fabric.accessors.PlayerEntityRenderStateAccessor;
import io.github.communityradargg.fabric.utils.Utils;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;

/**
 * An abstract Mixin class for {@link PlayerEntityRenderer}.
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    /**
     * Modifies the player name tag. This gets called once every tick with the original non-modified prefix.
     *
     * @param text The original text to modify.
     * @param playerEntityRenderState The needed local variable of the player entity render state.
     * @return Returns the modified local variable.
     */
    @ModifyArg(
            method = "renderLabelIfPresent(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitLabel(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/text/Text;ZIDLnet/minecraft/client/render/state/CameraRenderState;)V",
                    ordinal = 1
            ),
            index = 2
    )
    private Text modifyPlayerNameTag(final Text text, final @Local(index = 1, argsOnly = true) PlayerEntityRenderState playerEntityRenderState) {
        final UUID uuid = ((PlayerEntityRenderStateAccessor) playerEntityRenderState).communityradar_fabric$getPlayerUuid();

        if (uuid == null || !Utils.isOnGrieferGames()) {
            return text;
        }
        return Utils.includePrefixText(uuid, text);
    }

    /**
     * Modifies the player entity render state to set the self added uuid field.
     *
     * @param playerLikeEntity The player like entity as the source for the uuid.
     * @param playerEntityRenderState The player entity render state to set the uuid.
     * @param f The float f.
     * @param ci The callback info.
     */
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At(value = "TAIL"))
    private void modifyUpdateRenderState(final PlayerLikeEntity playerLikeEntity, final PlayerEntityRenderState playerEntityRenderState, final float f, final CallbackInfo ci) {
        ((PlayerEntityRenderStateAccessor) playerEntityRenderState).communityradar_fabric$setPlayerUuid(playerLikeEntity.getUuid());
    }
}
