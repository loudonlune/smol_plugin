package org.loudonlune.smol_plugin.general;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.loudonlune.smol_plugin.SmolPlugin;
import org.loudonlune.smol_plugin.utils.SmolCommand;

public class KillAllModule extends SmolCommand {

	public KillAllModule(SmolPlugin parent) {
		super(parent);
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		if (arg3.length < 1 || arg3.length > 2) return false;
		
		EntityType et;
		try {
			et = EntityType.valueOf(arg3[0].toUpperCase());
		} catch (IllegalArgumentException iae) {
			arg0.sendMessage(ChatColor.RED + "[KillAll] Invalid EntityType " + arg3[0]);
			return true;
		}
		
		World w = null;
		if (arg0 instanceof Player) {
			w = ((Player) arg0).getWorld();
		} else {
			if (arg3.length != 2) {
				arg0.sendMessage(ChatColor.RED + "[KillAll] For non-players, a second world name argument is required.");
				return true;
			}
			
			w = parent.getServer().getWorld(arg3[1]);
		}
		
		if (w == null) {
			arg0.sendMessage(ChatColor.RED + "[KillAll] World not found.");
			return true;
		}
		
		int removed = 0;
		if (et.equals(EntityType.PLAYER)) {
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				removed++;
				p.setLastDamageCause(new EntityDamageEvent(p, DamageCause.SUICIDE, 69696969.0));
				p.setHealth(0);
			}
		} else {
			for (Entity ent : w.getEntities()) {
				if (ent.getType().equals(et)) {
					ent.remove();
					removed++;
				}
			}
		}
		
		arg0.sendMessage(ChatColor.GREEN + "[KillAll] Removed " + removed + " entities of EntityType: " + et.toString());
		return true;
	}
	
	
	
}
