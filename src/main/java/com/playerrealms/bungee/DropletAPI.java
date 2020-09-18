package com.playerrealms.bungee;

import com.playerrealms.bungee.other.Language;
import com.playerrealms.bungee.redis.JedisAPI;
import com.playerrealms.bungee.sql.DatabaseAPI;
import com.playerrealms.bungee.sql.QueryResult;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.ServerStatus;
import com.playerrealms.common.ServerType;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DropletAPI {

	private DropletAPI() { }

	public static int getRank(ProxiedPlayer player) {
		return getRank(player.getUniqueId());
	}
	
	public static int getRank(UUID uuid) {
		if(!JedisAPI.keyExists("ranks."+uuid)){
			List<QueryResult> results = Collections.emptyList();
			try {
				results = DatabaseAPI.query("SELECT `rank` FROM `players` WHERE `uuid`=?", uuid.toString());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			//Select their data
			if(results.size() > 0){//This should always be true
				int rank = results.get(0).get("rank");
				
				//Cache values
				JedisAPI.setKey("ranks."+uuid.toString(), String.valueOf(rank));
			}else{
				return 0;
			}
		}
		int rank = Integer.valueOf(JedisAPI.getCachedValue("ranks."+uuid, 60000));
		
		return rank;
	}

	public static Collection<ServerInformation> getServers(){
		return ServerManager.client.getServers();
	}

	public static ServerInformation getHub(ProxiedPlayer pl) {

		for(ServerInformation info : getServers()){
			if(info.getStatus() != ServerStatus.ONLINE){
				continue;
			}
			if(!info.getLanguage().equalsIgnoreCase(pl.getLocale().toString())){
				continue;
			}
			if(info.getServerType() == ServerType.HUB){
				return info;
			}
		}

		return getHub();
	}

	private static ServerInformation getHub() {
		for(ServerInformation info : getServers()){
			if(info.getServerType() == ServerType.HUB){
				return info;
			}
		}
		return null;
	}

	public static List<ServerInformation> getPlayerServers(){
		return ServerManager.client.getServers().stream().filter(info -> !info.isOfficial()).filter(server -> server.getServerType() == ServerType.PLAYER).collect(Collectors.toList());
	}

	public static ServerInformation getServerInfo(String name){
		return ServerManager.client.getServerByName(name);
	}

	public static void connectToServer(ProxiedPlayer player, ServerInformation sinfo){
		final ServerInformation info = getServerInfo(sinfo.getName()); //Retrieve most updated
		if(info == null){
			Language.sendMessage(player, "connect_not_found");
			return;
		}else if(info.getStatus() == ServerStatus.OFFLINE){
			Language.sendMessage(player, "connect_offline");
			return;
		}else if(info.getStatus() == ServerStatus.STARTING){
			Language.sendMessage(player, "connect_starting");
			return;
		}else if(info.getStatus() == ServerStatus.ONLINE){
			Language.sendMessage(player, "connect_success", info.getName());
		}
		ServerInfo to = ServerManager.getInstance().getProxy().getServerInfo(info.getName());
		player.connect(to);
	}
}
