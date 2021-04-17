package com.telepathicgrunt.the_bumblezone.modinit;

import com.telepathicgrunt.the_bumblezone.Bumblezone;
import com.telepathicgrunt.the_bumblezone.items.ContainerCraftingRecipe;
import com.telepathicgrunt.the_bumblezone.items.HoneyCrystalShield;
import com.telepathicgrunt.the_bumblezone.items.HoneySlimeSpawnEgg;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public class BzItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Bumblezone.MODID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Bumblezone.MODID);

    /**
     * creative tab to hold our block items
     */
    public static final ItemGroup BUMBLEZONE_CREATIVE_TAB = new ItemGroup(ItemGroup.GROUPS.length, Bumblezone.MODID) {
        @Override
        // CLIENT-SIDED
        public ItemStack createIcon() {
            return new ItemStack(BzBlocks.FILLED_POROUS_HONEYCOMB.get());
        }
    };

    //Recipe
    public static final RegistryObject<IRecipeSerializer<ContainerCraftingRecipe>> CONTAINER_CRAFTING_RECIPE = RECIPES.register("container_shapeless_recipe_bz", ContainerCraftingRecipe.Serializer::new);

    //blocks
    public static final RegistryObject<Item> POROUS_HONEYCOMB = ITEMS.register("porous_honeycomb_block", () -> new BlockItem(BzBlocks.POROUS_HONEYCOMB.get(), new Item.Properties().group(BUMBLEZONE_CREATIVE_TAB)));
    public static final RegistryObject<Item> FILLED_POROUS_HONEYCOMB = ITEMS.register("filled_porous_honeycomb_block", () -> new BlockItem(BzBlocks.FILLED_POROUS_HONEYCOMB.get(), new Item.Properties().group(BUMBLEZONE_CREATIVE_TAB)));
    public static final RegistryObject<Item> EMPTY_HONEYCOMB_LARVA = ITEMS.register("empty_honeycomb_brood_block", () -> new BlockItem(BzBlocks.EMPTY_HONEYCOMB_BROOD.get(), new Item.Properties().group(BUMBLEZONE_CREATIVE_TAB)));
    public static final RegistryObject<Item> HONEYCOMB_LARVA = ITEMS.register("honeycomb_brood_block", () -> new BlockItem(BzBlocks.HONEYCOMB_BROOD.get(), new Item.Properties().group(BUMBLEZONE_CREATIVE_TAB)));
    public static final RegistryObject<Item> SUGAR_INFUSED_STONE = ITEMS.register("sugar_infused_stone", () -> new BlockItem(BzBlocks.SUGAR_INFUSED_STONE.get(), new Item.Properties().group(BUMBLEZONE_CREATIVE_TAB)));
    public static final RegistryObject<Item> SUGAR_INFUSED_COBBLESTONE = ITEMS.register("sugar_infused_cobblestone", () -> new BlockItem(BzBlocks.SUGAR_INFUSED_COBBLESTONE.get(), new Item.Properties().group(BUMBLEZONE_CREATIVE_TAB)));
    public static final RegistryObject<Item> HONEY_CRYSTAL = ITEMS.register("honey_crystal", () -> new BlockItem(BzBlocks.HONEY_CRYSTAL.get(), new Item.Properties().group(BUMBLEZONE_CREATIVE_TAB)));
    public static final RegistryObject<Item> STICKY_HONEY_RESIDUE = ITEMS.register("sticky_honey_residue", () -> new BlockItem(BzBlocks.STICKY_HONEY_RESIDUE.get(), new Item.Properties().group(BUMBLEZONE_CREATIVE_TAB)));
    public static final RegistryObject<Item> STICKY_HONEY_REDSTONE = ITEMS.register("sticky_honey_redstone", () -> new BlockItem(BzBlocks.STICKY_HONEY_REDSTONE.get(), new Item.Properties().group(BUMBLEZONE_CREATIVE_TAB)));
    public static final RegistryObject<Item> BEESWAX_PLANKS = ITEMS.register("beeswax_planks", () -> new BlockItem(BzBlocks.BEESWAX_PLANKS.get(), new Item.Properties().group(BUMBLEZONE_CREATIVE_TAB)));


    //items
    public static final RegistryObject<Item> HONEY_CRYSTAL_SHARDS = ITEMS.register("honey_crystal_shards", () -> new Item(new Item.Properties().group(BUMBLEZONE_CREATIVE_TAB).food((new Food.Builder()).hunger(2).saturation(0.15F).build())));
    public static final RegistryObject<Item> HONEY_CRYSTAL_SHIELD = ITEMS.register("honey_crystal_shield", HoneyCrystalShield::new);
    public static final RegistryObject<Item> SUGAR_WATER_BUCKET = ITEMS.register("sugar_water_bucket", () -> new BucketItem(BzFluids.SUGAR_WATER_FLUID, new Item.Properties().containerItem(Items.BUCKET).maxStackSize(1).group(BUMBLEZONE_CREATIVE_TAB)));
    public static final RegistryObject<Item> SUGAR_WATER_BOTTLE = ITEMS.register("sugar_water_bottle", () -> new HoneyBottleItem((new Item.Properties()).containerItem(Items.GLASS_BOTTLE).food((new Food.Builder()).hunger(1).saturation(0.05F).effect(() -> new EffectInstance(Effects.HASTE, 600, 0), 1.0F).build()).group(BUMBLEZONE_CREATIVE_TAB).maxStackSize(16)));
    public static final RegistryObject<Item> HONEY_SLIME_SPAWN_EGG = ITEMS.register("honey_slime_spawn_egg", () -> new HoneySlimeSpawnEgg(BzEntities.HONEY_SLIME_RAW, 0xFFCC00, 0xFCA800, (new Item.Properties()).group(BUMBLEZONE_CREATIVE_TAB)));
}