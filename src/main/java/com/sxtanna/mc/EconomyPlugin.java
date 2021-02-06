package com.sxtanna.mc;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.PaperCommandManager;
import co.aikar.locales.MessageKeyProvider;
import com.sxtanna.mc.cmds.EconomyCommand;
import com.sxtanna.mc.conf.EconomyConfiguration;
import com.sxtanna.mc.conf.repo.RepositorySettings;
import com.sxtanna.mc.cont.EconomyController;
import com.sxtanna.mc.cont.impl.EconomyControllerLocal;
import com.sxtanna.mc.hook.Replacer;
import com.sxtanna.mc.hook.papi.EconomyReplacerPAPI;
import com.sxtanna.mc.hook.plib.HoloBlocker;
import com.sxtanna.mc.mods.BalanceModifier;
import com.sxtanna.mc.repo.EconomyRepository;
import com.sxtanna.mc.repo.impl.EconomyRepositoryHikari;
import com.sxtanna.mc.repo.impl.EconomyRepositorySQLite;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;

import static com.google.common.io.Files.getFileExtension;
import static com.google.common.io.Files.getNameWithoutExtension;

public final class EconomyPlugin extends JavaPlugin
{

	{
		saveDefaultConfig();
	}


	@NotNull
	private final EconomyConfiguration configuration = new EconomyConfiguration(new File(getDataFolder(), "config.yml"));


	@Nullable
	private EconomyController controller;
	@Nullable
	private EconomyRepository repository;

	@NotNull
	private final Replacer        replacer = new Replacer();
	@NotNull
	private final BalanceModifier modifier = new BalanceModifier(this);


	private PaperCommandManager commandManager;


	@Override
	public void onLoad()
	{
		init();
	}

	@Override
	public void onEnable()
	{
		load();

		this.modifier.load();

		saveDefaultLanguageFiles();
		initializeAikarCommandManager();
		loadStorageLanguageFiles();

		attemptToRegisterProtocolLib();
		attemptToRegisterPlaceholders();
	}


	@Override
	public void onDisable()
	{
		kill();

		this.modifier.kill();
	}


	@NotNull
	public Replacer getReplacer()
	{
		return replacer;
	}

	@NotNull
	public BalanceModifier getModifier()
	{
		return modifier;
	}


	@NotNull
	public Optional<EconomyController> getController()
	{
		return Optional.ofNullable(this.controller);
	}

	@NotNull
	public Optional<EconomyRepository> getRepository()
	{
		return Optional.ofNullable(this.repository);
	}


	public String formMessage(@NotNull final CommandSender sender, @NotNull final MessageKeyProvider provider, final String... replacements)
	{
		final BukkitCommandIssuer issuer  = this.commandManager.getCommandIssuer(sender);
		final String              message = this.commandManager.getLocales().getMessage(issuer, provider.getMessageKey());

		return ChatColor.translateAlternateColorCodes('&', replacer.apply(issuer.getPlayer(), replacements.length == 0 ? message : ACFUtil.replaceStrings(message, replacements)));
	}

	public void sendMessage(@NotNull final CommandSender sender, @NotNull final MessageKeyProvider provider, final String... replacements)
	{
		final BukkitCommandIssuer issuer  = this.commandManager.getCommandIssuer(sender);
		final String              message = this.commandManager.getLocales().getMessage(issuer, provider.getMessageKey());

		issuer.sendMessage(ChatColor.translateAlternateColorCodes('&', replacer.apply(issuer.getPlayer(), replacements.length == 0 ? message : ACFUtil.replaceStrings(message, replacements))));
	}


	@NotNull
	@ApiStatus.Internal
	public EconomyConfiguration getConfiguration()
	{
		return configuration;
	}


	@NotNull
	@ApiStatus.Internal
	public Optional<Throwable> reload()
	{
		try
		{
			kill();

			configuration.reload();

			loadStorageLanguageFiles();

			init();

			load();
		}
		catch (final Throwable ex)
		{
			return Optional.of(ex);
		}

		return Optional.empty();
	}


	private void load()
	{
		final EconomyController controller = this.controller;
		final EconomyRepository repository = this.repository;

		if (controller == null || repository == null)
		{
			return; // log?
		}

		repository.load();
		controller.load();
	}

	private void kill()
	{
		final EconomyController controller = this.controller;
		final EconomyRepository repository = this.repository;

		if (controller != null)
		{
			controller.kill();
		}
		if (repository != null)
		{
			repository.kill();
		}

		this.controller = null;
		this.repository = null;
	}


	private void init()
	{
		final EconomyController controller = new EconomyControllerLocal(this);
		final EconomyRepository repository;

		switch (this.configuration.getPropertyOrDefault(RepositorySettings.REPOSITORY_TYPE))
		{
			case LOCAL:
				repository = new EconomyRepositorySQLite(this);
				break;
			case MYSQL:
				repository = new EconomyRepositoryHikari(this);
				break;
			default:
				throw new UnsupportedOperationException("repository type not implemented!");
		}

		this.controller = controller;
		this.repository = repository;
	}


	private void saveDefaultLanguageFiles()
	{
		final File path = new File(getDataFolder(), "lang");
		if (!path.exists() && !path.mkdirs())
		{
			return;
		}

		final String[] langs = {"en-US.yml"};

		for (final String lang : langs)
		{
			final File file = new File(path, lang);
			if (file.exists())
			{
				continue;
			}

			try (final InputStream resource = getResource("lang/" + lang))
			{
				if (resource == null)
				{
					continue;
				}

				Files.copy(resource, file.toPath());
			}
			catch (IOException ex)
			{
				getLogger().log(Level.WARNING, "could not save language file for " + lang, ex);
			}
		}
	}

	private void loadStorageLanguageFiles()
	{
		//noinspection UnstableApiUsage
		final File[] langs = new File(getDataFolder(), "lang").listFiles(($, name) -> getFileExtension(name).equalsIgnoreCase("yml"));
		if (langs == null || langs.length <= 0)
		{
			return;
		}


		for (final File lang : langs)
		{
			//noinspection UnstableApiUsage
			final Locale locale = Locale.forLanguageTag(getNameWithoutExtension(lang.getName()));

			this.commandManager.addSupportedLanguage(locale);
			try
			{
				this.commandManager.getLocales().loadYamlLanguageFile(lang, locale);

				getLogger().info("loaded locale: " + locale.getDisplayName());
			}
			catch (final IOException | InvalidConfigurationException ex)
			{
				getLogger().log(Level.WARNING, "could not load language file for " + lang, ex);
			}
		}
	}


	private void initializeAikarCommandManager()
	{
		this.commandManager = new PaperCommandManager(this);

		this.commandManager.enableUnstableAPI("help");
		this.commandManager.enableUnstableAPI("brigadier");
		this.commandManager.usePerIssuerLocale(true, true);

		// todo: register contexts and completions

		this.commandManager.registerCommand(new EconomyCommand(this));
	}

	private void attemptToRegisterProtocolLib()
	{
		if (!getServer().getPluginManager().isPluginEnabled("ProtocolLib"))
		{
			return;
		}

		try
		{
			new HoloBlocker(this).load();
		}
		catch (final Throwable ex)
		{
			getLogger().log(Level.SEVERE, "failed to register protocol holo blocker", ex);
		}
	}

	private void attemptToRegisterPlaceholders()
	{
		if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
		{
			return;
		}

		try
		{
			this.replacer.register(me.clip.placeholderapi.PlaceholderAPI::setPlaceholders);

			new EconomyReplacerPAPI(this).register();
		}
		catch (final Throwable ex)
		{
			getLogger().log(Level.SEVERE, "failed to register papi placeholders", ex);
		}
	}

}
