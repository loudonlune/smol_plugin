package org.loudonlune.smol_plugin.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.loudonlune.smol_plugin.SmolPlugin;

public abstract class SmolCommand extends SmolModule implements CommandExecutor {

	public SmolCommand(SmolPlugin parent) {
		super(parent);
	}
	
	protected List<OfflinePlayer> getPlayers(String[] args) {
		ArrayList<OfflinePlayer> players = new ArrayList<>();
		
		for (int i = 0; i < args.length; i++) {
			for (OfflinePlayer op : parent.getServer().getOfflinePlayers())
				if (op.getName().equals(args[i])) {
					players.add(op);
					break;
				}
		}
		
		return players;
	}
	
	protected boolean hasArg(String[] args, String arg) {
		for (String s : args)
			if (s.equalsIgnoreCase(arg))
				return true;
		
		return false;
	}
	
	protected double[] getNums(String[] args) {
		double[] nums = new double[0];
		
		for (String s : args) try {
			double num = Double.parseDouble(s);
			
			int newArrLength = 1 + nums.length;
			
			double[] newNums = new double[newArrLength];
			newNums[newNums.length - 1] = num;
			
			for (int i = 0; i < nums.length; i++)
				newNums[i] = nums[i];
			
			nums = newNums;
		} catch (Exception e) { continue; }
		
		return nums;
	}
	
	@Override
	public SmolModule register(String registeredName) {
		PluginCommand pluginCmd = parent.getCommand(registeredName);
		
		if (pluginCmd != null)
			pluginCmd.setExecutor(this);
		else {
			parent.getLogger().severe("[SmolMoudle: " + registeredName + "] This module is a command, but has no PluginCommand associated with it!");
		}
		
		parent.getLogger().info("[SmolModule: " + registeredName + "] Registration complete.");
		return this;
	}
}
