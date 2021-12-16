package org.loudonlune.smol_plugin.web;

import java.io.IOException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class APIHandler extends AbstractHandler {

	private APIDirectory root;
	
	public APIHandler() {
		root = new APIDirectory("api");
	}
	
	public APIDirectory getRoot() {
		return root;
	}
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		
		String basePath = baseRequest.getHttpURI().getDecodedPath();
		if (root.call(basePath, request, response)) {
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.flushBuffer();
			baseRequest.setHandled(true);
		}
	}

}
