package com.sxtanna.mc.data;

import com.sxtanna.mc.data.acts.UserActivity;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.UUID;

public interface User
{

	@NotNull
	UUID getUuid();

	@NotNull
	BigInteger getBalance();

	void setBalance(@NotNull final BigInteger balance);


	@NotNull
	UserActivity add(@NotNull final BigInteger amount);

	@NotNull
	UserActivity sub(@NotNull final BigInteger amount);

}
