package org.loudonlune.smol_plugin.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.loudonlune.smol_plugin.SmolPlugin;

public class StatsUtils extends SmolCommand {
	
	public StatsUtils(SmolPlugin parent) {
		super(parent);
	}
	
	public int forceGetStatistic(OfflinePlayer player, Statistic stat) {
		int statData = 0;
		
		switch (stat.getType()) {
		case UNTYPED:
			statData = player.getStatistic(stat);
			break;
		case BLOCK: {
				int summation = 0;
				for (Material m : Material.values())
					if (m.isBlock())
						summation += player.getStatistic(stat, m);

				statData = summation;
				break;
			}
		case ENTITY: {
				int summation = 0;
				for (EntityType et : EntityType.values()) {
					try {
						summation += player.getStatistic(stat, et);
					} catch (Exception e) {}
				}
			
				statData = summation;
				break;
			}
		case ITEM: {
				int summation = 0;
				for (Material m : Material.values())
					if (m.isItem())
						summation += player.getStatistic(stat, m);
				
				statData = summation;
				break;
			}
		}
		
		return statData;
	}
	
	public List<String> getPlayerNameList() {
		ArrayList<String> name_list = new ArrayList<>();
		
		for (OfflinePlayer op : Bukkit.getOfflinePlayers())
			name_list.add(op.getName());
		
		return name_list;
	}
	
	public HashMap<Statistic, Integer> getStats(String playerName) {
		HashMap<Statistic, Integer> results = new HashMap<>();
		OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(playerName);
		
		for (Statistic s : Statistic.values()) {
			int stat = forceGetStatistic(player, s);
			
			results.put(s, Integer.valueOf(stat));
		}
		
		return results;
	}
	
	public BufferedImage getPlayerHead(String playerName) {
		OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(playerName);
		if (player == null) return null;
		UUID pid = player.getUniqueId();
		
		try {
			URL apiURL = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + pid.toString() + "?unsigned=false");
			
			InputStreamReader isr = new InputStreamReader(apiURL.openStream());
			JSONObject obj = (JSONObject) new JSONParser().parse(isr);
			JSONObject texProperty = (JSONObject) ((JSONArray) obj.get("properties")).get(0);
			String textureBase64 = (String) texProperty.get("value");
			String result = new String(Base64.getDecoder().decode(textureBase64));
			
			JSONObject obj2 = (JSONObject) new JSONParser().parse(result);
			
			BufferedImage skin = ImageIO.read(new URL((String) ((JSONObject) ((JSONObject) obj2.get("textures")).get("SKIN")).get("url")));
			BufferedImage head = skin.getSubimage(8, 8, 8, 8);
			BufferedImage headForeground = skin.getSubimage(40, 8, 8, 8);
			
			head.createGraphics().drawImage(headForeground, null, 0, 0);
			
			return head;
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public HashMap<OfflinePlayer, Integer> precacheStatsMaterial(OfflinePlayer[] players, Statistic stat, Material mat) {
		if ((stat.getType() == Statistic.Type.ITEM && !mat.isItem()) || (stat.getType() == Statistic.Type.BLOCK && !mat.isBlock())) 
			throw new IllegalArgumentException("Stat type must match material type.");
		
		HashMap<OfflinePlayer, Integer> precachedValues = new HashMap<OfflinePlayer, Integer>();
		for (OfflinePlayer player : players) {
			precachedValues.put(player, player.getStatistic(stat, mat));
		}
		
		return precachedValues;
	}
	
	public HashMap<OfflinePlayer, Integer> precacheStatsEntity(OfflinePlayer[] players, Statistic stat, EntityType et) {
		HashMap<OfflinePlayer, Integer> precachedValues = new HashMap<OfflinePlayer, Integer>();
		for (OfflinePlayer player : players) {
			precachedValues.put(player, player.getStatistic(stat, et));
		}
		
		return precachedValues;
	}
	
	public HashMap<OfflinePlayer, Integer> precacheStats(OfflinePlayer[] players, Statistic stat) {
		HashMap<OfflinePlayer, Integer> precachedValues = new HashMap<OfflinePlayer, Integer>();
		for (OfflinePlayer player : players) {
			precachedValues.put(player, player.getStatistic(stat));
		}
		
		return precachedValues;
	}
	
	public void statsList(CommandSender arg0, double[] nums, String type) {
		Statistic[] statValues = Statistic.values();
		
		ArrayList<Statistic> toPrint = new ArrayList<>();
		if (type != null) {
			for (Statistic s : statValues) {
				if (s.getType().toString().equals(type.toUpperCase()))
					toPrint.add(s);
			}
		} else {
			toPrint.addAll(Arrays.asList(statValues));
		}
		
		int maxPages = (toPrint.size() / 10) + 1;
		
		int page = 1;
		if (nums.length > 0)
			page = (int) nums[0];
		
		if (page > maxPages)
			page = maxPages;
		
		arg0.sendMessage(ChatColor.GREEN + "Stats enumerated list (Page " + page + " of " + maxPages + "): ");
		
		for (int i = (page - 1) * 10; i < page * 10 && i < toPrint.size(); i++)
			arg0.sendMessage(ChatColor.GREEN + " - [" + toPrint.get(i).getType().toString() + "] " + toPrint.get(i).toString().toLowerCase());
	}
	
	public void printMaterialStatLine(CommandSender arg0, Statistic stat, Material mat) {
		String message = "  - " + mat.toString();
		
		int value = -1;
		OfflinePlayer maximum = null;
		for (OfflinePlayer op : parent.getServer().getOfflinePlayers()) {
			int cv = op.getStatistic(stat, mat);
			if (cv > value) {
				value = cv;
				maximum = op;
			}
		}
		
		if (maximum == null || value <= 0) {
			arg0.sendMessage(ChatColor.YELLOW + message + " - [No Record Holder]");
		} else {
			arg0.sendMessage(ChatColor.GREEN + message + " = " + maximum.getName() + " ["+ value + "]");
		}
	}
	
	public void printEntityStatLine(CommandSender arg0, Statistic stat, EntityType et) {
		String message = "  - " + et.toString();
		
		int value = -1;
		OfflinePlayer maximum = null;
		for (OfflinePlayer op : parent.getServer().getOfflinePlayers()) {
			int cv = op.getStatistic(stat, et);
			if (cv > value) {
				value = cv;
				maximum = op;
			}
		}
		
		if (maximum == null || value <= 0) {
			arg0.sendMessage(ChatColor.YELLOW + message + " - [No Record Holder]");
		} else {
			arg0.sendMessage(ChatColor.GREEN + message + " = " + maximum.getName() + " ["+ value + "]");
		}
	}
	
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		OfflinePlayer stats = null;
		
		if (hasArg(arg3, "help")) {
			// TODO: write real actual help page code that generates a nicer one or something
			
			arg0.sendMessage(ChatColor.GREEN + "Stats help: ");
			arg0.sendMessage(ChatColor.GREEN + "- NOTE: ALWAYS PASS IN THE PAGE NUMBER LAST");
			arg0.sendMessage(ChatColor.YELLOW + "- /stats: Prints your statistics");
			arg0.sendMessage(ChatColor.GREEN + "   1st optional argument is a player name");
			arg0.sendMessage(ChatColor.GREEN + "   2nd optional argument is a statistic type to filter by");
			arg0.sendMessage(ChatColor.GREEN + "   - Type may be any one of UNTYPED, ITEM, BLOCK, or ENTITY");
			arg0.sendMessage(ChatColor.YELLOW + " - /stats list: Lists all stats.");
			arg0.sendMessage(ChatColor.GREEN + "   1st arg is a type, followed by optional page number.");
			arg0.sendMessage(ChatColor.GREEN + "   - Type may be any one of UNTYPED, ITEM, BLOCK, or ENTITY");
			arg0.sendMessage(ChatColor.YELLOW + " - /stats leaderboard: Lists rankings for a statistic.");
			arg0.sendMessage(ChatColor.GREEN + "   Takes one statistic as an argument.");
			arg0.sendMessage(ChatColor.GREEN + "   - Typed statistics require an entity, item, or block.");
			arg0.sendMessage(ChatColor.YELLOW + " - /statis recordholders: Lists who holds records for what statistics.");
			arg0.sendMessage(ChatColor.GREEN + "   Takes statistic as an argument.");
			arg0.sendMessage(ChatColor.GREEN + "   A statistic type is an optional 2nd arg.");
			arg0.sendMessage(ChatColor.GREEN + "   - Type may be any one of UNTYPED, ITEM, BLOCK, or ENTITY");
			return true;
		}
		
		if (hasArg(arg3, "list")) {
			double[] nums = getNums(arg3);
			String restr = null;
			if (arg3.length > 2 || (nums.length == 0 && arg3.length == 2))
				restr = arg3[1];
			
			statsList(arg0, nums, restr);
			return true;
		}
		
		if (hasArg(arg3, "recordholders")) {
			double[] nums = getNums(arg3);
			
			boolean isItem = true;
			if (arg3.length > 1 && (arg3.length > 2 || nums.length == 0)) {
				Statistic stat = EnumUtils.tryGetStatistic(arg3[1]);
				
				Material mat = null;
				EntityType ent_type = null;
				
				if (arg3.length > 2) {
					mat = EnumUtils.tryGetMaterial(arg3[2]);
					ent_type = EnumUtils.tryGetEntityType(arg3[2]);
				}
				
				String menuPrefix = "";
				int page;
				int maxPages;
				switch (stat.getType()) {
				case UNTYPED:
					arg0.sendMessage(ChatColor.RED + "[Stats] An untyped statistic may not be passed in as an argument to this command.");
					return true;
				case ENTITY:
					if (ent_type == null) {
						menuPrefix = stat.toString() + " recordholders for all EntityTypes";
						
						EntityType[] ents = EntityType.values();
						
						maxPages = (ents.length / 10) + 1;
						 
						page = 1;
						if (nums.length > 0)
							page = (int) nums[0];
						
						if (page > maxPages)
							page = maxPages;
						
						arg0.sendMessage(ChatColor.GREEN + menuPrefix + " (Page " + page + " of " + maxPages + "): ");
						
						for (int i = (page - 1) * 10; i < page * 10 && i < ents.length; i++) {
							printEntityStatLine(arg0, stat, ents[i]);
						}
					} else {
						arg0.sendMessage(ChatColor.GREEN + stat.toString() + " recordholders for EntityType " + ent_type.toString() + ": ");
						printEntityStatLine(arg0, stat, ent_type);
					}
					
					break;
				case BLOCK:
					isItem = false;
				case ITEM:
					menuPrefix = stat.toString() + " recordholders for all " + (isItem ? "Item Materials" : "Block Materials");
					
					if (mat == null) {
						ArrayList<Material> selectedItems = new ArrayList<>();
						for (Material matl : Material.values())
							if ((isItem && matl.isItem()) || (!isItem && matl.isBlock()))
								selectedItems.add(matl);
						
						maxPages = (selectedItems.size() / 10) + 1;
						 
						page = 1;
						if (nums.length > 0)
							page = (int) nums[0];
						
						if (page > maxPages)
							page = maxPages;
						
						arg0.sendMessage(ChatColor.GREEN + menuPrefix + " (Page " + page + " of " + maxPages + "): ");
		
						for (int i = (page - 1) * 10; i < page * 10 && i < selectedItems.size(); i++)
							printMaterialStatLine(arg0, stat, selectedItems.get(i));
						
					} else {
						arg0.sendMessage(ChatColor.GREEN + stat.toString() + " recordholders for " + (isItem ? "item " : "block ") + mat.toString());
						printMaterialStatLine(arg0, stat, mat);
					}
					
					break;
				}
			} else {
				Statistic[] values = Statistic.values();
				
				int maxPages = (values.length / 10) + 1;
				 
				int page = 1;
				if (nums.length > 0)
					page = (int) nums[0];
				
				if (page > maxPages)
					page = maxPages;
				
				arg0.sendMessage(ChatColor.GREEN + "Recordholders (Page " + page + " of " + maxPages + "): ");
				for (int i = (page - 1) * 10; i < page * 10 && i < values.length; i++) {
					Statistic stat = values[i];
					String message = "  - " + stat.toString();
					
					if (stat.getType() == Statistic.Type.UNTYPED) {
						int value = -1;
						OfflinePlayer maximum = null;
						for (OfflinePlayer op : parent.getServer().getOfflinePlayers()) {
							int cv = op.getStatistic(stat);
							if (cv > value) {
								value = cv;
								maximum = op;
							}
						}
						
						if (maximum == null || value <= 0) {
							arg0.sendMessage(ChatColor.YELLOW + message + " - [No Record Holder]");
						} else {
							arg0.sendMessage(ChatColor.GREEN + message + " = " + maximum.getName() + " ["+ value + "]");
						}
					} else {
						arg0.sendMessage(ChatColor.BLUE + message + " - [Typed Statistic: Call with a " + stat.getType().toString() + " as an argument.");
					}
				}
			}
			
			return true;
		}
		
		if (hasArg(arg3, "leaderboard")) {
			if (arg3.length < 2) {
				arg0.sendMessage(ChatColor.RED + "[StatsModule] This command requires at least 2 arguments: /stats leaderboard <statistic> [stat args...]");
				return true;
			} else {
				boolean byLeast = hasArg(arg3, "byLeast");
				
				double[] nums = getNums(arg3);
				
				Statistic stat;
				try {
					stat = Statistic.valueOf(arg3[1].toUpperCase());
				} catch (IllegalArgumentException iae) {
					arg0.sendMessage(ChatColor.YELLOW + "[Stats] Failed to find statistic " + arg3[1]);
					return true;
				}
				
				OfflinePlayer[] refArray = arg0.getServer().getOfflinePlayers();
				OfflinePlayer[] players = Arrays.copyOf(refArray, refArray.length);
				HashMap<OfflinePlayer, Integer> statDataMap = null;
				
				String prefixString = ChatColor.GREEN + " =+= " + stat.toString() + " Leaderboard";
				
				switch (stat.getType()) {
				case UNTYPED:
					statDataMap = precacheStats(players, stat);
					break;
				case ENTITY:
					EntityType entType;
					try {
						entType = EnumUtils.tryGetEntityType(arg3[2]);
					} catch (ArrayIndexOutOfBoundsException aioobe) {
						arg0.sendMessage(ChatColor.RED + "[Stats] Not enough arguments provided.");
						return true;
					}
					
					if (entType == null) {
						arg0.sendMessage(ChatColor.RED + "[Stats] Malformed EntityType was provided.");
						return true;
					}
					
					prefixString += " for " + entType.toString();
					
					statDataMap = precacheStatsEntity(players, stat, entType);
					break;
				case BLOCK:
				case ITEM:
					Material mat;
					try {
						mat = EnumUtils.tryGetMaterial(arg3[2]);
					} catch (ArrayIndexOutOfBoundsException aioobe) {
						arg0.sendMessage(ChatColor.RED + "[Stats] Not enough arguments provided.");
						return true;
					}
					
					if (mat == null) {
						arg0.sendMessage(ChatColor.RED + "[Stats] Malformed Material was provided.");
					}
					
					try {
						statDataMap = precacheStatsMaterial(players, stat, mat);
					} catch (IllegalArgumentException iae) {
						arg0.sendMessage(ChatColor.RED + "[Stats] Message: " + iae.getMessage());
						arg0.sendMessage(ChatColor.RED + "[Stats] Mismatch between statistic type and provided material type. Items go with item stats, and blocks go with block stats!");
						return true;
					}
					
					prefixString += " for " + mat.toString();
					
					break;
				}
				
				for (int i = 1; i < players.length; i++) { // bidirectional by flag insertion sort
					int currentElemStat = statDataMap.get(players[i]);
					OfflinePlayer currentPlayer = players[i];
					
					int j = i - 1;
					
					while (j >= 0 && (byLeast ? statDataMap.get(players[j]) > currentElemStat : statDataMap.get(players[j]) < currentElemStat)) {	
						players[j + 1] = players[j];
						j--;
					}
					
					players[j + 1] = currentPlayer;
				}
				
				int maxPages = (players.length / 10) + 1;
				 
				int page = 1;
				if (nums.length > 0)
					page = (int) nums[0];
				
				if (page > maxPages)
					page = maxPages;
				
				arg0.sendMessage(prefixString + " (Page " + page + " of " + maxPages + ") =+= ");
				for (int i = (page - 1) * 10; i < page * 10 && i < players.length; i++) {
					OfflinePlayer curPlayer = players[i];

					int statData = statDataMap.get(curPlayer);
					
					arg0.sendMessage(ChatColor.GREEN + " - " + curPlayer.getName() + " = " + statData);
				}
			}
			
			return true;
		}
		
		if (arg3.length == 0) {
			if (arg0 instanceof Player) {
				stats = parent.getServer().getOfflinePlayer(((Player) arg0).getUniqueId());
			} else return false;
		} else {
			for (OfflinePlayer op : parent.getServer().getOfflinePlayers())
				if (op.getName().equalsIgnoreCase(arg3[0]))
					stats = op;
			if (stats == null)
				stats = parent.getServer().getOfflinePlayer(((Player) arg0).getUniqueId());
		}

		
		Statistic[] possibleStats = Statistic.values();
		
		int maxPages = (possibleStats.length / 10) + 1;
		
		double[] nums = getNums(arg3);
		int page = 1;
		if (nums.length > 0)
			page = (int) nums[0];
		
		if (page > maxPages)
			page = maxPages;
		
		arg0.sendMessage(ChatColor.GREEN + "Player statistics for " + stats.getName() + " (Page " + page + " of " + maxPages + "): ");
		for (int i = (page - 1) * 10; i < page * 10 && i < possibleStats.length; i++) {
			int statData = forceGetStatistic(stats, possibleStats[i]);
			
			arg0.sendMessage(ChatColor.GREEN + " - [" + possibleStats[i].getType().toString() + "] " + possibleStats[i].toString().toLowerCase() + " = " + statData);
		}
		
		return true;
	}

	
	
}
