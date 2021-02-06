package com.sxtanna.mc.conf.base;

import ch.jalu.configme.properties.BaseProperty;
import ch.jalu.configme.properties.PropertyBuilder;
import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.properties.types.EnumPropertyType;
import ch.jalu.configme.properties.types.PrimitivePropertyType;
import ch.jalu.configme.properties.types.PropertyType;
import ch.jalu.configme.resource.PropertyReader;
import com.sxtanna.mc.data.mods.MonetaryGains;
import com.sxtanna.mc.data.mods.MonetaryRange;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EnumMapProperty<E extends Enum<E>, V> extends BaseProperty<Map<E, V>>
{

	private final EnumPropertyType<E> kType;
	private final PropertyType<V>     vType;


	public EnumMapProperty(final String path, final Map<E, V> defaultValue, final EnumPropertyType<E> kType, final PropertyType<V> vType)
	{
		super(path, defaultValue);

		this.kType = kType;
		this.vType = vType;
	}


	@Nullable
	@Override
	protected Map<E, V> getFromReader(final PropertyReader reader, final ConvertErrorRecorder errorRecorder)
	{
		final Object rawObject = reader.getObject(getPath());

		if (!(rawObject instanceof Map<?, ?>))
		{
			return null;
		}

		final Map<?, ?> raw = (Map<?, ?>) rawObject;
		final Map<E, V> map = new LinkedHashMap<>();

		for (Map.Entry<?, ?> entry : raw.entrySet())
		{
			final E k = this.kType.convert(entry.getKey(), errorRecorder);
			final V v = this.vType.convert(entry.getValue(), errorRecorder);

			if (k != null && v != null)
			{
				map.put(k, v);
			}
		}

		return map;
	}

	@Override
	public Object toExportValue(final Map<E, V> value)
	{
		final Map<Object, Object> export = new LinkedHashMap<>();

		for (Map.Entry<E, V> entry : value.entrySet())
		{
			export.put(this.kType.toExportValue(entry.getKey()), this.vType.toExportValue(entry.getValue()));
		}

		return export;
	}


	public static <E extends Enum<E>, V> EnumMapPropertyBuilder<E, V> builder(final Class<E> kClazz, final PropertyType<V> vType)
	{
		return new EnumMapPropertyBuilder<>(EnumPropertyType.of(kClazz), vType);
	}


	public static class EnumMapPropertyBuilder<E extends Enum<E>, V> extends PropertyBuilder<V, Map<E, V>, EnumMapPropertyBuilder<E, V>>
	{

		private final EnumPropertyType<E> kType;

		public EnumMapPropertyBuilder(final EnumPropertyType<E> kType, final PropertyType<V> vType)
		{
			super(vType);

			this.kType = kType;

			defaultValue(new LinkedHashMap<>());
		}

		public EnumMapPropertyBuilder<E, V> defaultEntry(E key, V value)
		{
			getDefaultValue().put(key, value);
			return this;
		}

		@Override
		public EnumMapProperty<E, V> build()
		{
			return new EnumMapProperty<>(getPath(), getDefaultValue(), this.kType, getType());
		}

	}

}
