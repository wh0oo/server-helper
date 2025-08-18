package net.crypticverse.serverhelper.mixin.dimensions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.crypticverse.serverhelper.config.dimension.DimensionConfig;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
    @WrapOperation(method = "onPlace", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
    public boolean onPlaceWrap(Optional<PortalShape> instance, Operation<Boolean> original) {
        return !DimensionConfig.isNetherDisabled() && instance.isPresent();
    }
}
