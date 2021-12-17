package org.loudonlune.smol_plugin.utils;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
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
			pie.getPlayer().getWorld().getBlockAt(pie.getRightClicked().getLocation())
			.setType(Material.DIRT_PATH);
			
			if (pie.getRightClicked().getType() != EntityType.PLAYER) 
				pie.getRightClicked().remove();
			else {
				Player p = (Player) pie.getRightClicked();
				
				if (p.getHealth() <= 6.0) {
					p.setKiller(pie.getPlayer());
					p.setHealth(0);
				} else return;
			}
			
			Damageable shovel = ((Damageable) handItem.getItemMeta());
			shovel.setDamage(shovel.getDamage() - 1);	
			
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
				Damageable shovel = ((Damageable) pie.getItem().getItemMeta());
				shovel.setDamage(shovel.getDamage() - 1);	
			}
			
			break;
		default:
		}
	}
	
}
