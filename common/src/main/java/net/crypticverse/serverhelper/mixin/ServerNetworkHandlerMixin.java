package net.crypticverse.serverhelper.mixin;

import net.crypticverse.serverhelper.ServerHelper;
import net.crypticverse.serverhelper.config.filter.FilterConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 500)
public class ServerNetworkHandlerMixin {
    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    private void onHandleDecoratedMessage(PlayerChatMessage message, CallbackInfo ci) {
        ServerPlayer sender = ((ServerGamePacketListenerImpl) (Object) this).player;

        if(sender == null || FilterConfig.isIgnored(sender.getUUID())) return;
        String string = message.decoratedContent().getString();
        if(FilterConfig.isMuted(sender.getUUID())
                || (!FilterConfig.censorAndSend && (FilterConfig.checkWords(string)
                || FilterConfig.checkPhrases(string)
                || FilterConfig.checkRegexes(string)
                || FilterConfig.checkStandAloneWords(string)))) {
            ci.cancel();
            if(FilterConfig.logFiltered) {
                ServerHelper.LOGGER.info(Component.nullToEmpty("Filtered message from: " + sender.getDisplayName() + " (" + sender.getUUID() + "): " + message).getString());
            }
            if(FilterConfig.tellPlayer && FilterConfig.isTempMuted(sender.getUUID())) {
                sender.sendSystemMessage(Component.nullToEmpty("You are muted for " + (FilterConfig.timeLeftTempMuted(sender.getUUID()) / 60000) + " more minutes. Reason: " + FilterConfig.getMuteReason(sender.getUUID())));
            } else if(FilterConfig.tellPlayer && FilterConfig.isMuted(sender.getUUID())) {
                sender.sendSystemMessage(Component.nullToEmpty("You are muted. Reason: " + FilterConfig.getMuteReason(sender.getUUID())));
            } else if(FilterConfig.tellPlayer) {
                sender.sendSystemMessage(Component.nullToEmpty("Your message was filtered by the server. Please do not use that language!"));

                if(FilterConfig.muteAfterOffense) {
                    FilterConfig.addOffense(sender.getUUID());
                    if (FilterConfig.offenseCount(sender.getUUID()) >= FilterConfig.muteAfterOffenseNumber) {
                        if (FilterConfig.muteAfterOffenseType == FilterConfig.MuteType.PERMANENT) {
                            FilterConfig.addMutedPlayer(sender.getUUID(), Component.nullToEmpty("Repeated offenses").getString());
                            sender.sendSystemMessage(Component.nullToEmpty("You have been permanently muted for repeated offenses"));
                        } else {
                            FilterConfig.addTempMutedPlayer(sender.getUUID(), System.currentTimeMillis() + (FilterConfig.muteAfterOffenseMinutes * 60000L), Component.nullToEmpty("Repeated offenses.").getString());
                            sender.sendSystemMessage(Component.nullToEmpty("You have been temporarily muted for" + FilterConfig.muteAfterOffenseMinutes + " minutes due to repeated offenses"));
                        }
                        FilterConfig.removeOffenses(sender.getUUID());
                    }
                }
            }
        }
    }

    @ModifyVariable(method = "broadcastChatMessage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private PlayerChatMessage onHandleDecoratedMessage(PlayerChatMessage message, PlayerChatMessage m) {
        ServerPlayer sender = ((ServerGamePacketListenerImpl) (Object) this).player;
        if(FilterConfig.ignorePrivateMessages || FilterConfig.isIgnored(sender.getUUID())) return message;
        if(!FilterConfig.isMuted(sender.getUUID()) && FilterConfig.censorAndSend) {
            String newMessage = FilterConfig.censorWords(message.decoratedContent().getString());
            newMessage = FilterConfig.censorPhrases(newMessage);
            newMessage = FilterConfig.censorRegexes(newMessage);
            newMessage = FilterConfig.censorStandAloneWords(newMessage);
            newMessage = FilterConfig.censorWords(newMessage);
            if(!newMessage.equals(message.decoratedContent().getString())) {
                if(FilterConfig.tellPlayer) sender.sendSystemMessage(Component.nullToEmpty("Your message was censored by the server"));
                if(FilterConfig.logFiltered) ServerHelper.LOGGER.info(Component.nullToEmpty("Censored message from ").getString() +  Objects.requireNonNull(sender.getDisplayName()).getString() + " (" + sender.getUUID() + ")" + ": " + message.decoratedContent().getString());

                if(FilterConfig.muteAfterOffense) {
                    FilterConfig.addOffense(sender.getUUID());
                    if (FilterConfig.offenseCount(sender.getUUID()) >= FilterConfig.muteAfterOffenseNumber) {
                        if (FilterConfig.muteAfterOffenseType == FilterConfig.MuteType.PERMANENT) {
                            FilterConfig.addMutedPlayer(sender.getUUID(), Component.nullToEmpty("Repeated offenses").getString());
                            sender.sendSystemMessage(Component.nullToEmpty("You have been permanently muted for repeated offenses"));
                        } else {
                            FilterConfig.addTempMutedPlayer(sender.getUUID(), System.currentTimeMillis() + (FilterConfig.muteAfterOffenseMinutes * 60000L), Component.nullToEmpty("Repeated offenses").getString());
                            sender.sendSystemMessage(Component.nullToEmpty("You have been temporarily muted for" + FilterConfig.muteAfterOffenseMinutes + " minutes due to repeated offenses"));
                        }
                        FilterConfig.removeOffenses(sender.getUUID());
                    }
                }
                return PlayerChatMessage.system(newMessage);
            }
        }
        return message;
    }
}
