package net.crypticverse.serverhelper.mixin.dimensions;

import net.crypticverse.serverhelper.config.dimension.DimensionConfig;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderEyeItem.class)
public abstract class EnderEyeItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void serverhelper$gateEndEyeUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        // Only block Eye use when The End is explicitly disabled.
        if (DimensionConfig.isTheEndDisabled()) {
            // FAIL prevents interaction (including frames) when The End is disabled.
            cir.setReturnValue(InteractionResult.FAIL);
        }
        // If The End is enabled, do nothing here; let vanilla handle inserting the Eye.
    }
}
