package org.loudonlune.smol_plugin.utils;

import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;

import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.loudonlune.smol_plugin.SmolPlugin;

public class BossBarUtils extends SmolModule {

	public Queue<Integer> unusedSpaces;
	public ArrayList<KeyedBossBar> bossBars;
	
	public BossBarUtils(SmolPlugin parent) {
		super(parent);
		bossBars = new ArrayList<>();
		unusedSpaces = new PriorityQueue<Integer>();
	}
	
	public int createBossBar(String name, String title, BarColor color, BarStyle style, BarFlag... flags) {
		KeyedBossBar bar = (flags == null) ?  
			parent.getServer().createBossBar(new NamespacedKey(parent, name), title, color, style): 
			parent.getServer().createBossBar(new NamespacedKey(parent, name), title, color, style, flags);
		
		int id;
		if (!unusedSpaces.isEmpty()) { // if we have old IDs
			id = unusedSpaces.poll(); // get one
			bossBars.set(id, bar); // put a new bar there, recycling the ID
		} else {
			id = bossBars.size(); // end of array will be a new ID
			bossBars.add(bar);
		}
		
		return id; //return the ID
	}
	
	public KeyedBossBar getBossBar(int id) {
		try {
			return bossBars.get(id);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			return null;
		}
	}
	
	public KeyedBossBar getBossBar(String name) {
		KeyedBossBar bar = null;
		
		for (KeyedBossBar b : bossBars) {
			if (b.getKey().getKey().equals(name))
				bar = b;
		}
		
		return bar;
	}
	
	public int getMaxIDValue() {
		return bossBars.size();
	}
	
	public boolean remove(String name) {
		return remove(getBossBar(name));
	}
	
	public boolean remove(KeyedBossBar boss) {
		return remove(bossBars.indexOf(boss));
	}
	
	public boolean remove(int id) {
		if (id >= 0 && id < bossBars.size()) {
			unusedSpaces.add(id);
			
			KeyedBossBar toDelete = getBossBar(id);
			parent.getServer().removeBossBar(toDelete.getKey());
			bossBars.set(id, null);
		}
		
		return false;
	}
	
	@Override
	public SmolModule register(String registeredName) {
		// do nothing, we have no registrations
		return this;
	}
	
}
