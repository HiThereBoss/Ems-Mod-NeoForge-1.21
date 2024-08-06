package net.em.ems_mod.sound;

import net.em.ems_mod.EmsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, EmsMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> MICROWAVE_HUM = registerSoundEvents("microwave_hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> MICROWAVE_OPEN = registerSoundEvents("microwave_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> MICROWAVE_CLOSE = registerSoundEvents("microwave_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> MICROWAVE_FINISHED = registerSoundEvents("microwave_finished");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(EmsMod.MODID, name)));
    }

    public static void register(IEventBus eventBus){
        SOUND_EVENTS.register(eventBus);
    }
}
