package org.loudonlune.smol_plugin.general;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.loudonlune.smol_plugin.SmolPlugin;
import org.loudonlune.smol_plugin.utils.BossBarUtils;
import org.loudonlune.smol_plugin.utils.SmolCommand;

public class BossBarModule extends SmolCommand {

	public BossBarModule(SmolPlugin parent) {
		super(parent);
	}
	
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		BossBarUtils bbu = parent.getBossBarUtils();
		
		if (hasArg(arg3, "remove")) {
			if (arg3.length < 2) {
				arg0.sendMessage(ChatColor.RED + "[Boss] The clear subcommand requires at least one argument.");
			}
			
			double[] nums = getNums(arg3);
			
			boolean result = false;
			if (nums.length > 0)
				result = bbu.remove((int) nums[0]);
			else
				result = bbu.remove(arg3[1]);
			
			if (result)
				arg0.sendMessage(ChatColor.GREEN + "[Boss] Bar has been removed.");
			else
				arg0.sendMessage(ChatColor.RED + "[Boss] Bar not found, no bars were removed.");
			
			return true;
		}
		
		if (hasArg(arg3, "set")) {
			if (arg3.length < 2) {
				arg0.sendMessage(ChatColor.RED + "[Boss] The set subcommand requires at least three arguments.");
				return true;
			}
			
			int dataNums = 0;
			double[] nums = getNums(arg3);
			
			KeyedBossBar bar = bbu.getBossBar(arg3[1]);
			
			if (bar == null) {
				if (nums.length > 0 && nums[0] < bbu.getMaxIDValue())
					bar = bbu.getBossBar((int) nums[0]);
				
				if (bar == null) {
					arg0.sendMessage(ChatColor.RED + "[Boss] Could not identify a boss bar tag.");
					arg0.sendMessage(ChatColor.RED + "[Boss] Make sure you are passing in a valid boss bar tag, or a valid boss bar ID as the first subcommand parameter.");
					return true;
				}
				
				dataNums++;
			}
			
			if (hasArg(arg3, "progress")) {
				double prog = nums[dataNums++];
				
				bar.setProgress(prog);
				arg0.sendMessage(ChatColor.GREEN + "[Boss] Set " + bar.getKey().getKey() + "'s progress has been set to: " + prog);
				return true;
			} 
			
			if (hasArg(arg3, "visible")) {
				boolean isTrue = hasArg(arg3, "true");
				
				bar.setVisible(isTrue);
				arg0.sendMessage(ChatColor.GREEN + "[Boss] Set " + bar.getKey().getKey() + "'s visibility to " + isTrue);
				return true;
			}
			
			arg0.sendMessage(ChatColor.YELLOW + "[Boss] Could not resolve a valid parameter to set on " + bar.getKey().getKey());
			arg0.sendMessage(ChatColor.YELLOW + "Parameters: ");
			arg0.sendMessage(ChatColor.YELLOW + "  - progress (double)");
			
			return true;
		}
		
		if (hasArg(arg3, "subscribe")) {
			if (arg3.length < 2) {
				arg0.sendMessage("The subscribe subcommand requires at least 2 arguments.");
			}
			
			double[] nums = getNums(arg3);
			KeyedBossBar bar = null;
			
			if (bar == null) {
				if (nums.length > 0 && nums[0] < bbu.getMaxIDValue())
					bar = bbu.getBossBar((int) nums[0]);
				
				if (bar == null) {
					arg0.sendMessage(ChatColor.RED + "[Boss] Could not identify a boss bar tag.");
					arg0.sendMessage(ChatColor.RED + "[Boss] Make sure you are passing in a valid boss bar tag, or a valid boss bar ID as the first subcommand parameter.");
					return true;
				}
			}
			
			if (hasArg(arg3, "@a")) {
				for (Player p : parent.getServer().getOnlinePlayers())
					bar.addPlayer(p);
				
				
				return true;
			} else {	
				List<OfflinePlayer> players = getPlayers(arg3);
				int failed = 0;
				
				for (OfflinePlayer p : players) {
					if (p.isOnline()) {
						bar.addPlayer(p.getPlayer());
					} else {
						failed++;
						arg0.sendMessage(ChatColor.YELLOW + "[Boss] Player " + p.getName() + " is offline. Players must be online. This player has NOT been added to the boss bar.");
					}
				}
				
				arg0.sendMessage(ChatColor.GREEN + "[Boss] " + (players.size() - failed) + " players have been subscribed to " + bar.getKey().getKey());
				return true;
			}
			
		}
		
		if (hasArg(arg3, "add")) {
			if (arg3.length < 2) {
				arg0.sendMessage("The add subcommand requires at least 2 arguments.");
				return true;
			}
			
			String name = arg3[1];
			String title = arg3[2];
			BarColor color = BarColor.PURPLE;
			BarStyle style = BarStyle.SOLID;
			
			BarFlag[] flags = null;
			
			if (arg3.length >= 4) {
				color = BarColor.valueOf(arg3[3].toLowerCase());
				
				if (color == null) {
					arg0.sendMessage(ChatColor.RED + "[Boss] Invalid BarColor value. Valid values are: ");
					for (BarColor cv : BarColor.values()) 
						arg0.sendMessage("  - " + cv.toString().toLowerCase());
					return true;
				}
				
				if (arg3.length >= 5) {
					style = BarStyle.valueOf(arg3[4].toLowerCase());
					
					if (style == null) {
						arg0.sendMessage(ChatColor.RED + "[Boss] Invalid BarStyle value. Valid values are: ");
						for (BarStyle sv : BarStyle.values()) 
							arg0.sendMessage("  - " + sv.toString().toLowerCase());
						return true;
					}
					
					if (arg3.length >= 6) {
						int readFlagsUntil = arg3.length < 9 ? arg3.length : 9;
						
						flags = new BarFlag[readFlagsUntil - 6];
						for (int i = 6; i < readFlagsUntil; i++) {
							BarFlag flag = BarFlag.valueOf(arg3[i].toUpperCase());
							
							if (flag == null) {
								arg0.sendMessage(ChatColor.RED + "[Boss] Invalid BarStyle value. Valid values are: ");
								for (BarFlag fv : BarFlag.values()) 
									arg0.sendMessage("  - " + fv.toString().toLowerCase());
								return true;
							}
							
							flags[i - 5] = flag;
						}
					}
				}
			}
			
			if (flags == null)
				bbu.createBossBar(name, title, color, style);
			else
				bbu.createBossBar(name, title, color, style, flags);
				
			arg0.sendMessage(ChatColor.GREEN + "[Boss] Added new boss bar: " + name);
			
			return true;
		}
		
		return false;
	}

}
