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
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * An abstract Mixin class for {@link PlayerListHudMixin}.
 */
@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
    /**
     * Modifies the player player-list entry text. This gets called when the player-list entry gets updated.
     *
     * @param text The original chat message text to modify.
     * @param entry The needed local variable of the player list entry.
     * @return Returns the modified local variable.
     */
    @ModifyReturnValue(method = "getPlayerName", at = @At("RETURN"))
    private Text modifyGetPlayerName(final Text text, final @Local(argsOnly = true) PlayerListEntry entry) {
        if (!Utils.isOnGrieferGames()) {
            return text;
        }
        return Utils.includePrefixText(entry.getProfile().getId(), text);
    }
}
