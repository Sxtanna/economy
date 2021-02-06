package com.sxtanna.mc.repo.impl;

import com.sxtanna.mc.cont.EconomyController;
import com.sxtanna.mc.data.Bank;
import com.sxtanna.mc.data.User;
import com.sxtanna.mc.data.impl.UserImpl;
import com.sxtanna.mc.repo.EconomyRepository;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@ApiStatus.Internal
public final class EconomyRepositorySQLite implements EconomyRepository
{

	private static final long FIVE_MINUTES = 20 * 60 * 5;


	private static final String SQLITE_URL = "jdbc:sqlite:%s";
	private static final String SQLITE_DRV = "org.sqlite.JDBC";


	@NotNull
	private final Plugin plugin;
	@NotNull
	private final File   file;

	@NotNull
	private final AtomicReference<Connection> connection = new AtomicReference<>();
	@NotNull
	private final AtomicReference<BukkitTask> bukkitTask = new AtomicReference<>();


	public EconomyRepositorySQLite(@NotNull final Plugin plugin)
	{
		this.plugin = plugin;
		this.file   = new File(plugin.getDataFolder(), "repository.db");
	}


	@Override
	public void load()
	{
		if (!ensureFileExists() || !ensureJDBCLoaded() || !ensureConnecting())
		{
			return;
		}

		scheduleAutoSave();

		createTokensBank();
	}

	@Override
	public void kill()
	{
		final BukkitTask bukkitTask = this.bukkitTask.getAndSet(null);
		if (bukkitTask != null)
		{
			bukkitTask.cancel();
		}

		final Connection connection = this.connection.getAndSet(null);
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (final SQLException ex)
			{
				this.plugin.getLogger().log(Level.SEVERE, "failed to save sqlite database", ex);
			}
		}
	}


	@Override
	public CompletableFuture<Optional<User>> load(@NotNull final Bank bank, @NotNull final UUID uuid)
	{
		final Connection connection = this.connection.get();
		if (connection == null)
		{
			return completedFuture(Optional.empty());
		}

		return supplyAsync(() -> {
			try (final PreparedStatement statement = connection.prepareStatement(Statements.SELECT.getStatement(bank.getName().toLowerCase(Locale.ROOT))))
			{
				statement.setString(1, uuid.toString());

				final ResultSet result = statement.executeQuery();
				if (!result.next())
				{
					return Optional.empty();
				}

				final UserImpl user = new UserImpl(uuid);
				user.setBalance(result.getBigDecimal("balance").toBigInteger());

				return Optional.of(user);
			}
			catch (final SQLException ex)
			{
				throw new CompletionException(ex);
			}
		});
	}

	@Override
	public CompletableFuture<Void> save(@NotNull final Bank bank)
	{
		final Connection connection = this.connection.get();
		if (connection == null)
		{
			return completedFuture(null);
		}

		return runAsync(() -> {
			try
			{
				createBank(connection, bank.getName());
			}
			catch (final SQLException ex)
			{
				throw new CompletionException("failed to create bank table for " + bank.getName(), ex);
			}
		}).thenRun(() -> {
			final Set<User> failures = new HashSet<>();

			for (final User user : bank.getUsers().values())
			{
				try
				{
					insertUser(connection, bank.getName(), user.getUuid(), user.getBalance());
				}
				catch (final SQLException ex)
				{
					failures.add(user);
				}
			}

			if (!failures.isEmpty())
			{
				throw new CompletionException("failed to insert users: " + failures, new IllegalStateException("failures recorded"));
			}
		});
	}


	@Override
	public CompletableFuture<Void> save(@NotNull final Bank bank, @NotNull final User user)
	{
		final Connection connection = this.connection.get();
		if (connection == null)
		{
			return completedFuture(null);
		}

		return runAsync(() -> {
			try
			{
				insertUser(connection, bank.getName(), user.getUuid(), user.getBalance());
			}
			catch (final SQLException ex)
			{
				throw new CompletionException("failed to insert bank user for " + user.getUuid(), ex);
			}
		});
	}

	private boolean ensureFileExists()
	{
		if (this.file.exists())
		{
			return true;
		}

		try
		{
			return this.file.getParentFile().mkdirs() || this.file.createNewFile();
		}
		catch (final IOException ex)
		{
			this.plugin.getLogger().log(Level.SEVERE, "failed to create sqlite file", ex);
			return false;
		}
	}

	private boolean ensureJDBCLoaded()
	{
		try
		{
			Class.forName(SQLITE_DRV);
			return true;
		}
		catch (final ClassNotFoundException ex)
		{
			this.plugin.getLogger().log(Level.SEVERE, "failed to load sqlite jdbc driver", ex);
			return false;
		}
	}

	private boolean ensureConnecting()
	{
		try
		{
			this.connection.set(DriverManager.getConnection(String.format(SQLITE_URL, this.file)));
			return true;
		}
		catch (final SQLException ex)
		{
			this.plugin.getLogger().log(Level.SEVERE, "failed to open sqlite connection", ex);
			return false;
		}
	}


	private void scheduleAutoSave()
	{
		this.bukkitTask.set(this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, this::periodicallySave, FIVE_MINUTES, FIVE_MINUTES));
	}

	private void periodicallySave()
	{
		try
		{
			final Connection old = this.connection.getAndSet(DriverManager.getConnection(String.format(SQLITE_URL, this.file)));
			if (old != null)
			{
				old.close();
			}
		}
		catch (final SQLException ex)
		{
			this.plugin.getLogger().log(Level.SEVERE, "failed to save sqlite connection", ex);
		}
	}

	private void createTokensBank()
	{
		runAsync(() -> {
			final Connection connection = this.connection.get();
			if (connection == null)
			{
				return;
			}

			try
			{
				createBank(connection, EconomyController.DEFAULT_BANK);
			}
			catch (final SQLException ex)
			{
				plugin.getLogger().log(Level.SEVERE, "failed to create default bank table", ex);
			}
		});
	}


	private void createBank(@NotNull final Connection connection, @NotNull final String bank) throws SQLException
	{
		try (final PreparedStatement statement = connection.prepareStatement(Statements.CREATE.getStatement(bank)))
		{
			statement.execute();
		}
	}

	private void insertUser(@NotNull final Connection connection, @NotNull final String bank, @NotNull final UUID uuid, @NotNull final BigInteger balance) throws SQLException
	{
		try (final PreparedStatement statement = connection.prepareStatement(Statements.INSERT.getStatement(bank)))
		{
			statement.setString(1, uuid.toString());
			statement.setBigDecimal(2, new BigDecimal(balance));

			statement.execute();
		}
	}


	private enum Statements
	{
		CREATE("CREATE TABLE IF NOT EXISTS `bank_%s`(`uuid` CHAR(36) PRIMARY KEY NOT NULL, `balance` DECIMAL(38, 0) NOT NULL)"),

		SELECT("SELECT `balance` FROM `bank_%s` WHERE `uuid`=?"),

		INSERT("INSERT INTO `bank_%s`(`uuid`, `balance`) VALUES (?, ?) ON CONFLICT (`uuid`) DO UPDATE SET `balance`=excluded.balance");


		@NotNull
		private final String statement;


		Statements(@Language("SQLite") @NotNull final String statement)
		{
			this.statement = statement;
		}


		@NotNull
		public final String getStatement()
		{
			return statement;
		}

		@NotNull
		public final String getStatement(final Object... args)
		{
			return String.format(getStatement(), args);
		}


	}

}
