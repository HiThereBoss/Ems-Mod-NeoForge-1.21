package net.em.ems_mod.entity;

import net.em.ems_mod.EmsMod;
import net.em.ems_mod.entity.custom.MiniVanEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, EmsMod.MODID);

    public static final DeferredHolder<EntityType<?>,EntityType<MiniVanEntity>> MINIVAN_ENTITY = ENTITY_TYPES.register("minivan",
            () -> EntityType.Builder.of(MiniVanEntity::new, MobCategory.MISC).sized(2.25f,2f).eyeHeight(1.5f).build(ResourceLocation.fromNamespaceAndPath(EmsMod.MODID,"minivan").toString()));

    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }
}
