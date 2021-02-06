package com.sxtanna.mc.repo;

import com.sxtanna.mc.base.State;
import com.sxtanna.mc.data.Bank;
import com.sxtanna.mc.data.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface EconomyRepository extends State
{

	CompletableFuture<Void> save(@NotNull final Bank bank);
	CompletableFuture<Void> save(@NotNull final Bank bank, @NotNull final User user);

	CompletableFuture<Optional<User>> load(@NotNull final Bank bank, @NotNull final UUID uuid);

}
