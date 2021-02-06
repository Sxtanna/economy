package com.sxtanna.mc.cont.impl;

import com.sxtanna.mc.EconomyPlugin;
import com.sxtanna.mc.cont.EconomyController;
import com.sxtanna.mc.data.Bank;
import com.sxtanna.mc.data.User;
import com.sxtanna.mc.data.acts.BankActivity;
import com.sxtanna.mc.data.impl.BankImpl;
import com.sxtanna.mc.repo.EconomyRepository;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

@SuppressWarnings("CodeBlock2Expr")
@ApiStatus.Internal
public final class EconomyControllerLocal implements EconomyController, Listener
{

	@NotNull
	private final EconomyPlugin     plugin;
	@NotNull
	private final Map<String, Bank> cached = new HashMap<>();


	public EconomyControllerLocal(@NotNull final EconomyPlugin plugin)
	{
		this.plugin = plugin;
	}


	@Override
	public void load()
	{
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		final Bank bank = new BankImpl(EconomyController.DEFAULT_BANK);
		this.cached.put(EconomyController.DEFAULT_BANK, bank);


		plugin.getRepository().ifPresent(repository -> {
			for (final Player online : plugin.getServer().getOnlinePlayers())
			{
				load(repository, bank, online);
			}
		});
	}

	@Override
	public void kill()
	{
		HandlerList.unregisterAll(this);

		plugin.getRepository().ifPresent(repository -> {
			try
			{
				CompletableFuture.allOf(cached.values().stream().map(repository::save).toArray(CompletableFuture[]::new)).join();
			}
			catch (final CompletionException ex)
			{
				plugin.getLogger().log(Level.SEVERE, "failed to save banks to repository", ex);
			}
		});

		cached.clear();
	}


	@NotNull
	@Override
	public Optional<Bank> findBank(@NotNull final String name)
	{
		return Optional.ofNullable(this.cached.get(name));
	}

	@NotNull
	@Override
	public Optional<User> findUserInDefaultBank(@NotNull final UUID uuid)
	{
		return findBank(DEFAULT_BANK).flatMap(bank -> bank.findUser(uuid));
	}


	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(@NotNull final PlayerJoinEvent event)
	{
		plugin.getRepository().ifPresent(repository -> {

			findBank(EconomyController.DEFAULT_BANK).ifPresent(bank -> {

				load(repository, bank, event.getPlayer());

			});

		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuit(@NotNull final PlayerQuitEvent event)
	{
		plugin.getRepository().ifPresent(repository -> {

			findBank(EconomyController.DEFAULT_BANK).ifPresent(bank -> {

				bank.killUser(event.getPlayer().getUniqueId()).ifPresent(user -> {
					repository.save(bank, user);
				});

			});

		});
	}


	private void load(@NotNull final EconomyRepository repository, @NotNull final Bank bank, @NotNull final Player player)
	{
		repository.load(bank, player.getUniqueId()).whenComplete((user, ex) -> {
			if (ex != null)
			{
				plugin.getLogger().log(Level.SEVERE, "failed to load user " + player.getUniqueId(), ex);
				return;
			}

			final BankActivity make = bank.makeUser(player.getUniqueId());
			if (user != null && user.isPresent())
			{
				if (make.isSuccess())
				{
					((BankActivity.Success) make).getUser().setBalance(user.get().getBalance());
				}
				else
				{
					plugin.getLogger().log(Level.SEVERE, "failed to make user " + player.getUniqueId());
				}
			}
		});
	}

}