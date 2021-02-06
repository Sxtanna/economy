package com.sxtanna.mc.data.acts;

import com.sxtanna.mc.data.User;
import com.sxtanna.mc.data.base.Activity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.UUID;

@ApiStatus.NonExtendable
public abstract class BankActivity implements Activity
{

	@NotNull
	@Contract("_ -> new")
	public static BankActivity success(@NotNull final User user)
	{
		return new Success(user.getUuid(), Instant.now(), user);
	}


	@NotNull
	@Contract("_, _ -> new")
	public static BankActivity failure(@NotNull final UUID uuid, @NotNull final String message)
	{
		return new Failure(uuid, Instant.now(), message);
	}


	@NotNull
	private final UUID    target;
	@NotNull
	private final Instant moment;


	BankActivity(@NotNull final UUID target, @NotNull final Instant moment)
	{
		this.target = target;
		this.moment = moment;
	}


	@NotNull
	@Contract(pure = true)
	public final UUID getTarget()
	{
		return this.target;
	}

	@NotNull
	@Contract(pure = true)
	@Override
	public final Instant getMoment()
	{
		return this.moment;
	}


	@Contract(pure = true)
	public final boolean isSuccess()
	{
		return this instanceof Success;
	}

	@Contract(pure = true)
	public final boolean isFailure()
	{
		return this instanceof Failure;
	}


	public static final class Success extends BankActivity
	{

		@NotNull
		private final User user;


		Success(@NotNull final UUID target, @NotNull final Instant moment, @NotNull final User user)
		{
			super(target, moment);

			this.user = user;
		}


		@NotNull
		@Contract(pure = true)
		public User getUser()
		{
			return user;
		}

	}

	public static final class Failure extends BankActivity
	{

		@NotNull
		private final String message;


		Failure(@NotNull final UUID target, @NotNull final Instant moment, @NotNull final String message)
		{
			super(target, moment);

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
