package com.telepathicgrunt.the_bumblezone.items;

import com.telepathicgrunt.the_bumblezone.entities.nonliving.PollenPuffEntity;
import com.telepathicgrunt.the_bumblezone.modinit.BzItems;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PollenPuff extends Item {
    public PollenPuff(Item.Properties properties) {
        super(properties);

        DispenserBlock.registerBehavior(this, new ProjectileDispenseBehavior() {
            protected ProjectileEntity getProjectile(World world, IPosition position, ItemStack itemStack) {
                return Util.make(new PollenPuffEntity(
                        world,
                        position.x(),
                        position.y(),
                        position.z()),
                        (pollenPuffEntity) -> pollenPuffEntity.setItem(itemStack));
            }
        });
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity playerEntity, LivingEntity entity, Hand hand) {
        // No clientside exit early because if we return early, the use method runs on server and thus, throws pollen puff and depollinates bee at same time.
        if (!(entity instanceof BeeEntity)) return ActionResultType.PASS;

        BeeEntity beeEntity = (BeeEntity)entity;
        ItemStack itemstack = playerEntity.getItemInHand(hand);

        // right clicking on pollinated bee with pollen puff with room, gets pollen puff into hand.
        // else, if done with pollen puff without room, drops pollen puff in world
        if(beeEntity.hasNectar() && itemstack.getItem().equals(BzItems.POLLEN_PUFF.get())) {
            PollenPuff.spawnItemstackEntity(entity.level, beeEntity.blockPosition(), new ItemStack(BzItems.POLLEN_PUFF.get(), 1));
            playerEntity.swing(hand, true);
            beeEntity.dropOffNectar();
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        ItemStack itemstack = playerEntity.getItemInHand(hand);

        if (!world.isClientSide()) {
            world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
                PollenPuffEntity pollenPuffEntity = new PollenPuffEntity(world, playerEntity);
                pollenPuffEntity.setItem(itemstack);
                pollenPuffEntity.shootFromRotation(playerEntity, playerEntity.xRot, playerEntity.yRot, 0.0F, 1.5F, 1.0F);
                world.addFreshEntity(pollenPuffEntity);

            playerEntity.awardStat(Stats.ITEM_USED.get(this));
            if (!playerEntity.isCreative()) {
                itemstack.shrink(1);
            }
        }

        return ActionResult.sidedSuccess(itemstack, world.isClientSide());
    }


    public static void spawnItemstackEntity(World world, BlockPos blockPos, ItemStack itemStack) {
        if (!world.isClientSide() && !itemStack.isEmpty()) {
            double x = (double)(world.random.nextFloat() * 0.5F) + 0.25D;
            double y = (double)(world.random.nextFloat() * 0.5F) + 0.25D;
            double z = (double)(world.random.nextFloat() * 0.5F) + 0.25D;
            ItemEntity itemEntity = new ItemEntity(world, (double)blockPos.getX() + x, (double)blockPos.getY() + y, (double)blockPos.getZ() + z, itemStack);
            itemEntity.setDefaultPickUpDelay();
            world.addFreshEntity(itemEntity);
        }
    }
}