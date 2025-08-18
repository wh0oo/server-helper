package net.crypticverse.serverhelper.mixin.ranks;

import net.crypticverse.serverhelper.config.ranks.RanksConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void getName(CallbackInfoReturnable<Component> info) {
        Player player = (Player) (Object) this;
        Component role = RanksConfig.getPlayersInRole(player.getUUID());
        Component modified = Component.literal("[").append(role).append("] ").append(info.getReturnValue());
        if (Objects.equals(role, Component.literal(""))) {
            modified = info.getReturnValue();
        }
        info.setReturnValue(modified);
    }
}
