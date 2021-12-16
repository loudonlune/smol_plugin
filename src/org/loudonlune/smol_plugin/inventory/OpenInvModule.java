package org.loudonlune.smol_plugin.inventory;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.loudonlune.smol_plugin.SmolPlugin;
import org.loudonlune.smol_plugin.utils.SmolCmdListener;

import net.kyori.adventure.text.Component;

public class OpenInvModule extends SmolCmdListener {
	private class OpenInventoryRecord {
		private Player caller;
		private OfflinePlayer other;
		private boolean offlineWriteEnabled;
		private boolean ender;
		
		public OpenInventoryRecord(Player caller, OfflinePlayer op, boolean ender, boolean offlineWrite) {
			other = op;
			this.caller = caller;
			this.ender = ender;
			this.offlineWriteEnabled = offlineWrite;
		}
		
		public boolean doOfflineWrite() {
			return offlineWriteEnabled;
		}
		
		public boolean isEnder() {
			return ender;
		}
		
		public Player getCaller() {
			return caller;
		}
		
		public OfflinePlayer getOther() {
			return other;
		}
	}
	
	private ArrayList<OpenInventoryRecord> activeInvPlayers;
	
	public OpenInvModule(SmolPlugin parent) {
		super(parent);
		activeInvPlayers = new ArrayList<>();
	}
	
	@EventHandler
	public void onInventoryCloseEvent(InventoryCloseEvent ice) {
		OpenInventoryRecord processed = null;
		for (OpenInventoryRecord oir : activeInvPlayers)
			if (oir.getCaller().getUniqueId().equals(ice.getPlayer().getUniqueId())) {
				if (!oir.doOfflineWrite()) {
					oir.getCaller().sendMessage("[OpenInvModule] OfflinePlayer inventory write was skipped.");
				} else {
					try {
						parent.getInventoryUtils().closeOfflinePlayerInventory(ice.getInventory(), oir.getCaller(), oir.getOther(), oir.isEnder());
					} catch (StackOverflowError soe) {
						parent.getLogger().severe("[smol_plugin] StackOverflowError thrown by NBTAPI...");
						soe.printStackTrace();
						ice.getPlayer().sendMessage(ChatColor.DARK_RED + "FATAL: StackOverflowError encountered due to reflection: " + soe.getMessage());
					}
				}
				
				processed = oir;
				break;
			}
			
		if (processed != null)
			activeInvPlayers.remove(processed);
	}
	
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		if (arg3.length < 1)
			return false;
		
		boolean ender = hasArg(arg3, "ender");
		boolean doListOperation = hasArg(arg3, "list");
		boolean doOfflineWrite = !hasArg(arg3, "skipOfflineWrite");
		boolean online = hasArg(arg3, "online");
		double[] nums = getNums(arg3);
		
		if (doListOperation) {
			int page = 1;
			if (nums.length > 0)
				page = (int) nums[0];
			
			boolean detail = hasArg(arg3, "detail");
			int detailLevel = 0;
			if (nums.length > 1)
				detailLevel = (int) nums[1];
			
			parent.getGeneralUtils().printPlayerList(arg0, page, detail, detailLevel, !online);
			return true;
		} else if (arg0 instanceof Player) {
			OfflinePlayer[] players = parent.getServer().getOfflinePlayers();
			Player otherPlayer = null;
			OfflinePlayer otherPlayerOP = null;
			
			for (OfflinePlayer p : players)
				if (p.getName().equals(arg3[0])) {
					otherPlayerOP = p;
				}
			
			Player ourPlayer = (Player) arg0;
			if (otherPlayerOP == null)
				arg0.sendMessage(ChatColor.YELLOW + "Player " + arg3[0] + " was not found.");
			else {
				otherPlayer = otherPlayerOP.getPlayer();
				
				Inventory inv = null;
				
				if (otherPlayer == null) {
					inv = Bukkit.createInventory(ourPlayer, 
							ender ? 27 : 45, 
									Component.text(otherPlayerOP.getName() + "'s Inventory"));
					
					if (!parent.getInventoryUtils().readOfflinePlayerInventory(inv, ourPlayer, otherPlayerOP, ender)) {
						ourPlayer.sendMessage(ChatColor.RED + "[OpenInvModule] Failed to load player inventory.");
						return true;
					}
					
					activeInvPlayers.add(new OpenInventoryRecord(ourPlayer, otherPlayerOP, ender, doOfflineWrite));
				} else {
					if (ender) {
						inv = otherPlayer.getEnderChest();
					} else inv = otherPlayer.getInventory();
				}
				
				ourPlayer.openInventory(inv);
			}
			
			return true;
		} else arg0.sendMessage(ChatColor.DARK_RED + "[OpenInvModule] You must be a player to execute this command.");
		
		return true;
	}
	
}
