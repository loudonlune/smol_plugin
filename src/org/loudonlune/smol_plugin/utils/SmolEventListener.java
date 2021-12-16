package org.loudonlune.smol_plugin.utils;

import org.bukkit.event.Listener;
import org.loudonlune.smol_plugin.SmolPlugin;

public abstract class SmolEventListener extends SmolModule implements Listener {
	public SmolEventListener(SmolPlugin parent) {
		super(parent);
	}

	@Override
	public SmolModule register(String registeredName) {
		parent.getServer().getPluginManager().registerEvents(this, parent);
		parent.getLogger().info("[SmolModule: " + registeredName + "] Registration complete.");
		return this;
	}
}
