package com.telepathicgrunt.the_bumblezone.entities;

import com.telepathicgrunt.the_bumblezone.Bumblezone;
import com.telepathicgrunt.the_bumblezone.entities.mobs.HoneySlimeEntity;
import com.telepathicgrunt.the_bumblezone.generation.BzBiomeProvider;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.BiomeMaker;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.RegistryEvent;

public class BzEntities {

    public static final EntityType<HoneySlimeEntity> HONEY_SLIME = EntityType.Builder.<HoneySlimeEntity>create(HoneySlimeEntity::new, EntityClassification.CREATURE).size(0.6F, 1.99F).maxTrackingRange(8).build("honey_slime");

    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().register(HONEY_SLIME.setRegistryName(new ResourceLocation(Bumblezone.MODID, "honey_slime")));
        
        registerEntitySpawnRestrictions();
        registerEntityAttributes();
    }

    private static void registerEntitySpawnRestrictions(){
        EntitySpawnPlacementRegistry.register(HONEY_SLIME, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canSpawnOn);
    }

    private static void registerEntityAttributes(){
        GlobalEntityTypeAttributes.put(HONEY_SLIME, HoneySlimeEntity.getAttributeBuilder().build());
    }
}
