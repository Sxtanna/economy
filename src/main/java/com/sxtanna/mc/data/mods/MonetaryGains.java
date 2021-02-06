package com.sxtanna.mc.data.mods;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class MonetaryGains
{

	public static MonetaryGains of(@NotNull final Consumer<MonetaryGains> consumer)
	{
		final MonetaryGains gains = new MonetaryGains();
		consumer.accept(gains);

		return gains;
	}



	private Map<String, MonetaryRange> types = new LinkedHashMap<>();


	public Map<String, MonetaryRange> getTypes()
	{
		return types;
	}

	public void setTypes(final Map<String, MonetaryRange> types)
	{
		this.types = types;
	}


	public void addRange(@NotNull final EntityType type, @NotNull final MonetaryRange range)
	{
		if (this.types == null)
		{
			this.types = new LinkedHashMap<>();
		}

		this.types.put(type.name().toLowerCase(Locale.ROOT), range);
	}

	public Optional<MonetaryRange> getRange(@NotNull final EntityType type)
	{
		final Map<String, MonetaryRange> types = this.types;
		if (types == null)
		{
			return Optional.empty();
		}

		return Optional.ofNullable(types.get(type.name().toLowerCase(Locale.ROOT)));
	}

}
