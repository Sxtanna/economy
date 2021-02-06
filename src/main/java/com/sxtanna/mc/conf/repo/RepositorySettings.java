package com.sxtanna.mc.conf.repo;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;
import com.sxtanna.mc.repo.base.RepositoryType;
import org.jetbrains.annotations.NotNull;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public final class RepositorySettings implements SettingsHolder
{

	@NotNull
	public static final Property<RepositoryType> REPOSITORY_TYPE = newProperty(RepositoryType.class, "repository.type", RepositoryType.LOCAL);

	public static final Property<String>  REPOSITORY_HOST = newProperty("repository.host", "localhost");
	public static final Property<Integer> REPOSITORY_PORT = newProperty("repository.port", 3600);

	public static final Property<String>  REPOSITORY_DATABASE = newProperty("repository.database", "");

	public static final Property<String>  REPOSITORY_USERNAME = newProperty("repository.username", "");
	public static final Property<String>  REPOSITORY_PASSWORD = newProperty("repository.password", "");

}
