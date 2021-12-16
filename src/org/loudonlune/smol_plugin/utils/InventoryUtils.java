package org.loudonlune.smol_plugin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.loudonlune.smol_plugin.SmolPlugin;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTFile;
import de.tr7zw.nbtapi.NBTItem;

public class InventoryUtils extends SmolCommand implements SmolConfigurable {
	
	public static final String CONFIG_NAME = "inventory_utils";	
	public static final String MK_BACKUPS_KEY = "make_backups";
	private ConfigurationSection config = null;
	private File backupDir = null;
	
	public InventoryUtils(SmolPlugin plugin) {
		super(plugin);
	}
	
	private File getPlayerBackupFile(World w, UUID player) {
		if (backupDir == null) {
			File playerDataDir = null; 
			for (File f : w.getWorldFolder().listFiles())
				if (f.getName().equals("playerdata"))
					playerDataDir = f;
			
			backupDir = new File(playerDataDir.getPath(), "backup");
			
			if (!backupDir.isDirectory())
				backupDir.mkdir();
		}
		
		SimpleDateFormat sdm = new SimpleDateFormat("ss-mm-hh_dd-MM-yy");
		String now = sdm.format(new Date());
		return new File(backupDir, player.toString() + "." + now + ".dat");
	}
	
	private NBTCompound generateBaseNBTItemObject(int slot, ItemStack iStack, NBTCompoundList mother) {
		NBTCompound baseObject = mother.addCompound();
		
		baseObject.setByte("Slot", (byte) slot);
		baseObject.setByte("Count", (byte) iStack.getAmount());
		baseObject.setString("id", iStack.getType().getKey().toString());
		NBTCompound newItemMeta = baseObject.addCompound("tag");
		
		newItemMeta.mergeCompound(new NBTItem(iStack));
		
		return baseObject;
	}
	
	public void closeOfflinePlayerInventory(Inventory inv, CommandSender caller, OfflinePlayer offlineInvOwner, boolean isEnder) {
		World actingWorld = caller instanceof Player ? ((Player) caller).getWorld() : parent.getServer().getWorld("world");
		
		File playerDataDir = null;
		for (File f : actingWorld.getWorldFolder().listFiles())
			if (f.getName().equals("playerdata"))
				playerDataDir = f;
		
		File playerDatFile = null;
		if (playerDataDir != null) {
			for (File f : playerDataDir.listFiles()) {
				String[] namearr = f.getName().split(Pattern.quote("."));
				if (namearr.length < 2) continue;
				if (namearr[1].contains("old")) continue;
				if (namearr[0].equals(offlineInvOwner.getUniqueId().toString()))
					playerDatFile = f;
			}
		} else {
			caller.sendMessage(ChatColor.RED + "[InventoryUtils] Failed to find playerdata directory for world: " + actingWorld.getName());
			return;
		}
		
		if (config.getBoolean(MK_BACKUPS_KEY)) {
			try {
				File backupFile = getPlayerBackupFile(actingWorld, offlineInvOwner.getUniqueId());
				
				caller.sendMessage(ChatColor.YELLOW + "[InventoryUtils] Backing up playerdata for " + offlineInvOwner.getName() + "...");
				FileInputStream inStream = new FileInputStream(playerDatFile);
				FileOutputStream outStream = new FileOutputStream(backupFile);
						
				inStream.getChannel().transferTo(0, playerDatFile.length(), outStream.getChannel());
				inStream.close();
				outStream.close();
				
				caller.sendMessage(ChatColor.YELLOW + "[InventoryUtils] Backup complete.");
			} catch (IOException e) {
				caller.sendMessage(ChatColor.DARK_RED + "[InventoryUtils] An error occurred while making a backup: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		try {
			NBTFile outFile = new NBTFile(playerDatFile);
			String invName = "Inventory";
			int invDirectAccessibleSize = 36;
			
			if (isEnder) {
				invName = "EnderItems";
				invDirectAccessibleSize -= 9;
			}

			NBTCompoundList inventoryItems = outFile.getCompoundList(invName);
			if (inventoryItems == null) {
				caller.sendMessage(ChatColor.DARK_RED + "[InventoryUtils] NBTCompound array for " + invName + " data was not found.");
				return;
			}
			
			inventoryItems.clear();
			
			/*
			 * Copies 0 ... 26 or 35
			 * 
			 */
			for (int i = 0; i < invDirectAccessibleSize; i++) {
				ItemStack item = inv.getItem(i);
				
				if (item != null) 
					generateBaseNBTItemObject(i, item, inventoryItems);
			}
			
			if (invName.equals("Inventory")) {
				// right-hand inventory slot (slot -106)
				ItemStack item = inv.getItem(40);
				if (item != null) 
					generateBaseNBTItemObject(-106, item, inventoryItems);
					
				// armor (slots 100 ... 103))
				final int armorBegin = 36, armorEnd = 40;
				for (int i = armorBegin; i < armorEnd; i++) {
					item = inv.getItem(i);
					
					if (item != null) 
						generateBaseNBTItemObject(i, item, inventoryItems);					
				}
			}
			
			playerDatFile.delete();
			FileOutputStream writeStream = new FileOutputStream(playerDatFile);
			
			outFile.writeCompound(writeStream);
			writeStream.close();
			
			caller.sendMessage(ChatColor.YELLOW + "[InventoryUtils] Inventory of " + offlineInvOwner.getName() + " has been written to disk.");
		} catch (IOException e) {
			caller.sendMessage(ChatColor.DARK_RED + "[InventoryUtils] An error occurred while writing player inventory changes: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private ItemStack parseItem(CommandSender caller, NBTCompound item) {
		String id = item.getString("id");
		if (id == null) {
			caller.sendMessage(ChatColor.DARK_RED + "[InventoryUtils] Invalid item with null id found");
			return null;
		}
		
		String[] id_comps = id.split(":");
		if (id_comps.length < 2) {
			caller.sendMessage(ChatColor.DARK_RED + "[InventoryUtils] Invalid item with malformed id found");
			return null;
		}
		
		String matName = id_comps[1].toUpperCase();
		
		Material mat = Material.matchMaterial(id);
		if (mat == null)
			mat = Material.getMaterial(matName);
		
		if (mat == null) {
			caller.sendMessage(ChatColor.DARK_RED + "[InventoryUtils] Item with invalid material \"" + id + "\" found");
			return null;
		}
		
		ItemStack iStack = new ItemStack(mat, item.getByte("Count"));
		
		if (item.hasKey("tag")) {
			NBTCompound metaTag = item.getCompound("tag");
			NBTItem container = new NBTItem(iStack);
			
			container.mergeCompound(metaTag);
			container.applyNBT(iStack);
		}
		
		return iStack;
	}
	
	public boolean readOfflinePlayerInventory(Inventory inv, CommandSender caller, OfflinePlayer op, boolean ender) {
		World actingWorld = caller instanceof Player ? ((Player) caller).getWorld() : parent.getServer().getWorld("world");
		
		File playerDataDir = null;
		for (File f : actingWorld.getWorldFolder().listFiles())
			if (f.getName().equals("playerdata"))
				playerDataDir = f;
		
		File playerDatFile = null;
		if (playerDataDir != null) {
			for (File f : playerDataDir.listFiles()) {
				String[] namearr = f.getName().split(Pattern.quote("."));
				if (namearr.length < 2) continue;
				if (namearr[1].contains("old")) continue;
				if (namearr[0].equals(op.getUniqueId().toString()))
					playerDatFile = f;
			}
		} else {
			caller.sendMessage(ChatColor.RED + "[InventoryUtils] Failed to find playerdata directory.");
			return false;
		}
		
		
		if (playerDatFile != null) {
			try {
				NBTFile reader = new NBTFile(playerDatFile);
				String loadInvName = ender ? "EnderItems" : "Inventory";
				NBTCompoundList invRoot = reader.getCompoundList(loadInvName);
				int row4 = 41;
				for (NBTCompound item : invRoot) {
					ItemStack iStack = parseItem(caller, item);
					
					if (iStack == null)
						continue;
					
					try {
						int slot = 0;
						if (item.hasKey("Slot"))
							slot = item.getInteger("Slot");
						else {
							slot = row4++;
						}
						
						if (slot < 0) {
							slot = 40;
						} else if (slot > inv.getSize()) {
							slot = (slot - 100) + 36;
						}
						
						inv.setItem(slot, iStack);
					} catch (Exception e) {
						caller.sendMessage(ChatColor.DARK_RED + "Failed to set item: " + e.getMessage());
					}
				}
				
			} catch (IOException e) {
				caller.sendMessage(ChatColor.RED + "An error occurred attempting to open " + op.getName() + "'s inventory.");
				caller.sendMessage(ChatColor.RED + "Message: " + e.getMessage());
			}
		} else {
			caller.sendMessage(ChatColor.RED + "Failed to find player inventory file.");
			return false;
		}
		
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {
		if (hasArg(args, "set")) {
			if (hasArg(args, MK_BACKUPS_KEY)) {
				if (args.length > 2) {
					config.set(MK_BACKUPS_KEY, Boolean.parseBoolean(args[2]));
				} else {
					sender.sendMessage(ChatColor.RED + "[InventoryUtils] You must pass in \"true\" or \"false\" as a third argument.");
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.YELLOW + "[InventoryUtils] Unknown set parameter entered.");
				sender.sendMessage(ChatColor.YELLOW + "set - Sets a configuration value for InventoryUtils");
				for (String key : config.getKeys(false))
					sender.sendMessage(ChatColor.YELLOW + "  - invutils set " + key);
				return true;
			}
			
			sender.sendMessage(ChatColor.YELLOW + "[InventoryUtils] Parameter set successfully.");
			return true;
		}
		
		if (hasArg(args, "get")) {
			for (String key : config.getKeys(false))
				if (hasArg(args, key)) {
					sender.sendMessage(ChatColor.YELLOW + key + ": " + config.get(key));
					return true;
				}

			sender.sendMessage(ChatColor.YELLOW + "[InventoryUtils] Unknown get parameter entered.");
			sender.sendMessage(ChatColor.YELLOW + "get - Gets a configuration value from InventoryUtils");
			
			for (String key : config.getKeys(false))
				sender.sendMessage(ChatColor.YELLOW + "  - invutils get " + key);
			
			return true;
		}
		
		return false;
	}

	@Override
	public void writeOnto(ConfigurationSection root) {}

	@Override
	public void readFrom(ConfigurationSection root) {
		if (root.isConfigurationSection(CONFIG_NAME)) {
			config = root.getConfigurationSection(CONFIG_NAME);
		}
	}
}
