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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.communityradargg.fabric.utils.Utils;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * An abstract Mixin class for {@link PlayerTabOverlay}.
 */
@Mixin(PlayerTabOverlay.class)
public abstract class PlayerListHudMixin {
    /**
     * Modifies the player info component. This gets called when the player info gets updated.
     *
     * @param component The original chat message component to modify.
     * @param playerInfo The needed local variable of the player info.
     * @return Returns the modified local variable.
     */
    @ModifyReturnValue(method = "getNameForDisplay", at = @At("RETURN"))
    private Component modifyGetPlayerName(final Component component, final @Local(argsOnly = true) PlayerInfo playerInfo) {
        if (!Utils.isOnGrieferGames()) {
            return component;
        }
        return Utils.includePrefixText(playerInfo.getProfile().id(), component);
    }
}
