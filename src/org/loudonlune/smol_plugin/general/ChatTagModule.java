package org.loudonlune.smol_plugin.general;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.loudonlune.smol_plugin.SmolPlugin;
import org.loudonlune.smol_plugin.utils.SmolCmdListener;
import org.loudonlune.smol_plugin.utils.SmolConfigurable;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;

public class ChatTagModule extends SmolCmdListener implements SmolConfigurable {
	private HashMap<UUID, String> tagMap;
	
	public ChatTagModule(SmolPlugin parent) {
		super(parent);
		tagMap = new HashMap<UUID, String>();
	}
	
	public void save() {
		ConfigurationSection tagSect;
		if (!parent.config.isConfigurationSection("tags"))
			tagSect = parent.config.createSection("tags");
		else
			tagSect = parent.config.getConfigurationSection("tags");
		
		for (Entry<UUID, String> tag : tagMap.entrySet()) {
			tagSect.set(tag.getKey().toString(), tag.getValue());
		}
	}
	
	public boolean hasTag(UUID player) {
		return tagMap.containsKey(player);
	}
	
	public String getTag(UUID key) {
		return ChatColor.translateAlternateColorCodes('&', tagMap.get(key));
	}
	
	@EventHandler
	public void onAsyncChatEvent(AsyncChatEvent apce) {
		Component prefix = Component.text("");
		
		UUID playerID = apce.getPlayer().getUniqueId();
		if (hasTag(playerID))
			prefix.append(Component.text(getTag(playerID) + " "));
		
		if (apce.getPlayer().isSleeping())
			prefix = prefix.append(Component.text("[ZZZ] ")
					.color(
							TextColor.color(
										ChatColor.YELLOW.getColor().getRGB()
									)
							)
					);
		
		if (apce.getPlayer().getWorld() != parent.getServer().getWorld("world"))
			prefix = prefix.append(Component.text("[Off-World] ")
					.color(
							TextColor.color(
										ChatColor.BLUE.getColor().getRGB()
									)
							)
						);
		
		if (apce.getPlayer().isOp())
			prefix = prefix.append(Component.text("[Operator] ")
					.color(
							TextColor.color(
										ChatColor.DARK_RED.getColor().getRGB()
									)
							)
					);
		else
			prefix = prefix.append(Component.text("[User] ")
					.color(
							TextColor.color(
										ChatColor.GREEN.getColor().getRGB()
									)
							)
					);
		
		prefix = prefix.append(Component.text(apce.getPlayer().getName().toString() + ": "));
		
		Bukkit.getServer().sendMessage(prefix.append(apce.message()));
		
		// cancel the chat event
		apce.setCancelled(true);
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		if (arg3.length < 1) {
			arg0.sendMessage("This command allows operators to tag users. Subcommands:");
			arg0.sendMessage(" - set <tag> [player]");
			arg0.sendMessage(" - clear <player>");
			arg0.sendMessage(" - list");
			arg0.sendMessage("You must provide at least 1 argument.");
			return false;
		}
		
		if (arg3[0].equals("set")) {
			if (arg3.length < 2) {
				arg0.sendMessage("At least 2 args required for set subcmd.");
				return false;
			}
			
			String tag = arg3[1];
			
			for (int i = 2; i < arg3.length - 1; i++)
				tag += " " + arg3[i];
			
			Player p = null;
			if (arg3.length > 2) {
				if (arg0.hasPermission("smol_plugin.tag.others"))
					p = parent.getServer().getPlayer(arg3[arg3.length - 1]);

				if (p == null) {
					tag += " " + arg3[arg3.length - 1];
				}
			} 
			
			if (p == null) {
				if (!(arg0 instanceof Player)) {
					arg0.sendMessage("A valid player name is required for the last argument.");
					return false;
				}
				
				p = (Player) arg0;
			}
			
			tagMap.put(p.getUniqueId(), tag);
			
			arg0.sendMessage(ChatColor.YELLOW + p.getName() + "'s tag has been set to: \"" + getTag(p.getUniqueId()) + ChatColor.YELLOW + "\"");
		} else if (arg3[0].equals("clear")) {
			if (arg3.length < 1 || arg3.length > 2) {
				arg0.sendMessage("Subcmd clear only takes 1 optional argument.");
				return false;
			}
			
			String cmpName = null;
			if (arg3.length == 2 && arg0.hasPermission("smol_plugin.tag.others")) {
				cmpName = arg3[1];
				
			} else {
				if (!(arg0 instanceof Player)) {
					arg0.sendMessage("A valid player name is required for the last argument.");
					return false;
				}
				
				cmpName = ((Player) arg0).getName();
			}
			
			if (cmpName == null) {
				arg0.sendMessage("cmpName could not be gathered.");
				return false;
			}
			
			UUID toRemove = null;
			for (UUID uid : tagMap.keySet()) {
				Player thp = parent.getServer().getPlayer(uid);
				if (thp != null && thp.getName().equals(cmpName)) {
					toRemove = uid;
				}
			}
				
			
			if (toRemove != null) {
				tagMap.remove(toRemove);
				arg0.sendMessage(ChatColor.YELLOW + "Tag cleared for player " + toRemove.toString());
			} else {
				arg0.sendMessage("Player has no tag.");
				return false;
			}
		} else if (arg3[0].equals("list")) {
			arg0.sendMessage("Tag list:");
			for (UUID player : tagMap.keySet())
				arg0.sendMessage("    " + parent.getServer().getOfflinePlayer(player).getName() + " - " + getTag(player) + ChatColor.RESET + " ");
		} else {
			arg0.sendMessage("Unknown subcmd.");
			return false;
		}
		
		return true;
	}

	@Override
	public void writeOnto(ConfigurationSection sect) {
		ConfigurationSection ourSect;
		if (sect.isConfigurationSection("tag"))
			ourSect = sect.getConfigurationSection("tag");
		else
			ourSect = sect.createSection("tag");
		
		for (Entry<UUID, String> entry : tagMap.entrySet())
			ourSect.set(entry.getKey().toString(), entry.getValue());
	}

	@Override
	public void readFrom(ConfigurationSection sect) {
		ConfigurationSection ourSect;
		if (sect.isConfigurationSection("tag"))
			ourSect = sect.getConfigurationSection("tag");
		else
			ourSect = sect.createSection("tag");
		
		for (String key : ourSect.getKeys(false))
			tagMap.put(UUID.fromString(key), ourSect.getString(key));
		
	}
	
}
