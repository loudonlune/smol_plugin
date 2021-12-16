package org.loudonlune.smol_plugin.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.DefaultSessionIdManager;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.json.simple.JSONObject;
import org.loudonlune.smol_plugin.SmolPlugin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.lingala.zip4j.ZipFile;

public class WebService {
	public static final int WEB_PORT = 6900;
	public static final int WEB_MAX_CONNECTIONS = 8;
	public static final int WEB_TIMEOUT = 4000;
	
	private HandlerList handlerList;
	private Server webServer;
	private File webRoot;
	private SmolPlugin parent;
	private APIHandler api;
	
	private void startWebServer() {
		LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(WEB_MAX_CONNECTIONS);
		ExecutorThreadPool etp = new ExecutorThreadPool(WEB_MAX_CONNECTIONS, 2, queue);
		
		webServer = new Server(etp);
		webServer.setSessionIdManager(new DefaultSessionIdManager(webServer));
		
		NetworkTrafficServerConnector netTrafficConnector = new NetworkTrafficServerConnector(webServer);
		netTrafficConnector.setIdleTimeout(WEB_TIMEOUT);
		netTrafficConnector.setAcceptQueueSize(25);
		netTrafficConnector.setPort(WEB_PORT);
		
		webServer.setConnectors(new Connector[] { 
				netTrafficConnector
		});
		
		webServer.setStopAtShutdown(true);
	}
	
	private void initStaticWebroot() {
		webRoot = new File(parent.getDataFolder(), "webroot");
		if (!webRoot.exists()) {
			webRoot.mkdir();
			if (parent.getResource("staticfiles") != null) {
				parent.saveResource("staticfiles", false);
				
				File staticfiles = new File(parent.getDataFolder(), "staticfiles");
				File staticfilesDest = new File(webRoot, "staticfiles");
				staticfiles.renameTo(staticfilesDest);
				
				try (ZipFile archive = new ZipFile(staticfilesDest)) {
					archive.extractAll(webRoot.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				staticfilesDest.delete();
			} else {
				Bukkit.getLogger().log(Level.WARNING, "Failed to copy out staticfile, not found in jar.");
				return;
			}
		}
	}
	
	private void initHandlers() {
		handlerList = new HandlerList();
		
		api = new APIHandler();
		
		api.getRoot().addMember(new APIEndpoint("version") {

			@Override
			public int call(HttpServletRequest req, HttpServletResponse resp) {
				try {
					PrintWriter pw = resp.getWriter();
					pw.write(Message.serializeString(Bukkit.getServer().getVersion()));
					pw.flush();
				} catch (IOException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
					return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				}
				
				return HttpServletResponse.SC_OK;
			}
			
		});
		
		// test end point (for fun)
		api.getRoot().addMember(new APIEndpoint("broadcast") {

			@Override
			public int call(HttpServletRequest req, HttpServletResponse resp) {
				String param = req.getParameter("msg");
				if (param == null)
					return HttpServletResponse.SC_BAD_REQUEST;
				
				Bukkit.getServer()
				.broadcast(
						Component.text("[smol_plugin broadcast] ")
						.color(
								TextColor.fromCSSHexString("#690096")
								)
						.append(Component.text(param))
						);
				
				return HttpServletResponse.SC_OK;
			}
			
		});
		
		handlerList.addHandler(api);
		
		ResourceHandler staticHandler = new ResourceHandler();
		
		staticHandler.setBaseResource(Resource.newResource(webRoot.getAbsoluteFile()));
		staticHandler.setDirectoriesListed(true);
		staticHandler.setAcceptRanges(true);
		
		handlerList.addHandler(staticHandler);
		handlerList.addHandler(new DefaultHandler());
		
		webServer.setHandler(handlerList);
	}
	
	public WebService(SmolPlugin parent) {
		this.parent = parent;
		initStaticWebroot();
		startWebServer();
		initHandlers();
	}
	
	public APIHandler getAPI() {
		return api;
	}
	
	public Server getWebServer() {
		return webServer;
	}
}
