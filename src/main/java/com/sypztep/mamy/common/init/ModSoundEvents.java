package com.sypztep.mamy.common.init;

import com.sypztep.mamy.Mamy;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ModSoundEvents {
	Map<SoundEvent, Identifier> SOUND_EVENTS = new LinkedHashMap<>();
	//Sound
    SoundEvent ENTITY_GENERIC_HEADSHOT = createSoundEvent("entity.generic.headshot");
    SoundEvent ENTITY_GENERIC_RICOCHET = createSoundEvent("entity.generic.ricochet");
    SoundEvent ENTITY_GENERIC_SHOCKWAVE = createSoundEvent("entity.generic.shockwave");
    SoundEvent ENTITY_GENERIC_MAGIC_ARROW_EXPLODE = createSoundEvent("entity.generic.magic_arrow_explode");
    SoundEvent ENTITY_GENERIC_AHHH = createSoundEvent("entity.generic.ahhh");
    SoundEvent ENTITY_ZAP = createSoundEvent("entity.generic.zap");
    SoundEvent ENTITY_ELECTRIC_BIG_EXPLODE = createSoundEvent("entity.generic.electric_big_explode");
    SoundEvent ENTITY_ELECTRIC_SHOOT = createSoundEvent("entity.generic.electric_shoot");
    SoundEvent ENTITY_ELECTRIC_BLAST = createSoundEvent("entity.generic.electric_blast");
	static void init() {
		SOUND_EVENTS.keySet().forEach((soundEvent) -> Registry.register(Registries.SOUND_EVENT, SOUND_EVENTS.get(soundEvent), soundEvent));
	}
	private static SoundEvent createSoundEvent(String path) {
		SoundEvent soundEvent = SoundEvent.of(Mamy.id(path));
		SOUND_EVENTS.put(soundEvent, Mamy.id(path));
		return soundEvent;
	}
}
