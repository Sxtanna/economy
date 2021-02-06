package com.sxtanna.mc.conf;

import ch.jalu.configme.SettingsManagerImpl;
import ch.jalu.configme.migration.PlainMigrationService;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.resource.YamlFileResource;
import com.sxtanna.mc.conf.mods.ModifierSettings;
import com.sxtanna.mc.conf.plug.PluginSettings;
import com.sxtanna.mc.conf.repo.RepositorySettings;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;

import static ch.jalu.configme.configurationdata.ConfigurationDataBuilder.createConfiguration;

@ApiStatus.Internal
public final class EconomyConfiguration extends SettingsManagerImpl
{

	public EconomyConfiguration(@NotNull final File file)
	{
		super(new YamlFileResource(file.toPath()), createConfiguration(PluginSettings.class, RepositorySettings.class, ModifierSettings.class), new PlainMigrationService());
	}


	@NotNull
	public <T> T getPropertyOrDefault(@NotNull final Property<T> property)
	{
		return getPropertyOptional(property).orElse(property.getDefaultValue());
	}

	@NotNull
	public <T> Optional<T> getPropertyOptional(@NotNull final Property<T> property)
	{
		return Optional.ofNullable(getProperty(property));
	}

}
