package net.crypticverse.serverhelper.mixin.ranks;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void getName(CallbackInfoReturnable<Component> info) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        info.setReturnValue(player.getDisplayName());
    }
}
