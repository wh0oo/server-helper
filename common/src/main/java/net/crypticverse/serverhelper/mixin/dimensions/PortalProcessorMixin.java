package net.crypticverse.serverhelper.mixin.dimensions;

import net.crypticverse.serverhelper.config.dimension.DimensionConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.Nullable;

@Mixin(PortalProcessor.class)
public abstract class PortalProcessorMixin {
    @Shadow
    @Nullable
    public abstract TeleportTransition getPortalDestination(ServerLevel level, Entity entity);

    @Inject(method = "processPortalTeleportation", at = @At("HEAD"), cancellable = true)
    private void init(ServerLevel level, Entity entity, boolean canChangeDimensions, CallbackInfoReturnable<Boolean> cir) {
        TeleportTransition newLevel = getPortalDestination(level, entity);
        if (DimensionConfig.isNetherDisabled() && newLevel.newLevel().dimension() == Level.NETHER) {
            cir.setReturnValue(false);
        } else if (DimensionConfig.isTheEndDisabled() && newLevel.newLevel().dimension() == Level.END) {
            cir.setReturnValue(false);
        }
    }
}
