package com.playerrealms.bungee.ping;

import com.playerrealms.bungee.ServerManager;
import com.playerrealms.bungee.favicon.FaviconGenerator;
import com.playerrealms.bungee.favicon.NullFaviconGenerator;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.ServerStatus;
import com.playerrealms.common.ServerType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.PlayerInfo;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.ServerPing.Protocol;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SimplePingResponseGenerator implements PingResponseGenerator {

	private FaviconGenerator faviconGenerator;
	
	private PlayerInfo[] cachedPlayerInfo;
	private long lastGenerateTime;

	public static int maxplayer = 1000;

	public SimplePingResponseGenerator(FaviconGenerator faviconGenerator) {
		if(faviconGenerator == null){
			faviconGenerator = new NullFaviconGenerator();
		}
		this.faviconGenerator = faviconGenerator;
		lastGenerateTime = 0;
	}
	
	@Override
	public ServerPing generate(Protocol protocol) {
		
		ServerPing ping = new ServerPing();

		String description = ServerManager.PLAYER_ISLANDS_TITLE+" "+ChatColor.WHITE+"Create your own server today!\n"
				+ ChatColor.GREEN+"The home of "+ChatColor.WHITE+ServerManager.getInstance().getClient().getServers().size()+ChatColor.GREEN+" servers!";
		
		ping.setFavicon(faviconGenerator.generate());
		ping.setPlayers(new Players(maxplayer, ServerManager.getInstance().getOnlinePlayers(), generatePlayerInfo()));
		ping.setVersion(protocol);
		ping.setDescriptionComponent(new TextComponent(description));
		
		return ping;
	}

	@Override
	public ServerPing generate(Protocol protocol, ServerInformation info) {
		
		ServerPing ping = generate(protocol);
		
		ping.setDescriptionComponent(new TextComponent(generateDescription(info)));
		ping.getPlayers().setMax(info.getMaxPlayers());
		ping.getPlayers().setOnline(info.getPlayersOnline());
		
		return ping;
	}
	
	private String generateDescription(ServerInformation info){
		return info.hasMotd() ? info.getMotd() : ChatColor.GRAY+"Connecting to "+info.getName()+"\n"+ChatColor.GRAY+"Hosted by PlayerIslands.";
	}
	
	private PlayerInfo[] convertToPlayerInfo(String[] data){
		PlayerInfo[] info = new PlayerInfo[data.length];
		for(int i = 0; i < data.length;i++){
			if(data[i] == null){
				info[i] = new PlayerInfo("", UUID.randomUUID());
			}else{
				info[i] = new PlayerInfo(data[i], UUID.randomUUID());
			}
			
		}
		return info;
	}
	
	private PlayerInfo[] generatePlayerInfo(){
		if(System.currentTimeMillis() - lastGenerateTime < 30000 && cachedPlayerInfo != null){
			return cachedPlayerInfo;
		}
		List<ServerInformation> info = ServerManager.getInstance().getClient().getServers();
		
		Collections.sort(info);
		
		String[] playerInfo = new String[7];
		playerInfo[0] = ServerManager.PLAYER_ISLANDS_TITLE;
		playerInfo[1] = ChatColor.GREEN+ChatColor.BOLD.toString()+"Top Servers";
		
		for(int i = 0, j = 0; i < playerInfo.length-2;i++){
			if(j > 5) {
				break;
			}
			if(info.size() <= i){
				break;
			}
			ServerInformation server = info.get(i);
			if(server.getPlayersOnline() == 0){
				continue;
			}
			if(server.getServerType() == ServerType.HUB){
				continue;
			}
			if(server.getStatus() != ServerStatus.ONLINE){
				continue;
			}
			playerInfo[2 + j] = ChatColor.AQUA.toString()+(j+1)+". "+server.getName()+ChatColor.GRAY+" ["+ChatColor.GREEN+server.getPlayersOnline()+ChatColor.GRAY+"/"+server.getMaxPlayers()+"]";
			j++;
		}
		
		cachedPlayerInfo = convertToPlayerInfo(playerInfo);
		lastGenerateTime = System.currentTimeMillis();
		
		return cachedPlayerInfo;
	}

}
