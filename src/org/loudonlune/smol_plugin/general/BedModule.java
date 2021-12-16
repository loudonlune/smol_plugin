package org.loudonlune.smol_plugin.general;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.loudonlune.smol_plugin.SmolPlugin;
import org.loudonlune.smol_plugin.utils.SmolCommand;

public class BedModule extends SmolCommand {

	public BedModule(SmolPlugin parent) {
		super(parent);
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		boolean print = hasArg(arg3, "print");
		
		if (hasArg(arg3, "list")) {
			arg0.sendMessage(ChatColor.GREEN + "[Bed] List of bed spawn locations: ");
			
			for (OfflinePlayer op : parent.getServer().getOfflinePlayers()) {
				Location bedLocation = op.getBedSpawnLocation();
				
				if (bedLocation != null) {
					arg0.sendMessage(ChatColor.GREEN + "  - " + op.getName() + ": { World: " + bedLocation.getWorld().getName() + ", X: " + bedLocation.getBlockX() + ", Y: " + bedLocation.getBlockY() + ", Z: " + bedLocation.getBlockZ() + " } ");
				}
			}
			
			return true;
		}
		
		OfflinePlayer teleportPlayer = null;
		OfflinePlayer player = null;
		if (arg3.length < 1) {
			if (arg0 instanceof Player) {
				player = parent.getServer().getOfflinePlayer(((Player) arg0).getUniqueId());
				teleportPlayer = player;
			} else return false;
		} else {			
			List<OfflinePlayer> players = getPlayers(arg3);
			
			if (players.size() > 0) {
				teleportPlayer = players.get(0);
				
				if (players.size() > 1) {
					player = players.get(1);
					if (teleportPlayer == null || !teleportPlayer.isOnline()) {
						arg0.sendMessage(ChatColor.RED + "[Bed] With 2 player args, the first player argument MUST be an Online player!");
						return true;
					}
						
					if (player == null) {
						arg0.sendMessage(ChatColor.RED + "[Bed] Second player was not found.");
					}
				} else {
					if (!print) {
						if (arg0 instanceof Player) {
							player = teleportPlayer;
							teleportPlayer = parent.getServer().getOfflinePlayer(((Player) arg0).getUniqueId());
							
						} else arg0.sendMessage(ChatColor.RED + "[Bed] This command is only valid as a console sender with 1 arg if and only if the \"print\" arg is provided.");					
					} else {
						player = teleportPlayer;
					}
				}
			} else {
				arg0.sendMessage(ChatColor.RED + "[Bed] Failed to find player.");
				return true;
			}
		}
		
		if (player.getBedSpawnLocation() == null) {
			arg0.sendMessage(ChatColor.YELLOW + "[Bed] You must have a bed set for this command to function.");
			return true;
		}
		
		Location bedLocation = player.getBedSpawnLocation();
		if (bedLocation == null) {
			arg0.sendMessage(ChatColor.YELLOW + "[Bed] Player \"" + player.getName() + "\" has no bed.");
			return true;
		}
		
		if (print) {
			arg0.sendMessage(ChatColor.GREEN + "[Bed] Player's bed location: { World: " + bedLocation.getWorld().getName() + ", X: " + bedLocation.getBlockX() + ", Y: " + bedLocation.getBlockY() + ", Z: " + bedLocation.getBlockZ() + " } ");
		} else {
			teleportPlayer.getPlayer().teleport(bedLocation);
			arg0.sendMessage(ChatColor.GREEN + "[Bed] Teleported to bed.");
		}
		
		return true;
	}
	
}
