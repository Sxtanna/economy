package com.sxtanna.mc.data.acts;

import com.sxtanna.mc.data.base.Activity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.time.Instant;
import java.util.function.Consumer;

@ApiStatus.NonExtendable
public abstract class UserActivity implements Activity
{

	@NotNull
	@Contract("_ -> new")
	public static UserActivity success(@NotNull final BigInteger amount)
	{
		return new UserActivity.Success(amount, Instant.now());
	}


	@NotNull
	@Contract("_, _ -> new")
	public static UserActivity failure(@NotNull final BigInteger amount, @NotNull final String message)
	{
		return new UserActivity.Failure(amount, Instant.now(), message);
	}


	@NotNull
	private final BigInteger amount;
	@NotNull
	private final Instant    moment;


	UserActivity(@NotNull final BigInteger amount, @NotNull final Instant moment)
	{
		this.amount = amount;
		this.moment = moment;
	}


	@NotNull
	@Contract(pure = true)
	public final BigInteger getAmount()
	{
		return amount;
	}

	@NotNull
	@Contract(pure = true)
	@Override
	public final Instant getMoment()
	{
		return moment;
	}



	public void handle(@NotNull final Consumer<Failure> failure, @NotNull final Consumer<Success> success)
	{
		if (this instanceof Failure)
		{
			failure.accept(((Failure) this));
		}
		else if (this instanceof Success)
		{
			success.accept(((Success) this));
		}
	}



	public static final class Success extends UserActivity
	{

		Success(@NotNull final BigInteger amount, @NotNull final Instant moment)
		{
			super(amount, moment);
		}

	}

	public static final class Failure extends UserActivity
	{

		@NotNull
		private final String message;


		Failure(@NotNull final BigInteger amount, @NotNull final Instant moment, @NotNull final String message)
		{
			super(amount, moment);

			this.message = message;
		}


		@NotNull
		@Contract(pure = true)
		public String getMessage()
		{
			return message;
		}

	}

}
