package org.loudonlune.smol_plugin.web.stats;

import org.json.simple.JSONObject;

public class StatisticResult {
	public String playerName;
	public long data;
	
	public StatisticResult(String player, long data) {
		this.playerName = player;
		this.data = data;
	}
	
	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public long getData() {
		return data;
	}

	public void setData(long data) {
		this.data = data;
	}

	@SuppressWarnings("unchecked")
	public JSONObject asJSON() {
		JSONObject opt = new JSONObject();
		
		opt.put("player", playerName);
		opt.put("value", data);
		
		return opt;
	}
}
