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

import io.github.communityradargg.fabric.CommunityRadarMod;
import io.github.communityradargg.fabric.utils.Utils;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * An abstract Mixin class for {@link ChatHud}.
 */
@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Unique
    private static final Logger logger = LoggerFactory.getLogger(ChatHudMixin.class);

    /**
     * Modifies the player chat messages. This gets called when a message should be added to the player chat.
     *
     * @param text The original chat message text to modify.
     * @return Returns the modified local variable.
     */
    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "HEAD"), index = 1, argsOnly = true)
    private Text modifyChatMessages(final Text text) {
        if (!Utils.isOnGrieferGames()) {
            return text;
        }

        final String plainText = text.getString();
        if (plainText == null) {
            return text;
        }

        // On a chat message there should be never be the need to call to the Mojang API.
        try {
            final Optional<UUID> playerUuid = Utils.getChatMessagePlayer(plainText).get();

            if (playerUuid.isPresent() && CommunityRadarMod.getListManager().isInList(playerUuid.get())) {
                return Utils.includePrefixText(playerUuid.get(), text);
            }
        } catch (final ExecutionException | InterruptedException e) {
            logger.error("Could not get the player uuid in the message edit process", e);
        }
        return text;
    }
}
