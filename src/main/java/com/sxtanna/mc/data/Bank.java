package com.sxtanna.mc.data;

import com.sxtanna.mc.data.acts.BankActivity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface Bank
{

	@NotNull
	String getName();


	@NotNull
	@Unmodifiable
	Map<UUID, User> getUsers();


	@NotNull
	Optional<User> findUser(@NotNull final UUID uuid);

	@NotNull
	Optional<User> killUser(@NotNull final UUID uuid);


	@NotNull
	BankActivity makeUser(@NotNull final UUID uuid);

}
