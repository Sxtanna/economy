package com.sxtanna.mc.mods;

import com.sxtanna.mc.EconomyPlugin;
import com.sxtanna.mc.conf.mods.ModifierSettings;
import com.sxtanna.mc.cont.EconomyController;
import com.sxtanna.mc.data.User;
import com.sxtanna.mc.data.mods.MonetaryGroup;
import com.sxtanna.mc.data.mods.MonetaryLimit;
import com.sxtanna.mc.data.mods.MonetaryRange;
import com.sxtanna.mc.lang.Lang;
import com.sxtanna.mc.mods.base.Module;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class BalanceModifier extends Module
{

	private final Map<UUID, Long>       start = new HashMap<>();
	private final Map<UUID, Long>       limit = new HashMap<>();
	private final Map<UUID, BigInteger> gains = new HashMap<>();
	private final Map<UUID, UUID>       await = new HashMap<>();


	public BalanceModifier(@NotNull final EconomyPlugin plugin)
	{
		super(plugin);
	}


	@Override
	public void kill()
	{
		super.kill();

		start.clear();
		limit.clear();
		gains.clear();
	}


	public Map<UUID, UUID> getAwait()
	{
		return await;
	}


	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onKill(@NotNull final EntityDeathEvent event)
	{
		final Player killer = event.getEntity().getKiller();
		if (killer == null)
		{
			return;
		}

		final Optional<MonetaryRange> range = plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.REWARDS).get(EconomyController.DEFAULT_BANK).getRange(event.getEntityType());
		if (!range.isPresent())
		{
			return;
		}

		final Optional<User> user = plugin.getController()
										  // find bank
										  .flatMap(cont -> cont.findBank(EconomyController.DEFAULT_BANK))
										  // find user
										  .flatMap(bank -> bank.findUser(killer.getUniqueId()));
		if (!user.isPresent())
		{
			return;
		}

		// resolve random gain value
		final BigInteger gain = range.get().random();

		if (!recordGain(killer, gain))
		{

			final Long prev = limit.get(killer.getUniqueId());
			if (prev != null && (System.currentTimeMillis() - prev) < TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES))
			{
				return;
			}


			if (plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_SHOW_LIMIT_MESSAGE))
			{
				plugin.sendMessage(killer, Lang.GAINS__MESSAGE__LIMIT,

								   "{tokens}",
								   gain.toString());
			}

			final Location location = event.getEntity().getLocation();

			if (plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_PLAY_LIMIT_SOUND))
			{
				killer.playSound(location,
								 plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_LIMIT_SOUND),
								 plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_LIMIT_SOUND_VOLUME).floatValue(),
								 plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_LIMIT_SOUND_PITCH).floatValue());
			}

			limit.put(killer.getUniqueId(), System.currentTimeMillis());

			return;
		}


		// deposit random value based on gain
		user.get().add(gain);


		if (plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_SHOW_CHAT_MESSAGE))
		{
			plugin.sendMessage(killer, Lang.GAINS__MESSAGE__CHAT,

							   "{tokens}",
							   gain.toString());
		}

		if (plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_SHOW_HOLO_MESSAGE))
		{
			final Location location = event.getEntity().getLocation();

			spawnHologram(killer, location, gain);

			if (plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_PLAY_HOLO_SOUND))
			{
				killer.playSound(location,
								 plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_HOLO_SOUND),
								 plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_HOLO_SOUND_VOLUME).floatValue(),
								 plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_HOLO_SOUND_PITCH).floatValue());
			}
		}
	}


	private void spawnHologram(@NotNull final Player source, @NotNull final Location location, @NotNull final BigInteger gain)
	{
		final int time = plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_HOLO_FLOAT_TIME);

		final PotionEffect effect = new PotionEffect(PotionEffectType.LEVITATION,
													 20,
													 plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.GAINS_HOLO_FLOAT_MULT), false, false);


		final ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class, (entity) -> {

			await.put(entity.getUniqueId(), source.getUniqueId());

			entity.setSmall(true);
			entity.setVisible(false);

			entity.setCustomName(plugin.formMessage(source, Lang.GAINS__MESSAGE__HOLO, "{tokens}", gain.toString()));
			entity.setCustomNameVisible(true);

			entity.addPotionEffect(effect);

		});

		plugin.getServer().getScheduler().runTaskLater(plugin, () -> stand.setGravity(false), effect.getDuration());
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			stand.remove();
			await.remove(stand.getUniqueId());
		}, time);
	}

	private boolean recordGain(@NotNull final Player player, @NotNull final BigInteger amount)
	{
		final long start = this.start.computeIfAbsent(player.getUniqueId(), ($) -> System.currentTimeMillis());

		if ((System.currentTimeMillis() - start) >= TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS))
		{

			this.start.remove(player.getUniqueId());
			this.gains.remove(player.getUniqueId());

			return true;
		}

		this.gains.compute(player.getUniqueId(), ($0, old) -> (old == null ? BigInteger.ZERO : old).add(amount));

		return canGain(player);
	}

	public int currentGained(@NotNull final UUID uuid)
	{
		return Optional.ofNullable(gains.get(uuid)).orElse(BigInteger.ZERO).intValue();
	}

	public int resolveGainLimit(@NotNull final Player player)
	{
		final MonetaryLimit limit = plugin.getConfiguration().getPropertyOrDefault(ModifierSettings.LIMITS);

		final List<MonetaryGroup> groups = new ArrayList<>(limit.getGroups().values());
		Collections.reverse(groups);

		for (final MonetaryGroup group : groups)
		{
			if (!player.hasPermission(group.getPerm()))
			{
				continue;
			}

			return group.getMax();
		}

		return -1;
	}

	public long timeLeftIn(@NotNull final UUID uuid, @NotNull final TimeUnit unit)
	{
		final Long start = this.start.get(uuid);
		if (start == null)
		{
			return 0;
		}

		return unit.convert((start + TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS)) - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	private boolean canGain(@NotNull final Player player)
	{
		final BigInteger gained = this.gains.get(player.getUniqueId());
		return gained == null || gained.intValue() < resolveGainLimit(player);
	}

}
