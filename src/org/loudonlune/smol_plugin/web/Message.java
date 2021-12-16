package org.loudonlune.smol_plugin.web;

import org.json.simple.JSONObject;

public class Message {
	private String message;
	
	public static String serializeString(String message) {
		return new Message(message).asJSON().toJSONString();
	}
	
	public Message(String message) {
		this.setMessage(message);
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject asJSON() {
		JSONObject jobj = new JSONObject();
		jobj.put("message", message);
		
		return jobj;
	}
}
