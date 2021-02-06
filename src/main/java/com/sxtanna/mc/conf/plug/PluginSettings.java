package com.sxtanna.mc.conf.plug;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public final class PluginSettings implements SettingsHolder
{

	public static final Property<String> DEFAULT_LOCALE = newProperty("plugin.default-lang", "en-US");

}
