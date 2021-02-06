package com.sxtanna.mc.util;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public enum Optionals
{
	;


	public static <T> void handle(@NotNull final Optional<T> optional, @NotNull final Runnable none, @NotNull final Consumer<T> some)
	{
		if (!optional.isPresent())
		{
			none.run();
		}
		else
		{
			some.accept(optional.get());
		}
	}

}
