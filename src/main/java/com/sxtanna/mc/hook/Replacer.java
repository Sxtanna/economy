package com.sxtanna.mc.hook;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class Replacer implements BiFunction<@Nullable OfflinePlayer, @NotNull String, @NotNull String>
{

	private final List<BiFunction<@Nullable OfflinePlayer, @NotNull String, @NotNull String>> functions = new ArrayList<>();



	public void register(BiFunction<@Nullable OfflinePlayer, @NotNull String, @NotNull String> function)
	{
		this.functions.add(function);
	}


	@NotNull
	@Override
	public String apply(@Nullable OfflinePlayer player, @NotNull String text)
	{
		for (final BiFunction<OfflinePlayer, String, String> function : functions)
		{
			text = function.apply(player, text);
		}

		return text;
	}

}
