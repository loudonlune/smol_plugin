package org.loudonlune.smol_plugin.utils;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

public class EnumUtils {
	public static EntityType tryGetEntityType(String input) {
		try {
			return EntityType.valueOf(input.toUpperCase());
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}
	
	public static Material tryGetMaterial(String input) {
		try {
			return Material.valueOf(input.toUpperCase());
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}
	
	public static Statistic tryGetStatistic(String input) {
		try {
			return Statistic.valueOf(input.toUpperCase());
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}
}
