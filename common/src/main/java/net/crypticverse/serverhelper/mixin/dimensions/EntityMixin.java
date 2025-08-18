package net.crypticverse.serverhelper.mixin.dimensions;

import net.crypticverse.serverhelper.config.dimension.DimensionConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void init(TeleportTransition transition, CallbackInfoReturnable<Entity> cir) {
        if (DimensionConfig.isNetherDisabled() && transition.newLevel().dimension() == Level.NETHER) {
            cir.setReturnValue(null);
        } else if (DimensionConfig.isTheEndDisabled() && transition.newLevel().dimension() == Level.END) {
            cir.setReturnValue(null);
        }
    }
}
