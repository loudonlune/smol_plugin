package org.loudonlune.smol_plugin.damage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.loudonlune.smol_plugin.SmolPlugin;
import org.loudonlune.smol_plugin.utils.SmolConfigurable;
import org.loudonlune.smol_plugin.utils.SmolEventListener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityExplodeHandlingModule extends SmolEventListener implements SmolConfigurable {
	private final static String CONFIG_NAME = "entity_explode_handler";
	
	private ConfigurationSection config;
	
	public EntityExplodeHandlingModule(SmolPlugin parent) {
		super(parent);
		config = null;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityExplode(EntityExplodeEvent eee) {
		for (EntityType et : EntityType.values()) {
			String name = et.toString().toLowerCase();
			
			if (config.getBoolean(name))
				eee.setCancelled(true);
		}
	}

	@Override
	public void writeOnto(ConfigurationSection sect) {}

	@Override
	public void readFrom(ConfigurationSection sect) {
		if (!sect.isConfigurationSection(CONFIG_NAME)) {
			config = sect.createSection(CONFIG_NAME);
			
			for (EntityType et : EntityType.values())
				config.set(et.toString(), false);
		} else config = sect.getConfigurationSection(CONFIG_NAME);
	}
	
}
