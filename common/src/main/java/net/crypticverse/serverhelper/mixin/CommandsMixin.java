package net.crypticverse.serverhelper.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.crypticverse.serverhelper.ServerHelper;
import net.crypticverse.serverhelper.command.DimensionCommand;
import net.crypticverse.serverhelper.command.FilterMessageCommand;
import net.crypticverse.serverhelper.command.MutePlayerCommand;
import net.crypticverse.serverhelper.command.RankCommand;
import net.crypticverse.serverhelper.config.filter.FilterConfig;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.regex.Pattern;

@Mixin(Commands.class)
public class CommandsMixin {
    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V", remap = false))
    private void injectCommands(Commands.CommandSelection selection, CommandBuildContext context, CallbackInfo info) {
        FilterMessageCommand.register(dispatcher);
        MutePlayerCommand.register(dispatcher);
        RankCommand.register(dispatcher);
        DimensionCommand.register(dispatcher);
    }

    @Inject(method = "performCommand", at = @At("HEAD"), cancellable = true)
    private void onPerformCommand(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo info) {
        CommandSourceStack source = parseResults.getContext().getSource();
        command = command.replaceFirst(Pattern.quote("/"), "");

        if (command.startsWith("say") || command.startsWith("me") || (!FilterConfig.ignorePrivateMessages && (command.startsWith("whisper") || command.startsWith("tell") || command.startsWith("msg") || command.startsWith("w")))) {
            String string = command.replaceFirst("say ", "");
            string = string.replaceFirst("me ", "");
            string = string.replaceFirst("w ", "");
            string = string.replaceFirst("tell ", "");
            string = string.replaceFirst("msg ", "");
            string = string.replaceFirst("w ", "");

            ServerPlayer sender = source.getPlayer();
            if (sender == null || FilterConfig.isIgnored(sender.getUUID())) return;
            if (FilterConfig.isMuted(sender.getUUID())
                    || (!FilterConfig.censorAndSend && (FilterConfig.checkWords(string)
                    || FilterConfig.checkPhrases(string)
                    || FilterConfig.checkRegexes(string)
                    || FilterConfig.checkStandAloneWords(string)))) {
                info.cancel();
                if (FilterConfig.logFiltered) {
                    ServerHelper.LOGGER.info(Component.nullToEmpty("Filtered message from: " + sender.getDisplayName() + " (" + sender.getUUID() + "):" + string).getString());
                }
                if (FilterConfig.tellPlayer && FilterConfig.isTempMuted(sender.getUUID())) {
                    sender.sendSystemMessage(Component.nullToEmpty("You are muted for " + (FilterConfig.timeLeftTempMuted(sender.getUUID()) / 60000) + ". Reason: " + FilterConfig.getMuteReason(sender.getUUID())));
                } else if (FilterConfig.tellPlayer && FilterConfig.isMuted(sender.getUUID())) {
                    sender.sendSystemMessage(Component.nullToEmpty("You are muted. " + "Reason: " + FilterConfig.getMuteReason(sender.getUUID())));
                } else if (FilterConfig.tellPlayer) {
                    sender.sendSystemMessage(Component.nullToEmpty("Your message was filtered by the server. Please refrain from using that language."));

                    if (FilterConfig.muteAfterOffense) {
                        FilterConfig.addOffense(sender.getUUID());
                        if (FilterConfig.offenseCount(sender.getUUID()) >= FilterConfig.muteAfterOffenseNumber) {
                            if (FilterConfig.muteAfterOffenseType == FilterConfig.MuteType.PERMANENT) {
                                FilterConfig.addMutedPlayer(sender.getUUID(), Component.nullToEmpty("Repeated offenses").getString());
                                sender.sendSystemMessage(Component.nullToEmpty("You have been permanently muted for repeated offenses"));
                            } else {
                                FilterConfig.addTempMutedPlayer(sender.getUUID(), System.currentTimeMillis() + (FilterConfig.muteAfterOffenseMinutes * 60000L), Component.nullToEmpty("Repeated offenses").getString());
                                sender.sendSystemMessage(Component.nullToEmpty("You have been temporarily muted for " + FilterConfig.muteAfterOffenseMinutes + " minutes due to repeated offenses"));
                            }
                            FilterConfig.removeOffenses(sender.getUUID());
                        }
                    }
                }
            }
        }
    }

    @ModifyVariable(method = "performCommand", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private ParseResults<CommandSourceStack> onPerformCommands(ParseResults<CommandSourceStack> parseResults,  ParseResults<CommandSourceStack> p, String command) {
        CommandSourceStack source = parseResults.getContext().getSource();
        command = command.replaceFirst(Pattern.quote("/"), "");
        if(command.startsWith("say") || command.startsWith("me") || (!FilterConfig.ignorePrivateMessages && (command.startsWith("whisper") || command.startsWith("tell") || command.startsWith("msg") || command.startsWith("w")))) {
            String string = command.replaceFirst("say ", "");
            string = string.replaceFirst("me ", "");
            string = string.replaceFirst("w ", "");
            string = string.replaceFirst("tell ", "");
            string = string.replaceFirst("msg ", "");
            string = string.replaceFirst("w ", "");
            ServerPlayer sender = source.getPlayer();
            if (sender == null || FilterConfig.isIgnored(sender.getUUID())) return parseResults;
            if (!FilterConfig.isMuted(sender.getUUID()) && FilterConfig.censorAndSend) {
                String newMessage = FilterConfig.censorWords(string);
                newMessage = FilterConfig.censorPhrases(newMessage);
                newMessage = FilterConfig.censorRegexes(newMessage);
                newMessage = FilterConfig.censorStandAloneWords(newMessage);
                newMessage = FilterConfig.censorWords(newMessage);
                if (!newMessage.equals(string)) {
                    if(FilterConfig.muteAfterOffense) {
                        FilterConfig.addOffense(sender.getUUID());
                        if (FilterConfig.offenseCount(sender.getUUID()) >= FilterConfig.muteAfterOffenseNumber) {
                            if (FilterConfig.muteAfterOffenseType == FilterConfig.MuteType.PERMANENT) {
                                FilterConfig.addMutedPlayer(sender.getUUID(), Component.nullToEmpty("Repeated offenses").getString());
                                sender.sendSystemMessage(Component.nullToEmpty("You have been permanently muted for repeated offenses"));
                            } else {
                                FilterConfig.addTempMutedPlayer(sender.getUUID(), System.currentTimeMillis() + (FilterConfig.muteAfterOffenseMinutes * 60000L), Component.nullToEmpty("Repeated offenses").getString());
                                sender.sendSystemMessage(Component.nullToEmpty("You have been temporarily muted for " + FilterConfig.muteAfterOffenseMinutes + " minutes due to repeated offenses"));
                            }
                            FilterConfig.removeOffenses(sender.getUUID());
                        }
                    }
                    return dispatcher.parse(command.split(" ")[0] + " " +  newMessage, source);
                }
            }
        }
        return parseResults;
    }


    @ModifyVariable(method = "performCommand", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private String onPerformCommand(String command, ParseResults<CommandSourceStack> parseResults, String c) {
        CommandSourceStack source = parseResults.getContext().getSource();
        String newCommand = command.replaceFirst(Pattern.quote("/"), "");

        if(newCommand.startsWith("say") || newCommand.startsWith("me") || (!FilterConfig.ignorePrivateMessages && (newCommand.startsWith("whisper") || newCommand.startsWith("tell") || newCommand.startsWith("msg") || newCommand.startsWith("w")))) {

            String string = newCommand.replaceFirst("say ", "");
            string = string.replaceFirst("me ", "");
            string = string.replaceFirst("w ", "");
            string = string.replaceFirst("tell ", "");
            string = string.replaceFirst("msg ", "");
            string = string.replaceFirst("w ", "");

            ServerPlayer sender = source.getPlayer();
            if (sender == null || FilterConfig.isIgnored(sender.getUUID())) return command;
            if (!FilterConfig.isMuted(sender.getUUID()) && FilterConfig.censorAndSend) {
                String newMessage = FilterConfig.censorWords(string);
                newMessage = FilterConfig.censorPhrases(newMessage);
                newMessage = FilterConfig.censorRegexes(newMessage);
                newMessage = FilterConfig.censorStandAloneWords(newMessage);
                newMessage = FilterConfig.censorWords(newMessage);
                if (!newMessage.equals(string)) {

                    if(FilterConfig.tellPlayer) sender.sendSystemMessage(Component.nullToEmpty("Your message was censored by the server"));
                    if (FilterConfig.logFiltered)
                        ServerHelper.LOGGER.info(Component.nullToEmpty("Censored message from ").getString() + Objects.requireNonNull(sender.getDisplayName()).getString() + " (" + sender.getUUID() + "): " + string);
                    if(FilterConfig.muteAfterOffense) {
                        FilterConfig.addOffense(sender.getUUID());
                        if (FilterConfig.offenseCount(sender.getUUID()) >= FilterConfig.muteAfterOffenseNumber) {
                            if (FilterConfig.muteAfterOffenseType == FilterConfig.MuteType.PERMANENT) {
                                FilterConfig.addMutedPlayer(sender.getUUID(), Component.nullToEmpty("Repeated offenses").getString());

                                sender.sendSystemMessage(Component.nullToEmpty("You have been permanently muted for repeated offenses"));
                            } else {
                                FilterConfig.addTempMutedPlayer(sender.getUUID(), System.currentTimeMillis() + (FilterConfig.muteAfterOffenseMinutes * 60000L), Component.nullToEmpty("Repeated offenses").getString());
                                sender.sendSystemMessage(Component.nullToEmpty("You have been temporarily muted for " + FilterConfig.muteAfterOffenseMinutes + " minutes due to repeated offenses"));
                            }
                            FilterConfig.removeOffenses(sender.getUUID());
                        }
                    }
                    return command.split(" ")[0] + " " +  newMessage;
                }
            }
        }
        return command;
    }

}
