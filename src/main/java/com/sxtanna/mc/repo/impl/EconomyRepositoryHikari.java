package com.sxtanna.mc.repo.impl;

import com.sxtanna.mc.EconomyPlugin;
import com.sxtanna.mc.conf.repo.RepositorySettings;
import com.sxtanna.mc.cont.EconomyController;
import com.sxtanna.mc.data.Bank;
import com.sxtanna.mc.data.User;
import com.sxtanna.mc.data.impl.UserImpl;
import com.sxtanna.mc.repo.EconomyRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
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

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public final class EconomyRepositoryHikari implements EconomyRepository
{

	private static final String HIKARI_DRV = "org.mariadb.jdbc.Driver";
	private static final String HIKARI_URL = "jdbc:mariadb://%s:%d/%s?useSSL=%s";


	@NotNull
	private final EconomyPlugin plugin;

	@NotNull
	private final AtomicReference<HikariDataSource> pool = new AtomicReference<>();


	public EconomyRepositoryHikari(@NotNull final EconomyPlugin plugin)
	{
		this.plugin = plugin;
	}


	@Override
	public void load()
	{
		this.pool.set(new HikariDataSource(config()));

		createTokensBank();
	}

	@Override
	public void kill()
	{
		final HikariDataSource pool = this.pool.getAndSet(null);
		if (pool != null)
		{
			pool.close();
		}
	}


	@Override
	public CompletableFuture<Optional<User>> load(final @NotNull Bank bank, final @NotNull UUID uuid)
	{
		return supplyAsync(() -> {
			try (final Connection connection = this.pool.get().getConnection())
			{
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
			}
			catch (final SQLException ex)
			{
				throw new CompletionException(ex);
			}
		});
	}


	@Override
	public CompletableFuture<Void> save(final @NotNull Bank bank)
	{
		return runAsync(() -> {

			try (final Connection connection = this.pool.get().getConnection())
			{

				try
				{
					createBank(connection, bank.getName());
				}
				catch (final SQLException ex)
				{
					throw new CompletionException("failed to create bank table for " + bank.getName(), ex);
				}


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

			}
			catch (final SQLException ex)
			{
				throw new CompletionException("failed to open connection for insert ", ex);
			}

		});
	}

	@Override
	public CompletableFuture<Void> save(final @NotNull Bank bank, final @NotNull User user)
	{
		return runAsync(() -> {
			try (final Connection connection = this.pool.get().getConnection())
			{
				insertUser(connection, bank.getName(), user.getUuid(), user.getBalance());
			}
			catch (final SQLException ex)
			{
				throw new CompletionException("failed to insert bank user for " + user.getUuid(), ex);
			}
		});
	}


	@NotNull
	private HikariConfig config()
	{
		final HikariConfig config = new HikariConfig();

		final Optional<String>  host = plugin.getConfiguration().getPropertyOptional(RepositorySettings.REPOSITORY_HOST);
		final Optional<Integer> port = plugin.getConfiguration().getPropertyOptional(RepositorySettings.REPOSITORY_PORT);

		final Optional<String> database = plugin.getConfiguration().getPropertyOptional(RepositorySettings.REPOSITORY_DATABASE);
		final Optional<String> username = plugin.getConfiguration().getPropertyOptional(RepositorySettings.REPOSITORY_USERNAME);
		final Optional<String> password = plugin.getConfiguration().getPropertyOptional(RepositorySettings.REPOSITORY_PASSWORD);

		if (!host.isPresent() || !port.isPresent() || !database.isPresent() || !username.isPresent() || !password.isPresent())
		{
			throw new IllegalStateException(String.format("Invalid SQL Configuration: %s:%s '%s'@'%s' auth '%s'", host, port, username, database, password));
		}

		config.addDataSourceProperty("cachePrepStmts", true);
		config.addDataSourceProperty("prepStmtCacheSize", 250);
		config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
		config.addDataSourceProperty("useServerPrepStmts", true);
		config.addDataSourceProperty("cacheCallableStmts", true);
		config.addDataSourceProperty("elideSetAutoCommits", true);
		config.addDataSourceProperty("useLocalSessionState", true);
		config.addDataSourceProperty("alwaysSendSetIsolation", true);
		config.addDataSourceProperty("cacheResultSetMetadata", true);
		config.addDataSourceProperty("cacheServerConfiguration", true);

		config.setDriverClassName(HIKARI_DRV);
		config.setJdbcUrl(String.format(HIKARI_URL,
										// host
										host.get(),
										// port
										port.get(),
										// database
										database.get(),
										// useSSL
										false));

		config.setUsername(username.get());
		config.setPassword(password.get());

		return config;
	}


	private void createTokensBank()
	{
		runAsync(() -> {
			try (final Connection connection = this.pool.get().getConnection())
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

		INSERT("INSERT INTO `bank_%s`(`uuid`, `balance`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `balance`=VALUES(`balance`)");


		@NotNull
		private final String statement;


		Statements(@Language("MySQL") @NotNull final String statement)
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