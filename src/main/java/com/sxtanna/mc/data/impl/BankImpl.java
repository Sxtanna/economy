package com.sxtanna.mc.data.impl;

import com.google.common.collect.ImmutableMap;
import com.sxtanna.mc.data.Bank;
import com.sxtanna.mc.data.User;
import com.sxtanna.mc.data.acts.BankActivity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class BankImpl implements Bank
{

	@NotNull
	private final String          name;
	@NotNull
	private final Map<UUID, User> users = new HashMap<>();


	public BankImpl(@NotNull final String name)
	{
		this.name = name;
	}


	@NotNull
	@Override
	public String getName()
	{
		return this.name;
	}


	@NotNull
	@Unmodifiable
	@Override
	public Map<UUID, User> getUsers()
	{
		return ImmutableMap.copyOf(this.users);
	}


	@NotNull
	@Override
	public Optional<User> findUser(@NotNull final UUID uuid)
	{
		return Optional.ofNullable(this.users.get(uuid));
	}

	@NotNull
	@Override
	public Optional<User> killUser(@NotNull final UUID uuid)
	{
		return Optional.ofNullable(this.users.remove(uuid));
	}


	@NotNull
	@Override
	public BankActivity makeUser(@NotNull final UUID uuid)
	{
		final User prev = this.users.get(uuid);
		if (prev != null)
		{
			return BankActivity.failure(uuid, "user already exists");
		}

		final User user = new UserImpl(uuid);
		this.users.put(uuid, user);

		return BankActivity.success(user);
	}

}
