package com.playerrealms.bungee;

import java.util.concurrent.atomic.AtomicLong;

import com.maxmind.geoip2.record.Country;
import com.playerrealms.client.NoAvailableServerException;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.ServerStatus;
import com.playerrealms.common.ServerType;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BetterReconnectHandler implements ReconnectHandler {

	private final ServerManager manager;
	
	public BetterReconnectHandler(ServerManager man) {
		manager = man;
	}
	
	@Override
	public ServerInfo getServer(ProxiedPlayer player) {
		ServerInfo desired = ServerManager.getDesiredServer(player);
		Country location = null;
		
		try {
			location = manager.getIpLookup().country(player.getAddress().getAddress()).getCountry();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		desired = (desired == null ? ServerManager.findHub(null) : desired);
		
		if(desired == null){
			
			if(location != null && location.getName().equalsIgnoreCase("Japan")){
				player.disconnect(new TextComponent(ChatColor.RED+"No lobby!"));
			}else{
				player.disconnect(new TextComponent(ChatColor.RED+"Could not find a hub to place you in."));
			}
			
			return null;
		}
		
		ServerManager.getInstance().getLogger().info("Selected server "+desired+" for "+player.getName());
		
		return desired;
	}

	@Override
	public void setServer(ProxiedPlayer player) {
		
	}

	@Override
	public void save() {
		
	}

	@Override
	public void close() {
		
	}

}
