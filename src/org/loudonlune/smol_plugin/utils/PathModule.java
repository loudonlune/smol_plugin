package org.loudonlune.smol_plugin.utils;

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

	public PathModule(SmolPlugin parent) {
		super(parent);
	}
	
	@EventHandler
	public void onRightClickDoThings(PlayerInteractEntityEvent pie) {
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
				
				pie.getPlayer().playSound(loc, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 10.0f, 0.5f);
				
				Damageable shovel = ((Damageable) handItem.getItemMeta());
				shovel.setDamage(shovel.getDamage() - 1);	
			}
			
			break;
		default:
		}
	}
	
	
	@EventHandler
	public void onRightClickWithShovelMakePath(PlayerInteractEvent pie) {
		Material type = pie.getItem().getType();
		
		switch (type) {
		case WOODEN_SHOVEL:
		case STONE_SHOVEL:
		case IRON_SHOVEL:
		case DIAMOND_SHOVEL:
		case GOLDEN_SHOVEL:
		case NETHERITE_SHOVEL:
			if (pie.getAction().isRightClick()) {
				pie.getClickedBlock().setType(Material.DIRT_PATH);
				
				pie.getPlayer().playSound(pie.getClickedBlock().getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 10.0f, 0.5f);
				
				Damageable shovel = ((Damageable) pie.getItem().getItemMeta());
				shovel.setDamage(shovel.getDamage() - 1);	
			}
			
			break;
		default:
		}
	}
	
}
