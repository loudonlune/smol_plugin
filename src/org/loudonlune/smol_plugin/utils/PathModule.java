package org.loudonlune.smol_plugin.utils;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.loudonlune.smol_plugin.SmolPlugin;

public class PathModule extends SmolEventListener {

	private boolean disabled;
	private HashMap<UUID, Integer> songPositions;
	
	public static final float[] notes = {
		(float) Math.pow(2.0, -1.5), // C2
		(float) Math.pow(2.0, -0.5),  // C3
		(float) Math.pow(2.0, -1.0 + (5.0/6.0)), // E3
		(float) Math.pow(2.0, 0.0), // F3
		(float) Math.pow(2.0, 0.0 + (1.0/12.0)), // F#3
		(float) Math.pow(2.0, 0.0 - (1.0/12.0)),  // F-flat3
		(float) Math.pow(2.0, -1.0 + (5.0/6.0)), // E3
		(float) Math.pow(2.0, -0.5),  // C3,
		(float) Math.pow(2.0, -0.5 - (1.0/6.0)),  // B2
		(float) Math.pow(2.0, -0.5 + (1.0/6.0)),  // D3
		(float) Math.pow(2.0, -0.5),  // C3
	};
	
	public void playSound(Player p, Location loc) {
		UUID pid = p.getUniqueId();
		if (!songPositions.containsKey(pid)) {
			songPositions.put(pid, Integer.valueOf(0));
		}
		
		int index = songPositions.get(pid);
		p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 10.0f, notes[index]);
		
		songPositions.put(pid, Integer.valueOf((index + 1) % notes.length));
	}
	
	public PathModule(SmolPlugin parent) {
		super(parent);
		songPositions = new HashMap<>();
		disabled = true;
	}
	
	@EventHandler
	public void onRightClickDoThings(PlayerInteractEntityEvent pie) {
		if (disabled)
			return;
		
		ItemStack handItem = pie.getPlayer().getInventory().getItemInMainHand();
		Material type = handItem.getType();
		
		switch (type) {
		case WOODEN_SHOVEL:
		case STONE_SHOVEL:
		case IRON_SHOVEL:
		case DIAMOND_SHOVEL:
		case GOLDEN_SHOVEL:
		case NETHERITE_SHOVEL:
			Location loc = pie.getRightClicked().getLocation();
			
			if (pie.getRightClicked() instanceof LivingEntity) {
				LivingEntity ent = (LivingEntity) pie.getRightClicked();
				
				if (ent.getType() != EntityType.PLAYER) {
					ent.setKiller(pie.getPlayer());
					ent.setHealth(0);
					pie.getPlayer().getWorld().getBlockAt(
							pie.getRightClicked().getLocation()
					).setType(Material.DIRT_PATH);
				} else {
					Player p = (Player) pie.getRightClicked();
					
					if (pie.getPlayer().isOp() || p.getHealth() <= 6.0) {
						p.setKiller(pie.getPlayer());
						p.setHealth(0);
						pie.getPlayer().getWorld().getBlockAt(
								pie.getRightClicked().getLocation()
						).setType(Material.DIRT_PATH);
					} else return;
				}
				
				playSound(pie.getPlayer(), loc);
				
				Damageable shovel = ((Damageable) handItem.getItemMeta());
				shovel.setDamage(shovel.getDamage() - 1);	
			}
			
			break;
		default:
		}
	}
	
	
	@EventHandler
	public void onRightClickWithShovelMakePath(PlayerInteractEvent pie) {
		if (disabled)
			return;
		
		if (pie.getItem() == null) return;
		Material type = pie.getItem().getType();
		
		switch (type) {
		case WOODEN_SHOVEL:
		case STONE_SHOVEL:
		case IRON_SHOVEL:
		case DIAMOND_SHOVEL:
		case GOLDEN_SHOVEL:
		case NETHERITE_SHOVEL:
			if (pie.getAction().isRightClick() && pie.getClickedBlock().getType() != Material.DIRT_PATH) {
				pie.getClickedBlock().setType(Material.DIRT_PATH);
				
				playSound(pie.getPlayer(), pie.getClickedBlock().getLocation());
				
				Damageable shovel = ((Damageable) pie.getItem().getItemMeta());
				shovel.setDamage(shovel.getDamage() - 1);	
			}
			
			break;
		default:
		}
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
}
