package org.loudonlune.smol_plugin.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class APIEndpoint implements APIMember {

	private String endpoint;
	
	public APIEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	public abstract int call(HttpServletRequest req, HttpServletResponse resp);
	
	@Override
	public boolean call(String point, HttpServletRequest req, HttpServletResponse resp) {
		if (point.equals("/" + endpoint)) {
			resp.setStatus(this.call(req, resp));
			return true;
		}
		
		return false;
	}
	
}
