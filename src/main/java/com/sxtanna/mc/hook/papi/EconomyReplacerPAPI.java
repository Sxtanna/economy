package com.sxtanna.mc.hook.papi;

import com.sxtanna.mc.EconomyPlugin;
import com.sxtanna.mc.cont.EconomyController;
import com.sxtanna.mc.data.User;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class EconomyReplacerPAPI extends PlaceholderExpansion
{

	@NotNull
	private final EconomyPlugin plugin;


	public EconomyReplacerPAPI(@NotNull final EconomyPlugin plugin)
	{
		this.plugin = plugin;
	}


	@NotNull
	@Override
	public String getIdentifier()
	{
		return "tokens";
	}

	@NotNull
	@Override
	public String getAuthor()
	{
		return this.plugin.getDescription().getAuthors().get(0);
	}

	@NotNull
	@Override
	public String getVersion()
	{
		return this.plugin.getDescription().getVersion();
	}

	@Override
	public boolean persist()
	{
		return true;
	}


	@Override
	public String onRequest(@Nullable final OfflinePlayer player, @NotNull final String params)
	{
		final String[] args = params.split("_");
		if (args.length < 1 || player == null)
		{
			return null;
		}

		final Optional<EconomyController> controller = plugin.getController();
		if (!controller.isPresent())
		{
			return null;
		}


		switch (args[0].toLowerCase(Locale.ROOT))
		{
			case "balance":
				final BigInteger balance = controller.flatMap(cont -> cont.findUserInDefaultBank(player.getUniqueId())).map(User::getBalance).orElse(BigInteger.ZERO);
				return balance.toString();
			case "daily":
				if (args.length < 2)
				{
					return null;
				}

				switch (args[1].toLowerCase(Locale.ROOT))
				{
					case "max":
						return !(player instanceof Player) ? "0" : String.valueOf(plugin.getModifier().resolveGainLimit(((Player) player)));
					case "current":
						return String.valueOf(plugin.getModifier().currentGained(player.getUniqueId()));
					default:
						return null;
				}
			case "reset":

				final TimeUnit unit;

				if (args.length <= 1)
				{
					unit = TimeUnit.HOURS;
				}
				else
				{
					try
					{
						unit = TimeUnit.valueOf(args[1].toUpperCase());
					}
					catch (final IllegalArgumentException ex)
					{
						return null;
					}
				}

				return String.valueOf(plugin.getModifier().timeLeftIn(player.getUniqueId(), unit));
			default:
				return null;
		}
	}

}
