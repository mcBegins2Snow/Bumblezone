package com.telepathicgrunt.the_bumblezone.fluids;

import com.telepathicgrunt.the_bumblezone.blocks.HoneyFluidBlock;
import com.telepathicgrunt.the_bumblezone.mixin.blocks.FlowingFluidAccessor;
import com.telepathicgrunt.the_bumblezone.modinit.BzFluids;
import com.telepathicgrunt.the_bumblezone.modinit.BzItems;
import com.telepathicgrunt.the_bumblezone.modinit.BzParticles;
import com.telepathicgrunt.the_bumblezone.tags.BzFluidTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectUtils;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import java.util.Random;

public abstract class HoneyFluid extends ForgeFlowingFluid {

    public static final IntegerProperty BOTTOM_LEVEL = HoneyFluidBlock.BOTTOM_LEVEL;
    public static final BooleanProperty ABOVE_FLUID = HoneyFluidBlock.ABOVE_FLUID;

    protected HoneyFluid(Properties properties) {
        super(properties);
    }

    @Override
    public Fluid getFlowing() {
        return BzFluids.HONEY_FLUID_FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return BzFluids.HONEY_FLUID.get();
    }

    @Override
    public Item getBucket() {
        return BzItems.HONEY_BUCKET.get();
    }

    @Override
    public void animateTick(World worldIn, BlockPos pos, FluidState state, Random random) {
        if (random.nextInt(82) == 0) {
            worldIn.addParticle(BzParticles.HONEY_PARTICLE.get(),
                    pos.getX() + random.nextFloat(),
                    pos.getY() + random.nextFloat(),
                    pos.getZ() + random.nextFloat(),
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }

    @Override
    public IParticleData getDripParticle() {
        return BzParticles.HONEY_PARTICLE.get();
    }

    @Override
    protected float getExplosionResistance() {
        return 120.0F;
    }

    @Override
    protected void beforeDestroyingBlock(IWorld world, BlockPos pos, BlockState state) {
        TileEntity blockEntity = state.getBlock().isEntityBlock() ? world.getBlockEntity(pos) : null;
        Block.dropResources(state, world, pos, blockEntity);
    }

    @Override
    public int getSlopeFindDistance(IWorldReader world) {
        return 4;
    }

    @Override
    public int getDropOff(IWorldReader world) {
        return 1;
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid.is(BzFluidTags.HONEY_FLUID);
    }

    @Override
    public boolean canBeReplacedWith(FluidState state, IBlockReader world, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.is(FluidTags.WATER);
    }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return BzFluids.HONEY_FLUID_BLOCK.get().defaultBlockState()
                .setValue(FlowingFluidBlock.LEVEL, state.isSource() ? 0 : state.getAmount())
                .setValue(BOTTOM_LEVEL, state.isSource() ? 0 : state.getValue(BOTTOM_LEVEL))
                .setValue(FALLING, !state.isSource() && state.getValue(FALLING))
                .setValue(ABOVE_FLUID, state.getValue(ABOVE_FLUID));
    }

    @Override
    public void tick(World world, BlockPos blockPos, FluidState fluidState) {
        boolean justFilledBottom = false;
        // removes self if not source and is not fed.
        // otherwise, schedule fluid tick and update flow.
        if (!fluidState.isSource()) {
            FluidState newFluidState = this.getNewLiquid(world, blockPos, world.getBlockState(blockPos));
            int spreadDelay = this.getSpreadDelay(world, blockPos, fluidState, newFluidState);
            if (newFluidState.isEmpty()) {
                fluidState = newFluidState;
                world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
            }
            else if (!newFluidState.equals(fluidState)) {
                if(fluidState.getValue(BOTTOM_LEVEL) != 0 && (newFluidState.isSource() || newFluidState.getValue(BOTTOM_LEVEL) == 0))
                    justFilledBottom = true;

                fluidState = newFluidState;
                BlockState blockstate = newFluidState.createLegacyBlock();
                world.setBlock(blockPos, blockstate, 2);
                world.getLiquidTicks().scheduleTick(blockPos, newFluidState.getType(), spreadDelay);
                world.updateNeighborsAt(blockPos, blockstate.getBlock());
            }
        }

        // For spreading downward and to the side.
        // Is basically the spread method but with justFilledBottom boolean
        // used so new fluid is not made in same tick as when fluid just reached bottom layer = 0.
        if (!fluidState.isEmpty()) {
            int bottomFluidLevel = fluidState.isSource() ? 0 : fluidState.getValue(BOTTOM_LEVEL);
            if(bottomFluidLevel == 0) {
                BlockState blockState = world.getBlockState(blockPos);
                BlockPos belowBlockPos = blockPos.below();
                BlockState belowBlockState = world.getBlockState(belowBlockPos);
                FluidState belowFluidState = this.getNewLiquid(world, belowBlockPos, belowBlockState);
                if (!belowBlockState.getFluidState().is(BzFluidTags.HONEY_FLUID) &&
                    this.canSpreadTo(world, blockPos, blockState, Direction.DOWN, belowBlockPos, belowBlockState, world.getFluidState(belowBlockPos), belowFluidState.getType())) {

                    if(!justFilledBottom) {
                        this.spreadDown(world, belowBlockPos, belowBlockState, Direction.DOWN, belowFluidState);
                        if (((FlowingFluidAccessor)this).thebumblezone_callSourceNeighborCount(world, blockPos) >= 3) {
                            ((FlowingFluidAccessor)this).thebumblezone_callSpreadToSides(world, blockPos, fluidState, blockState);
                        }
                    }
                }
                else if (fluidState.isSource() || !belowBlockState.getFluidState().getType().isSame(this)) {
                    ((FlowingFluidAccessor)this).thebumblezone_callSpreadToSides(world, blockPos, fluidState, blockState);
                }
            }
        }
    }

    @Override
    protected void spread(IWorld world, BlockPos blockPos, FluidState fluidState) {
        if (!fluidState.isEmpty()) {
            int bottomFluidLevel = fluidState.getValue(BOTTOM_LEVEL);
            if(bottomFluidLevel == 0) {
                BlockState blockState = world.getBlockState(blockPos);
                BlockPos belowBlockPos = blockPos.below();
                BlockState belowBlockState = world.getBlockState(belowBlockPos);
                FluidState belowFluidState = this.getNewLiquid(world, belowBlockPos, belowBlockState);
                if (!belowBlockState.getFluidState().is(BzFluidTags.HONEY_FLUID) && this.canSpreadTo(world, blockPos, blockState, Direction.DOWN, belowBlockPos, belowBlockState, world.getFluidState(belowBlockPos), belowFluidState.getType())) {
                    this.spreadDown(world, belowBlockPos, belowBlockState, Direction.DOWN, belowFluidState);
                    if (((FlowingFluidAccessor)this).thebumblezone_callSourceNeighborCount(world, blockPos) >= 3) {
                        ((FlowingFluidAccessor)this).thebumblezone_callSpreadToSides(world, blockPos, fluidState, blockState);
                    }
                }
                else if (fluidState.isSource() || !belowBlockState.getFluidState().getType().isSame(this)) {
                    ((FlowingFluidAccessor)this).thebumblezone_callSpreadToSides(world, blockPos, fluidState, blockState);
                }
            }
        }
    }

    protected void spreadDown(IWorld world, BlockPos blockPos, BlockState blockState, Direction direction, FluidState fluidState) {
        if (!blockState.isAir()) {
            this.beforeDestroyingBlock(world, blockPos, blockState);
        }
        world.setBlock(blockPos, fluidState.createLegacyBlock(), 3);
    }

    @Override
    protected FluidState getNewLiquid(IWorldReader worldReader, BlockPos blockPos, BlockState blockState) {
        int lowestNeighboringFluidLevel = blockState.getBlock() instanceof HoneyFluidBlock ? blockState.getValue(BOTTOM_LEVEL) : HoneyFluidBlock.maxBottomLayer;
        int currentFluidLevel = blockState.getBlock() instanceof HoneyFluidBlock ? blockState.getValue(HoneyFluidBlock.LEVEL) : 0;
        int highestNeighboringFluidLevel = currentFluidLevel;
        int neighboringFluidSource = 0;
        boolean hadAboveFluid = blockState.getBlock() instanceof HoneyFluidBlock ? blockState.getValue(ABOVE_FLUID) : false;

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos sideBlockPos = blockPos.relative(direction);
            BlockState sideBlockState = worldReader.getBlockState(sideBlockPos);
            FluidState sideFluidState = sideBlockState.getFluidState();
            if (sideFluidState.getType().isSame(this) && ((FlowingFluidAccessor)this).thebumblezone_callCanPassThroughWall(direction, worldReader, blockPos, blockState, sideBlockPos, sideBlockState)) {
                if (sideFluidState.isSource() && net.minecraftforge.event.ForgeEventFactory.canCreateFluidSource(worldReader, sideBlockPos, sideBlockState, this.canConvertToSource())) {
                    ++neighboringFluidSource;
                }

                highestNeighboringFluidLevel = Math.max(highestNeighboringFluidLevel, sideFluidState.getAmount());
                if(sideFluidState.getType() instanceof HoneyFluid){
                    lowestNeighboringFluidLevel = Math.min(lowestNeighboringFluidLevel, sideFluidState.isSource() ? 0 : sideFluidState.getValue(BOTTOM_LEVEL));
                }
            }
        }

        BlockPos aboveBlockPos = blockPos.above();
        BlockState aboveBlockState = worldReader.getBlockState(aboveBlockPos);
        FluidState aboveFluidState = aboveBlockState.getFluidState();
        boolean aboveFluidIsThisFluid = !aboveFluidState.isEmpty() && aboveFluidState.getType().isSame(this);
        int newBottomFluidLevel = Math.max(lowestNeighboringFluidLevel - 1, 0);
        boolean isFalling = true;
        int newFluidLevel = 8;
        int dropOffValue = this.getDropOff(worldReader);
        if(hadAboveFluid && !aboveFluidIsThisFluid) {
            dropOffValue = 0;
        }

        if (aboveFluidIsThisFluid && ((FlowingFluidAccessor)this).thebumblezone_callCanPassThroughWall(Direction.UP, worldReader, blockPos, blockState, aboveBlockPos, aboveBlockState)) {
            if(!aboveFluidState.isSource() && aboveFluidState.getValue(BOTTOM_LEVEL) != 0) {
                newFluidLevel = highestNeighboringFluidLevel - dropOffValue;
            }
        }
        else {
            BlockState belowBlockState = worldReader.getBlockState(blockPos.below());
            isFalling = aboveFluidState.isEmpty() && neighboringFluidSource == 0 && highestNeighboringFluidLevel <= currentFluidLevel && ((FlowingFluidAccessor)this).thebumblezone_callCanPassThroughWall(Direction.DOWN, worldReader, blockPos, blockState, blockPos.below(), belowBlockState);
            newFluidLevel = highestNeighboringFluidLevel - dropOffValue;
        }

        return newFluidLevel <= 0 ?
                Fluids.EMPTY.defaultFluidState() :
                this.getFlowing(newFluidLevel, isFalling)
                        .setValue(BOTTOM_LEVEL, newBottomFluidLevel)
                        .setValue(ABOVE_FLUID, aboveFluidIsThisFluid && (aboveFluidState.isSource() || aboveFluidState.getValue(BOTTOM_LEVEL) == 0));
    }

    @Override
    public float getHeight(FluidState fluidState, IBlockReader world, BlockPos blockPos) {
        BlockPos aboveBlockPos = blockPos.above();
        BlockState aboveBlockState = world.getBlockState(aboveBlockPos);
        FluidState aboveFluidState = aboveBlockState.getFluidState();
        boolean aboveFluidIsThisFluid =
                    !aboveFluidState.isEmpty() &&
                    aboveFluidState.getType().isSame(this) &&
                    (aboveFluidState.isSource() || aboveFluidState.getValue(BOTTOM_LEVEL) == 0);

        return fluidState.getValue(ABOVE_FLUID) || aboveFluidIsThisFluid ? 1.0f : fluidState.getOwnHeight();
    }

    public static float getHoneyFluidHeight(IBlockReader world, BlockPos blockPos, Fluid fluid) {
        float totalHeight = 0.0F;
        int checkedSides = 0;
        int fluidSides = 0;
        FluidState currentMatchingFluidState = null;

        // Checks in a square. One spot will be the current fluid
        for(int xOffset = -2; xOffset <= 1; xOffset++) {
            for(int zOffset = -2; zOffset <= 1; zOffset++) {
                BlockPos currentBlockPos = blockPos.offset(xOffset, 0, zOffset);

                if(xOffset == -2 || zOffset == -2 || xOffset == 1 || zOffset == 1) {
                    if (world.getFluidState(currentBlockPos).getType().isSame(fluid)) {
                        fluidSides++;
                    }
                    continue;
                }

                FluidState aboveFluidState = world.getFluidState(currentBlockPos.above());
                if (aboveFluidState.getType().isSame(fluid) && (aboveFluidState.isSource() || aboveFluidState.getValue(BOTTOM_LEVEL) == 0)) {
                    return 1.0F;
                }

                FluidState currentFluidState = world.getFluidState(currentBlockPos);
                if (currentFluidState.getType().isSame(fluid)) {
                    currentMatchingFluidState = currentFluidState;
                    fluidSides++;
                    float fluidStateHeight = currentFluidState.getHeight(world, currentBlockPos);
                    if (fluidStateHeight >= 0.8F) {
                        totalHeight += fluidStateHeight * 10.0F;
                        checkedSides += 10;
                    } else {
                        totalHeight += fluidStateHeight;
                        checkedSides++;
                    }
                } else if (!world.getBlockState(currentBlockPos).getMaterial().isSolid()) {
                    checkedSides++;
                }
            }
        }

        if(fluidSides == 1 &&
            currentMatchingFluidState.getType().isSame(fluid) &&
            !currentMatchingFluidState.isSource() &&
            currentMatchingFluidState.getValue(FALLING))
        {
            return currentMatchingFluidState.getHeight(world, blockPos);
        }

        return totalHeight / (float)checkedSides;
    }

    public static boolean shouldNotCullSide(IBlockReader world, BlockPos blockPos, Direction direction, FluidState currentFluidState) {
        if(direction == Direction.UP) {
            FluidState aboveFluidState = world.getBlockState(blockPos.above()).getFluidState();
            return aboveFluidState.is(BzFluidTags.BZ_HONEY_FLUID) && !aboveFluidState.isSource() &&
                    (aboveFluidState.getValue(BOTTOM_LEVEL) != 0 || currentFluidState.getAmount() != 8);
        }
        else if(direction == Direction.DOWN) {
            FluidState belowFluidState = world.getBlockState(blockPos.below()).getFluidState();
            return belowFluidState.is(BzFluidTags.BZ_HONEY_FLUID) && !currentFluidState.isSource() &&
                    (belowFluidState.getAmount() != 8 || currentFluidState.getValue(BOTTOM_LEVEL) != 0);
        }
        else {
            FluidState sideFluidState = world.getBlockState(blockPos.relative(direction)).getFluidState();
            if(sideFluidState.is(BzFluidTags.BZ_HONEY_FLUID)) {
                int bottomLayerCurrent = currentFluidState.isSource() ? 0 : currentFluidState.getValue(BOTTOM_LEVEL);
                int bottomLayerSide = sideFluidState.isSource() ? 0 : sideFluidState.getValue(BOTTOM_LEVEL);
                return bottomLayerCurrent < bottomLayerSide;
            }
        }

        return false;
    }

    public static void breathing(LivingEntity thisEntity) {
        boolean invulnerable = thisEntity instanceof PlayerEntity && ((PlayerEntity)thisEntity).abilities.invulnerable;
        if (thisEntity.isAlive()) {
            if (thisEntity.isEyeInFluid(BzFluidTags.BZ_HONEY_FLUID)) {
                if (!thisEntity.canBreatheUnderwater() && !EffectUtils.hasWaterBreathing(thisEntity) && !invulnerable) {
                    thisEntity.setAirSupply(
                        decreaseAirSupply(
                            thisEntity.getAirSupply() - 4, // -4 to counteract the +4 for rebreathing as vanilla thinks the honey fluid is air
                            thisEntity,
                            thisEntity.level.random)
                    );
                    if (thisEntity.getAirSupply() == -20) {
                        thisEntity.setAirSupply(0);
                        Vector3d vector3d = thisEntity.getDeltaMovement();

                        for(int i = 0; i < 8; ++i) {
                            double d2 = thisEntity.level.random.nextDouble() - thisEntity.level.random.nextDouble();
                            double d3 = thisEntity.level.random.nextDouble() - thisEntity.level.random.nextDouble();
                            double d4 = thisEntity.level.random.nextDouble() - thisEntity.level.random.nextDouble();
                            thisEntity.level.addParticle(BzParticles.HONEY_PARTICLE.get(), thisEntity.getX() + d2, thisEntity.getY() + d3, thisEntity.getZ() + d4, vector3d.x, vector3d.y, vector3d.z);
                        }

                        thisEntity.hurt(DamageSource.DROWN, 2.0F);
                    }
                }

                if (!thisEntity.level.isClientSide() && thisEntity.isPassenger() && thisEntity.getVehicle() != null && !thisEntity.getVehicle().canBeRiddenInWater(thisEntity)) {
                    thisEntity.stopRiding();
                }
            }
        }
    }

    protected static int decreaseAirSupply(int airSupply, LivingEntity entity, Random random) {
        int respiration = EnchantmentHelper.getRespiration(entity);
        return respiration > 0 && random.nextInt(respiration + 1) > 0 ? airSupply : airSupply - 1;
    }

    public static class Flowing extends HoneyFluid {
        public Flowing(Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any()
                    .setValue(LEVEL, 8)
                    .setValue(BOTTOM_LEVEL, 0)
                    .setValue(ABOVE_FLUID, false)
            );
        }

        protected void createFluidStateDefinition(StateContainer.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
            builder.add(BOTTOM_LEVEL);
            builder.add(ABOVE_FLUID);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        protected boolean canConvertToSource() {
            return true;
        }
    }

    public static class Source extends HoneyFluid {

        public Source(Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(ABOVE_FLUID, false));
        }

        protected void createFluidStateDefinition(StateContainer.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(ABOVE_FLUID);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        protected boolean canConvertToSource() {
            return false;
        }
    }
}
