package org.loudonlune.smol_plugin.general;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.loudonlune.smol_plugin.SmolPlugin;
import org.loudonlune.smol_plugin.utils.SmolEventListener;
import org.loudonlune.smol_plugin.utils.SmolModule;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;

public class SleepVotingModule extends SmolEventListener {
	
	private ArrayList<Player> players;
	
	public SleepVotingModule(SmolPlugin parent) {
		super(parent);
		players = new ArrayList<Player>();
	}
	
	private void eval(Player interacting_bed, boolean leaving) {
		World w = parent.getServer().getWorld("world");
		
		if (w == null && parent.getServer().getWorlds().size() > 0)
			w = parent.getServer().getWorlds().get(0);
				
		if (w.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE) && (w.hasStorm() ? (w.getTime() > 12010 && w.getTime() < 23992) : (w.getTime() > 12542 && w.getTime() < 23460) )) {
			int count = players.size(), sleeping = 0;
			int offworld = 0;
			
			if (interacting_bed != null && !leaving) {
				sleeping++;
			}
			
			for (Player p : players) {
				if (p.getWorld() != w) {
					count--; // do not include off-world players
					offworld++;
				}
				
				if (p != interacting_bed && p.isSleeping())
					sleeping++;
			}
			
			if (sleeping > 0) {
				int half = count - (count / 2); // ceiling division
				
				if (sleeping >= half) {
					w.setTime(0);
					
					Bukkit.getServer().broadcast(
							Component.text(
									"[SleepVoting] It is now day."
							)
							.color(TextColor.color(ChatColor.YELLOW.getColor().getRGB()))
						);
					return;
				}
				
				int needed = half - sleeping;
				
				Bukkit.getServer().broadcast(
						Component.text(
								"[SleepVoting] " 
								+ Integer.toString(sleeping) 
								+ " of " + count + (sleeping > 1 ? " are" : " is") 
								+ " sleeping. " 
								+ (offworld > 0 ? "(" + offworld + " Offworld) " : "") 
								+ needed + " more players need to sleep to skip to day."
						)
						.color(TextColor.color(ChatColor.YELLOW.getColor().getRGB()))
					);
			}
		}
	}
	
	@Override
	public SmolModule register(String registeredName) {
		super.register(registeredName);
		
		// reload tolerance
		players.addAll(parent.getServer().getOnlinePlayers());
		Bukkit.getLogger().info("[smol_plugin] SleepVotingModule initialized with " + players.size() + " players.");
		
		return this;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent pje) {
		players.add(pje.getPlayer());
		eval(null, false);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent pqe) {
		if (!players.remove(pqe.getPlayer())) {
			
			Bukkit.getServer().broadcast(
					Component.text(
							"An error occurred when " + pqe.getPlayer().getName().toString() + " left the server."
					)
					.color(TextColor.color(ChatColor.RED.getColor().getRGB()))
				);
			
			Bukkit.getLogger().warning("[smol_plugin] Found unknown player leaving. This is strange...");
		}
		
		eval(null, false);
	}
	
	@EventHandler
	public void onPlayerBedEnter(PlayerBedEnterEvent pbee) {
		eval(pbee.getPlayer(), false);
	}
	
	@EventHandler
	public void onPlayerBedExit(PlayerBedLeaveEvent pble) {
		eval(pble.getPlayer(), true);
	}
}
