package com.telepathicgrunt.the_bumblezone.mixin.items;

import com.telepathicgrunt.the_bumblezone.items.GlassBottleBehavior;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(GlassBottleItem.class)
public class GlassBottleUseMixin {
    //using glass bottle to get honey could anger bees
    @Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
            at = @At(value = "INVOKE", target = "net/minecraft/world/World.getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true)
    private void thebumblezone_bottleFluidInteract(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult<ItemStack>> cir, List<AreaEffectCloudEntity> list, ItemStack itemStack, RayTraceResult hitResult, BlockPos blockPos) {
        if (GlassBottleBehavior.useBottleOnSugarWater(world, user, hand, blockPos))
            cir.setReturnValue(ActionResult.success(user.getItemInHand(hand)));
        if (GlassBottleBehavior.useBottleOnHoneyFluid(world, user, hand, blockPos))
            cir.setReturnValue(ActionResult.success(user.getItemInHand(hand)));
    }
}