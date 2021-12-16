package org.loudonlune.smol_plugin.web;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class APIDirectory implements APIMember {

	private String dir;
	private List<APIMember> members;
	
	public APIDirectory(String dir) {
		this.dir = dir;
		members = new ArrayList<APIMember>();
	}
	
	public void addMember(@NotNull APIMember member) {
		if (member != null)
			members.add(member);
	}

	@Override
	public boolean call(String point, HttpServletRequest req, HttpServletResponse resp) {
		if (point.startsWith("/" + dir)) {
			String subPoint = point.substring(dir.length() + 1);
			
			for (APIMember mem : members) {
				if (mem.call(subPoint, req, resp))
					return true;
			}
		}
		
		return false;
	}
	
}
