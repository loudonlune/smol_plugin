package org.loudonlune.smol_plugin.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.loudonlune.smol_plugin.SmolPlugin;

public class GeneralUtils extends SmolCommand {
	
	public GeneralUtils(SmolPlugin parent) {
		super(parent);
	}

	public void printPlayerList(CommandSender arg0, int page, boolean detail, int detailLevel,  boolean includeOffline) {
		OfflinePlayer[] players = parent.getServer().getOfflinePlayers();
		
		if (!includeOffline) {
			ArrayList<OfflinePlayer> onlinePlayers = new ArrayList<OfflinePlayer>();
			
			for (OfflinePlayer p : players)
				if (p.isOnline())
					onlinePlayers.add(p);
			
			players = new OfflinePlayer[onlinePlayers.size()];
			onlinePlayers.toArray(players);
		}

		if (detailLevel < 0 || detailLevel > 3)
			return;
		
		int cntPerPage = detail ? 4 - detailLevel : 10;
		
		int maxPages = (players.length / cntPerPage) + 1;
		if (page > maxPages)
			page = maxPages;
		
		arg0.sendMessage(ChatColor.YELLOW + (includeOffline ? "Offline p" : "P") + "layer list (Page " + page + " of " + maxPages + "): ");
		
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss MM/dd/yy");
		for (int i = (page - 1) * cntPerPage; i < page * cntPerPage && i < players.length; i++) {
			if (detail) {
				if (detailLevel >= 0) {
					arg0.sendMessage(ChatColor.YELLOW + " - Player " + i + ": ");
					arg0.sendMessage(ChatColor.YELLOW + "   - Name: " + players[i].getName());
					arg0.sendMessage(ChatColor.YELLOW + "   - Unique-ID: " + players[i].getUniqueId().toString());
					arg0.sendMessage(ChatColor.YELLOW + "   - Death Count: " + players[i].getStatistic(Statistic.DEATHS));
				}
				
				if (detailLevel >= 1) {
					arg0.sendMessage(ChatColor.YELLOW + "   - First Logon: " + sdf.format(players[i].getFirstPlayed()));
					arg0.sendMessage(ChatColor.YELLOW + "   - Last Logon: " + sdf.format(players[i].getLastSeen()));
				}	
				
				if (detailLevel >= 2) {
					arg0.sendMessage(ChatColor.YELLOW + "   - Has bed spawn location? " + (players[i].getBedSpawnLocation() != null));
					if (players[i].getBedSpawnLocation() != null) {
						Location loc = players[i].getBedSpawnLocation();
						arg0.sendMessage(ChatColor.YELLOW + "      - [ World: " + loc.getWorld().getName() + ", X: " + loc.getX() + ", Y: " + loc.getY() + ", Z: " + loc.getZ() + " ] ");
					}
					
					arg0.sendMessage(ChatColor.YELLOW + "   - Has played more than one minute? " + (players[i].getStatistic(Statistic.PLAY_ONE_MINUTE) > 0));
				}
				
				if (detailLevel >= 3) {
					arg0.sendMessage(ChatColor.YELLOW + "   - Has ever slept in bed? " + (players[i].getStatistic(Statistic.SLEEP_IN_BED) > 0));
					arg0.sendMessage(ChatColor.YELLOW + "   - Has ever stored in chest? " + (players[i].getStatistic(Statistic.CHEST_OPENED) > 0));
				}
				
			} else {
				arg0.sendMessage(ChatColor.YELLOW + " - " + players[i].getName());
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		if (hasArg(arg3, "reload")) {
			parent.reloadConfig();
			arg0.sendMessage(ChatColor.BLUE + "[smol_plugin] Configuration loaded from disk.");
			return true;
		}
		
		if (hasArg(arg3, "save")) {
			parent.saveConfig();
			arg0.sendMessage(ChatColor.BLUE + "[smol_plugin] Configuration saved to disk.");
			return true;
		}
		
		return false;
	}
	
}
