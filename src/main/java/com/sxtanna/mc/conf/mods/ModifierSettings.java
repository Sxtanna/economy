package com.sxtanna.mc.conf.mods;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.MapProperty;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.types.BeanPropertyType;
import com.sxtanna.mc.cont.EconomyController;
import com.sxtanna.mc.data.mods.MonetaryGains;
import com.sxtanna.mc.data.mods.MonetaryLimit;
import com.sxtanna.mc.data.mods.MonetaryRange;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

import static ch.jalu.configme.properties.PropertyInitializer.mapProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newBeanProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public final class ModifierSettings implements SettingsHolder
{

	public static final Property<Boolean> GAINS_SHOW_CHAT_MESSAGE = newProperty("gains.option.chat.show", true);
	public static final Property<Boolean> GAINS_SHOW_HOLO_MESSAGE = newProperty("gains.option.holo.show", true);

	public static final Property<Boolean> GAINS_SHOW_LIMIT_MESSAGE = newProperty("gains.option.limit.show", true);
	public static final Property<Boolean> GAINS_PLAY_LIMIT_SOUND   = newProperty("gains.option.limit.sound.play", true);
	public static final Property<Sound>   GAINS_LIMIT_SOUND        = newProperty(Sound.class, "gains.option.limit.sound.type", Sound.BLOCK_STONE_BREAK);
	public static final Property<Double>  GAINS_LIMIT_SOUND_VOLUME = newProperty("gains.option.limit.sound.volume", 1.0);
	public static final Property<Double>  GAINS_LIMIT_SOUND_PITCH   = newProperty("gains.option.limit.sound.pitch", 1.0);

	public static final Property<Boolean> GAINS_PLAY_HOLO_SOUND   = newProperty("gains.option.holo.sound.play", true);
	public static final Property<Sound>   GAINS_HOLO_SOUND        = newProperty(Sound.class, "gains.option.holo.sound.type", Sound.BLOCK_ENCHANTMENT_TABLE_USE);
	public static final Property<Double>  GAINS_HOLO_SOUND_VOLUME = newProperty("gains.option.holo.sound.volume", 1.0);
	public static final Property<Double>  GAINS_HOLO_SOUND_PITCH  = newProperty("gains.option.holo.sound.pitch", 1.0);
	public static final Property<Integer> GAINS_HOLO_FLOAT_TIME   = newProperty("gains.option.holo.float.time", 20);
	public static final Property<Integer> GAINS_HOLO_FLOAT_MULT   = newProperty("gains.option.holo.float.mult", 1);


	public static final MapProperty<MonetaryGains> REWARDS = mapProperty(BeanPropertyType.of(MonetaryGains.class))
			.path("gains.reward")
			.defaultEntry(EconomyController.DEFAULT_BANK, MonetaryGains.of(gains -> {
				gains.addRange(EntityType.ZOMBIE, MonetaryRange.of(1, 10));
				gains.addRange(EntityType.WITHER, MonetaryRange.of(5, 10));
			}))
			.build();

	public static final Property<MonetaryLimit> LIMITS = newBeanProperty(MonetaryLimit.class, "gains.limits", MonetaryLimit.DEFAULT);

}
