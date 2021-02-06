package com.sxtanna.mc.data.impl;

import com.sxtanna.mc.data.User;
import com.sxtanna.mc.data.acts.UserActivity;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

public final class UserImpl implements User
{

	@NotNull
	private final UUID       uuid;
	@NotNull
	private       BigInteger balance = BigInteger.ZERO;


	public UserImpl(@NotNull final UUID uuid)
	{
		this.uuid = uuid;
	}


	@NotNull
	@Override
	public UUID getUuid()
	{
		return this.uuid;
	}


	@NotNull
	@Override
	public BigInteger getBalance()
	{
		return this.balance;
	}

	@Override
	public void setBalance(@NotNull final BigInteger balance)
	{
		this.balance = balance;
	}


	@NotNull
	@Override
	public UserActivity add(@NotNull final BigInteger amount)
	{
		this.balance = this.balance.add(amount);
		return UserActivity.success(amount);
	}


	@NotNull
	@Override
	public UserActivity sub(@NotNull final BigInteger amount)
	{
		final BigInteger next = this.balance.subtract(amount);
		if (next.compareTo(BigInteger.ZERO) < 0)
		{
			return UserActivity.failure(amount, "insufficient funds");
		}

		this.balance = next;

		return UserActivity.success(amount);
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof UserImpl))
		{
			return false;
		}

		final UserImpl user = (UserImpl) o;
		return getUuid().equals(user.getUuid()) &&
			   getBalance().equals(user.getBalance());
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(getUuid(), getBalance());
	}

	@Override
	public String toString()
	{
		return String.format("User[%s: %s]", uuid, balance);
	}

}
