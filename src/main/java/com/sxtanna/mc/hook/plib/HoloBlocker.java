package com.sxtanna.mc.hook.plib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.sxtanna.mc.EconomyPlugin;
import com.sxtanna.mc.base.State;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class HoloBlocker implements State
{

	private static final int ARMOR_STAND_ID = 1;


	@NotNull
	private final EconomyPlugin   plugin;


	public HoloBlocker(@NotNull final EconomyPlugin plugin)
	{
		this.plugin = plugin;
	}


	@Override
	public void load()
	{
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.SPAWN_ENTITY_LIVING)
		{
			@Override
			public void onPacketSending(final PacketEvent event)
			{
				if (event.getPacket().getIntegers().getValues().get(1) != ARMOR_STAND_ID)
				{
					return; // not an armor stand
				}


				final UUID target = HoloBlocker.this.plugin.getModifier().getAwait().get(event.getPacket().getUUIDs().getValues().get(0));
				if (target == null)
				{
					return; // not our armor stand
				}

				if (!target.equals(event.getPlayer().getUniqueId()))
				{
					event.setCancelled(true); // cancel when not target player :D
				}
			}
		});
	}

	@Override
	public void kill()
	{

	}

}
