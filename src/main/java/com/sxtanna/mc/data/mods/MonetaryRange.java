package com.sxtanna.mc.data.mods;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

public final class MonetaryRange
{

	@NotNull
	@Contract("_, _ -> new")
	public static MonetaryRange of(@NotNull final Number min, @NotNull final Number max)
	{
		final MonetaryRange range = new MonetaryRange();
		range.setMin(min.intValue());
		range.setMax(max.intValue());

		return range;
	}


	private int min;
	private int max;


	public int getMin()
	{
		return min;
	}

	public void setMin(final int min)
	{
		this.min = min;
	}


	public int getMax()
	{
		return max;
	}

	public void setMax(final int max)
	{
		this.max = max;
	}


	@NotNull
	public BigInteger random()
	{
		// todo: use actual big integer randomization
		return BigInteger.valueOf(ThreadLocalRandom.current().nextLong(getMin(), getMax()));
	}

}
