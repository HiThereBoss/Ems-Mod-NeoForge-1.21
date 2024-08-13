package net.em.ems_mod.item;

import net.em.ems_mod.EmsMod;
import net.em.ems_mod.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EmsMod.MODID);

    public static final DeferredItem<Item> SAPPHIRE = ITEMS.register("sapphire",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAW_SAPPHIRE = ITEMS.register("raw_sapphire",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SHAWARMA = ITEMS.register("shawarma",
            () -> new Item(new Item.Properties().food(ModFoods.SHAWARMA)));

    public static final DeferredItem<Item> TEST_SHAWARMA = ITEMS.register("test_shawarma",
            () -> new Item(new Item.Properties().food(ModFoods.TEST_SHAWARMA)));

    //public static final DeferredItem<Item> MINIVAN_SPAWN_EGG = ITEMS.register("minivan_spawn_egg.json",
    //        () -> new DeferredSpawnEggItem(ModEntities.MINIVAN_ENTITY, 0xFFFFFF,0x000000, new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
