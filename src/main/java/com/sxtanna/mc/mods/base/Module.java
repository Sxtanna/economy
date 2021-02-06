package com.sxtanna.mc.mods.base;

import com.sxtanna.mc.EconomyPlugin;
import com.sxtanna.mc.base.State;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public abstract class Module implements State, Listener
{

	@NotNull
	protected final EconomyPlugin plugin;


	protected Module(@NotNull final EconomyPlugin plugin)
	{
		this.plugin = plugin;
	}


	@Override
	public void load()
	{
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}

	@Override
	public void kill()
	{
		HandlerList.unregisterAll(this);
	}

}
