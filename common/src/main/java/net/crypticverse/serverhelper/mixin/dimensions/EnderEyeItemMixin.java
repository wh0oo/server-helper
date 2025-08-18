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
    public void onUseItem(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!DimensionConfig.isTheEndDisabled()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
