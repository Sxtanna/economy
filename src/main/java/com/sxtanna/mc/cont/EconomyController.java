package com.sxtanna.mc.cont;

import com.sxtanna.mc.base.State;
import com.sxtanna.mc.data.Bank;
import com.sxtanna.mc.data.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface EconomyController extends State
{

	@NotNull
	String DEFAULT_BANK = "tokens";


	@NotNull
	Optional<Bank> findBank(@NotNull final String name);

	@NotNull
	Optional<User> findUserInDefaultBank(@NotNull final UUID uuid);

}
