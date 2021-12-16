package org.loudonlune.smol_plugin.web.stats;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.loudonlune.smol_plugin.utils.EnumUtils;
import org.loudonlune.smol_plugin.utils.StatsUtils;
import org.loudonlune.smol_plugin.web.APIDirectory;
import org.loudonlune.smol_plugin.web.APIEndpoint;
import org.loudonlune.smol_plugin.web.Message;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class StatsAPI extends APIDirectory {
	
	public StatsAPI(StatsUtils statsUtils) {
		super("stats");
		initialize(statsUtils);
	}
	
	// TODO: make this work nice and have it not get cached for too long
	//public static OfflinePlayer[] offlinePlayers;
	
	public void initialize(StatsUtils statsUtils) {
		addMember(new APIEndpoint("getStatisticsList") {
			
			@SuppressWarnings("unchecked")
			@Override
			public int call(HttpServletRequest req, HttpServletResponse resp) {
				try {
					PrintWriter bodyWriter = resp.getWriter();
					
					JSONArray opt_array = new JSONArray();
					List<StatisticInfo> info = StatisticInfo.all();
					
					for (StatisticInfo i : info)
						opt_array.add(i.asJSON());
					
					bodyWriter.write(opt_array.toJSONString());
					
					bodyWriter.flush();
				} catch (IOException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
					return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				}
				
				return HttpServletResponse.SC_OK;
			}
			
		});
		
		addMember(new APIEndpoint("getBlockList") {
			
			@SuppressWarnings("unchecked")
			@Override
			public int call(HttpServletRequest req, HttpServletResponse resp) {
				try {
					PrintWriter bodyWriter = resp.getWriter();
					
					JSONArray opt_array = new JSONArray();
					
					for (Material m : Material.values())
						if (m.isBlock())
							opt_array.add(m.toString());
					
					bodyWriter.write(opt_array.toJSONString());
					bodyWriter.flush();
				} catch (IOException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
					return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				}

				return HttpServletResponse.SC_OK;
			}
		});
		
		addMember(new APIEndpoint("getItemList") {

			@SuppressWarnings("unchecked")
			@Override
			public int call(HttpServletRequest req, HttpServletResponse resp) {
				try {
					PrintWriter bodyWriter = resp.getWriter();
					
					JSONArray opt_array = new JSONArray();
					
					for (Material m : Material.values())
						if (m.isItem())
							opt_array.add(m.toString());
					
					bodyWriter.write(opt_array.toJSONString());
					bodyWriter.flush();
				} catch (IOException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
					return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				}
				
				return HttpServletResponse.SC_OK;
			}
			
		});
		
		addMember(new APIEndpoint("getEntityList") {

			@SuppressWarnings("unchecked")
			@Override
			public int call(HttpServletRequest req, HttpServletResponse resp) {
				try {
					PrintWriter bodyWriter = resp.getWriter();
					
					JSONArray opt_array = new JSONArray();
					
					for (EntityType et : EntityType.values())
						opt_array.add(et.toString());
					
					bodyWriter.write(opt_array.toJSONString());
					bodyWriter.flush();
				} catch (IOException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
					return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				}
				
				return HttpServletResponse.SC_OK;
			}
		});
		
		addMember(new APIEndpoint("getPlayerData") {

			@Override
			public int call(HttpServletRequest req, HttpServletResponse resp) {
				try {
					PrintWriter pw = resp.getWriter();
					
					String player = req.getParameter("player");
					if (player == null) {
						pw.write(Message.serializeString("requires a player name"));
						pw.flush();
						return HttpServletResponse.SC_BAD_REQUEST;
					}
					
					pw.write(new JSONObject(statsUtils.getStats(player)).toJSONString());
					pw.flush();
				} catch (IOException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
					return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				}
				
				return HttpServletResponse.SC_OK;
			}
			
		});
		
		addMember(new APIEndpoint("getPlayerList") {
			
			@SuppressWarnings("unchecked")
			@Override
			public int call(HttpServletRequest req, HttpServletResponse resp) {
				try {
					PrintWriter bodyWriter = resp.getWriter();
					
					JSONArray opt_array = new JSONArray();
					
					for (String s : statsUtils.getPlayerNameList())
						opt_array.add(s);
					
					bodyWriter.write(opt_array.toJSONString());
					bodyWriter.flush();
				} catch (IOException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
					return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				}
				
				return HttpServletResponse.SC_OK;
			}
		});
		
		addMember(new APIEndpoint("getPlayerHead") {

			@Override
			public int call(HttpServletRequest req, HttpServletResponse resp) {
				try {
					String value = req.getParameter("player");
					if (value == null) {	
						PrintWriter pw = resp.getWriter();
						pw.write(Message.serializeString("requires a player name"));
						pw.flush();
						return HttpServletResponse.SC_BAD_REQUEST;
					}
					
					BufferedImage head;
					String size = req.getParameter("size");
					if (size == null) {
						head = statsUtils.getPlayerHead(value);
					} else {
						int i; 
						try {
							i = Integer.parseInt(size);
						} catch (NumberFormatException nfe) {
							PrintWriter pw = resp.getWriter();
							pw.write(Message.serializeString(nfe.toString()));
							pw.flush();
							return HttpServletResponse.SC_BAD_REQUEST;
						}
						
						if (i > 1024 || i < 8) {
							PrintWriter pw = resp.getWriter();
							pw.write(Message.serializeString("size must be no greater than 1024 and greater than or equal to 8"));
							pw.flush();
							return HttpServletResponse.SC_BAD_REQUEST;
						}
						
						head = statsUtils.getPlayerHead(value);
						BufferedImage upsizedHead = new BufferedImage(i, i, BufferedImage.TYPE_4BYTE_ABGR);
						Graphics2D gfx_context = upsizedHead.createGraphics();
						
						gfx_context.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
						gfx_context.drawImage(head, 0, 0, i, i, 0, 0, 8, 8, null);
						
						head = upsizedHead;
					}
					
					if (head == null) {	
						PrintWriter pw = resp.getWriter();
						pw.write(Message.serializeString("skin lookup failed"));
						pw.flush();
						return HttpServletResponse.SC_NOT_FOUND;
					}
					
					ServletOutputStream os = resp.getOutputStream();
					ImageIO.write(head, "png", os);
					os.flush();
				} catch (IOException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
					return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				}

				return HttpServletResponse.SC_OK;
			}
			
		});
		
		addMember(new APIEndpoint("getData") {

			@SuppressWarnings("unchecked")
			@Override
			public int call(HttpServletRequest req, HttpServletResponse resp) {
				try {
					PrintWriter bodyWriter = resp.getWriter();
					String statStr = req.getParameter("stat");
					if (statStr == null) {
						bodyWriter.write(Message.serializeString("stat parameter missing"));
						bodyWriter.flush();
						return HttpServletResponse.SC_BAD_REQUEST;
					}
					
					Statistic stat = EnumUtils.tryGetStatistic(statStr);
					if (stat == null) {
						bodyWriter.write(Message.serializeString("stat parameter was malformed, it must be a value returned by getStatisticList"));
						bodyWriter.flush();
						return HttpServletResponse.SC_BAD_REQUEST;
					}
					
					HashMap<OfflinePlayer, Integer> stats;
					boolean isItem = false;
					switch (stat.getType()) {
					case ENTITY:
						String entityStr = req.getParameter("entity");
						
						if (entityStr == null) {
							bodyWriter.write(Message.serializeString("stat of entity type requires entity parameter, it's missing"));
							bodyWriter.flush();
							return HttpServletResponse.SC_BAD_REQUEST;
						}
						
						EntityType et = EnumUtils.tryGetEntityType(entityStr);
						if (et == null) {
							bodyWriter.write(Message.serializeString("entity parameter was malformed, it must be a value returned by getEntityList"));
							bodyWriter.flush();
							return HttpServletResponse.SC_BAD_REQUEST;
						}
						
						stats = statsUtils.precacheStatsEntity(Bukkit.getOfflinePlayers(), stat, et);
						break;
					case ITEM:
						isItem = true;
					case BLOCK:
						String matStr = req.getParameter(isItem ? "item" : "block");
						if (matStr == null) {
							bodyWriter.write(Message.serializeString("stat of the item or block type requires an item or block parameter, it's missing"));
							bodyWriter.flush();
							return HttpServletResponse.SC_BAD_REQUEST;
						}
						
						Material mat = EnumUtils.tryGetMaterial(matStr);
						if (mat == null) {
							bodyWriter.write(Message.serializeString("item or block was malformed, must be a string returned by getItemList or getBlockList"));
							bodyWriter.flush();
							return HttpServletResponse.SC_BAD_REQUEST;
						}
						
						stats = statsUtils.precacheStatsMaterial(Bukkit.getOfflinePlayers(), stat, mat);
						break;
					default:
						stats = statsUtils.precacheStats(Bukkit.getOfflinePlayers(), stat);
					}
					
					JSONArray opt = new JSONArray();
					for (Map.Entry<OfflinePlayer, Integer> e : stats.entrySet())
						opt.add(new StatisticResult(e.getKey().getName(), e.getValue()).asJSON());
					
					bodyWriter.write(opt.toJSONString());
					bodyWriter.flush();
				} catch (IOException e) {
					Bukkit.getLogger().log(Level.SEVERE, e.toString());
					return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				}
				
				return HttpServletResponse.SC_OK;
			}
			
		});
	}
}
