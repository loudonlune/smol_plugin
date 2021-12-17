package org.loudonlune.smol_plugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.loudonlune.smol_plugin.utils.BossBarUtils;
import org.loudonlune.smol_plugin.utils.GeneralUtils;
import org.loudonlune.smol_plugin.utils.InventoryUtils;
import org.loudonlune.smol_plugin.utils.StatsUtils;
import org.loudonlune.smol_plugin.web.WebService;
import org.loudonlune.smol_plugin.web.stats.StatsAPI;

public class SmolPlugin extends JavaPlugin {
	public ConfigurationSection config;
	private ModuleManager masterModule;
	private WebService webService;
	
	public WebService getWebService() {
		return webService;
	}
	
	public InventoryUtils getInventoryUtils() {
		return (InventoryUtils) masterModule.getModule("invutils");
	}
	
	public GeneralUtils getGeneralUtils() {
		return (GeneralUtils) masterModule.getModule("smolplugin");
	}
	
	public BossBarUtils getBossBarUtils() {
		return (BossBarUtils) masterModule.getModule("bossbarutils");
	}
	
	public StatsUtils getStatsUtils() {
		return (StatsUtils) masterModule.getModule("stats");
	}
	
	public ModuleManager getModuleManager() {
		return masterModule;
	}
	
	public SmolPlugin() {
		super();
		Bukkit.getLogger().info("[smol_plugin] Constructing module manager...");
		masterModule = new ModuleManager(this);
		masterModule.constructModules();
		Bukkit.getLogger().info("[smol_plugin] Construction complete.");
	}
	
	private StatsAPI statsAPI;
	private void initializeAPIs() {
		statsAPI = new StatsAPI(getStatsUtils());
		webService.getAPI().getRoot().addMember(statsAPI);
	}
	
	@Override
	public void onEnable() {
		config = getConfig();
		webService = new WebService(this);
		initializeAPIs();
		
		try {
			webService.getWebServer().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		masterModule.readFrom(config);
		masterModule.register();
		
		Bukkit.getLogger().info("[smol_plugin] Initialization complete.");
	}
	
	public static void main(String[] args) {
		System.out.println("Put this file in your plugins directory!");
	}
	
	@Override
	public void onDisable() {		
		masterModule.writeOnto(config);
		
		try {
			webService.getWebServer().stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Bukkit.getLogger().info("[smol_plugin] Disabled");
		saveConfig();
	}
	
}
