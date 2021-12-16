package org.loudonlune.smol_plugin.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface APIMember {
	public boolean call(String point, HttpServletRequest req, HttpServletResponse resp);
}
