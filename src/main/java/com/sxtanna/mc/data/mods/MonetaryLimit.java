package com.sxtanna.mc.data.mods;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MonetaryLimit
{

	public static final MonetaryLimit DEFAULT = new MonetaryLimit();

	static
	{
		final MonetaryGroup group0 = new MonetaryGroup();
		group0.setPerm("tokens.daily.group0");
		group0.setMax(50);

		DEFAULT.groups.put("group0", group0);

		final MonetaryGroup group1 = new MonetaryGroup();
		group1.setPerm("tokens.daily.group1");
		group1.setMax(100);

		DEFAULT.groups.put("group1", group1);
	}



	private Map<String, MonetaryGroup> groups = new LinkedHashMap<>();


	public Map<String, MonetaryGroup> getGroups()
	{
		return groups;
	}

	public void setGroups(final Map<String, MonetaryGroup> groups)
	{
		this.groups = groups;
	}

}
