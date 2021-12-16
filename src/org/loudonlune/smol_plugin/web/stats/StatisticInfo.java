package org.loudonlune.smol_plugin.web.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Statistic;
import org.json.simple.JSONObject;

public class StatisticInfo {
	private static HashMap<Statistic, StatisticInfo> statInfoMap = null;
	
	public String name;
	public String type;
	
	public static StatisticInfo fromStatistic(Statistic s) {
		if (statInfoMap == null)
			statInfoMap = new HashMap<>();
		
		if (!statInfoMap.containsKey(s))
			statInfoMap.put(s, new StatisticInfo(s));
		
		return statInfoMap.get(s);
	}
	
	public static List<StatisticInfo> all() {
		ArrayList<StatisticInfo> statInfo = new ArrayList<>();
		
		for (Statistic s : Statistic.values())
			statInfo.add(fromStatistic(s));
		
		return statInfo;
	}
	
	private StatisticInfo(Statistic stat) {
		this.name = stat.toString();
		this.type = stat.getType().toString();
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject asJSON() {
		JSONObject opt = new JSONObject();
		
		opt.put("statistic", name);
		opt.put("type", type);
		
		return opt;
	}
}
