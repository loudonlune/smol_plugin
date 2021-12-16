package org.loudonlune.smol_plugin.utils;

import org.bukkit.event.Listener;
import org.loudonlune.smol_plugin.SmolPlugin;

public abstract class SmolCmdListener extends SmolCommand implements Listener {

	public SmolCmdListener(SmolPlugin parent) {
		super(parent);
	}
	
	@Override
	public SmolModule register(String registeredName) {
		super.register(registeredName);
		parent.getServer().getPluginManager().registerEvents(this, parent);
		parent.getLogger().info("[SmolModule: " + registeredName + "] Registration complete.");
		return this;
	}
	
}
