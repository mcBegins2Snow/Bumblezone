package com.telepathicgrunt.bumblezone.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.MendingEnchantment;
import net.minecraft.item.ItemStack;
import com.telepathicgrunt.bumblezone.items.BzItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class HoneyShieldEnchantmentMixin {

    @Inject(method = "canApply",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void isHoneyCrystalShield(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(stack.getItem() == BzItems.HONEY_CRYSTAL_SHIELD && ((Enchantment)(Object)this) instanceof MendingEnchantment)
            cir.setReturnValue(false);
    }
}