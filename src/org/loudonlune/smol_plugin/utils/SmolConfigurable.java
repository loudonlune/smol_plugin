package org.loudonlune.smol_plugin.utils;

import org.bukkit.configuration.ConfigurationSection;

public interface SmolConfigurable {
	public void writeOnto(ConfigurationSection sect);
	public void readFrom(ConfigurationSection sect);
}
