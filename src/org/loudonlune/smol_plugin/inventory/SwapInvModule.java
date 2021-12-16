package org.loudonlune.smol_plugin.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.loudonlune.smol_plugin.SmolPlugin;
import org.loudonlune.smol_plugin.utils.SmolCommand;

public class SwapInvModule extends SmolCommand {
	class SwapRecord {
		private UUID playerOne;
		private UUID playerTwo;
		
		SwapRecord(UUID playerOne, UUID playerTwo) {
			this.playerOne = playerOne;
			this.playerTwo = playerTwo;
		}
		
		public Player getPlayerOne() {
			return parent.getServer().getPlayer(playerOne);
		}
		
		public Player getPlayerTwo() {
			return parent.getServer().getPlayer(playerTwo);
		}
		
		public UUID getOneId() {
			return playerOne;
		}
		
		public UUID getTwoId() {
			return playerTwo;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof SwapRecord) 
				return playerOne.equals(((SwapRecord) other).playerOne) 
						&& playerTwo.equals(((SwapRecord) other).playerTwo);
			
			return false;
		}
		
		@Override
		public String toString() {
			return "[ " + parent.getServer().getOfflinePlayer(playerOne).getName() 
					+ " <-> " 
					+ parent.getServer().getOfflinePlayer(playerTwo).getName()
					+ " ]";
		}
	}
	
	private ArrayList<SwapRecord> lastSwaps;
	
	public SwapInvModule(SmolPlugin parent) {
		super(parent);
		lastSwaps = new ArrayList<>();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmdId, String cmdStr, String[] args) {
		if (args.length > 0) {
			if (hasArg(args, "list")) {
				double[] nums = getNums(args);
				
				boolean doDetail = hasArg(args, "detail");
				int detail = 0;
				int page = 1;
				if (nums.length > 0) {
					page = (int) nums[0];
					if (nums.length > 1)
						detail = (int) nums[1];
				}
				
				boolean onlineOnly = hasArg(args, "onlineOnly");
				
				parent.getGeneralUtils().printPlayerList(sender, page, doDetail, detail, !onlineOnly);
				return true;
			}
			
			if (hasArg(args, "history")) {
				double[] nums = getNums(args);
				
				int page = 1;
				if (nums.length > 0)
					page = (int) nums[0];
				
				int cntPerPage = 10;
				
				int maxPages = (lastSwaps.size() / cntPerPage) + 1;
				if (page > maxPages)
					page = maxPages;
				
				sender.sendMessage(ChatColor.GREEN + "[SwapInv] Swap history (Page " + page + " of " + maxPages + "): ");				
				for (int i = (page - 1) * 10; i < page * 10 && i < lastSwaps.size(); i++) {
					String line = "  - " + lastSwaps.get(i).toString();
					sender.sendMessage(ChatColor.GREEN + line);
				}
				
				return true;
			}
			
			OfflinePlayer playerOne = null;
			if (args.length > 1) {
				for (OfflinePlayer c : parent.getServer().getOfflinePlayers())
					if (c.getName().equals(args[1]))
						playerOne = c;
				
				if (playerOne == null) {
					sender.sendMessage(ChatColor.RED + "[SwapInv] Error: Could not find second player: " + args[2]);
					return true;
				}
			} else {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "[SwapInv] You must be a Player to execute this command.");
					return true;
				}
				
				playerOne = parent.getServer().getOfflinePlayer(((Player) sender).getUniqueId());
			}
			
			OfflinePlayer playerTwo = null;
			for (OfflinePlayer c : parent.getServer().getOfflinePlayers())
				if (c.getName().equals(args[0]))
					playerTwo = c;
			
			if (playerTwo == null) {
				sender.sendMessage(ChatColor.RED + "[SwapInv] Player " + args[0] + " not found.");
				return true;
			}
			
			Inventory invOne = null;
			Inventory chestOne = null;
			
			if (!playerOne.isOnline()) {
				invOne = Bukkit.createInventory(null, InventoryType.PLAYER);
				chestOne = Bukkit.createInventory(null, InventoryType.ENDER_CHEST);
				
				parent.getInventoryUtils().readOfflinePlayerInventory(invOne, sender, playerOne, false);
				parent.getInventoryUtils().readOfflinePlayerInventory(chestOne, sender, playerOne, true);
			} else {
				Player playerOneInstance = playerOne.getPlayer();
				
				invOne = playerOneInstance.getInventory();
				chestOne = playerOneInstance.getEnderChest();
			}
			
			ItemStack[] chestOneItems = chestOne.getContents().clone();
			ItemStack[] invOneItems = invOne.getContents().clone();
			
			invOne.clear();
			chestOne.clear();
			
			Inventory invTwo = null;
			Inventory chestTwo = null;
			
			if (!playerTwo.isOnline()) {
				invTwo = Bukkit.createInventory(null, 45);
				chestTwo = Bukkit.createInventory(null, 27);
				
				parent.getInventoryUtils().readOfflinePlayerInventory(invTwo, sender, playerTwo, false);
				parent.getInventoryUtils().readOfflinePlayerInventory(chestTwo, sender, playerTwo, true);
			} else {
				Player otherPlayer = playerTwo.getPlayer();
				invTwo = otherPlayer.getInventory();
				chestTwo = otherPlayer.getEnderChest();
			}
			
			if (invOne.getSize() == invTwo.getSize()) {
				invOne.setContents(invTwo.getContents());
				invTwo.clear();
				invTwo.setContents(invOneItems);
			} else {
				if (invOne.getSize() < invTwo.getSize()) {
					invOne.setContents(Arrays.copyOf(invTwo.getContents(), 41));
					invTwo.clear();
					invTwo.setContents(invOneItems);
				} else {
					invOne.setContents(invTwo.getContents());
					invTwo.clear();
					invTwo.setContents(Arrays.copyOf(invOneItems, 41));
				}
			}
			
			chestOne.setContents(chestTwo.getContents());
			chestTwo.clear();
			chestTwo.setContents(chestOneItems);
			
			if (!playerOne.isOnline()) {
				parent.getInventoryUtils().closeOfflinePlayerInventory(invOne, sender, playerOne, false);
				parent.getInventoryUtils().closeOfflinePlayerInventory(chestOne, sender, playerOne, true);
			}
			
			if (!playerTwo.isOnline()) {
				parent.getInventoryUtils().closeOfflinePlayerInventory(invTwo, sender, playerTwo, false);
				parent.getInventoryUtils().closeOfflinePlayerInventory(chestTwo, sender, playerTwo, true);
			}
			
			lastSwaps.add(new SwapRecord(playerOne.getUniqueId(), playerTwo.getUniqueId()));
			
			sender.sendMessage(ChatColor.GREEN + "[SwapInv] Your inventory has been swapped with " + playerTwo.getName());
			return true;
		}
		
		return false;
	}

	
	
}
