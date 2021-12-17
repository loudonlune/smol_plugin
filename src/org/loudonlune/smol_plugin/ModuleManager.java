package org.loudonlune.smol_plugin;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.loudonlune.smol_plugin.damage.EntityExplodeHandlingModule;
import org.loudonlune.smol_plugin.general.BedModule;
import org.loudonlune.smol_plugin.general.BossBarModule;
import org.loudonlune.smol_plugin.general.ChatTagModule;
import org.loudonlune.smol_plugin.general.KillAllModule;
import org.loudonlune.smol_plugin.general.ListEntityTypes;
import org.loudonlune.smol_plugin.general.SleepVotingModule;
import org.loudonlune.smol_plugin.inventory.OpenInvModule;
import org.loudonlune.smol_plugin.inventory.SwapInvModule;
import org.loudonlune.smol_plugin.utils.BossBarUtils;
import org.loudonlune.smol_plugin.utils.GeneralUtils;
import org.loudonlune.smol_plugin.utils.PathModule;
import org.loudonlune.smol_plugin.utils.SmolConfigurable;
import org.loudonlune.smol_plugin.utils.SmolModule;
import org.loudonlune.smol_plugin.utils.StatsUtils;

public final class ModuleManager extends SmolModule implements SmolConfigurable {
	private HashMap<String, SmolModule> modules;
	
	public ModuleManager(SmolPlugin master) {
		super(master);
		modules = new HashMap<String, SmolModule>();
	}
	
	public void constructModules() {
		modules.put("sleepvote", new SleepVotingModule(parent));
		modules.put("tag", new ChatTagModule(parent));
		modules.put("openinv", new OpenInvModule(parent));
		modules.put("swapinv", new SwapInvModule(parent));
		modules.put("smolplugin", new GeneralUtils(parent));
		modules.put("stats", new StatsUtils(parent));
		modules.put("bed", new BedModule(parent));
		modules.put("bossbarutils", new BossBarUtils(parent));
		modules.put("entityexplhandler", new EntityExplodeHandlingModule(parent));
		modules.put("bar", new BossBarModule(parent));
		modules.put("killall", new KillAllModule(parent));
		modules.put("ents", new ListEntityTypes(parent));
		modules.put("paths", new PathModule(parent));
	}

	public void register() {
		register(null);
	}
	
	public SmolModule getModule(String moduleName) {
		return modules.get(moduleName);
	}
	
	@Override
	public SmolModule register(String registeredName) {
		for (Entry<String, SmolModule> entry : modules.entrySet())
			entry.getValue().register(entry.getKey());
		
		return this;
	}

	@Override
	public void writeOnto(ConfigurationSection sect) {
		for (SmolModule mod : modules.values())
			if (mod instanceof SmolConfigurable) ((SmolConfigurable) mod).writeOnto(sect);
	}

	@Override
	public void readFrom(ConfigurationSection sect) {
		for (SmolModule mod : modules.values())
			if (mod instanceof SmolConfigurable) ((SmolConfigurable) mod).readFrom(sect);
	}
}
