package com.playerrealms.bungee.ping;

import com.playerrealms.bungee.ServerManager;
import com.playerrealms.bungee.favicon.FaviconGenerator;
import com.playerrealms.bungee.redis.JedisAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.Protocol;
import net.md_5.bungee.api.chat.TextComponent;

public class JapanesePingResponseGenerator extends SimplePingResponseGenerator {

	public JapanesePingResponseGenerator(FaviconGenerator faviconGenerator) {
		super(faviconGenerator);
	}

	@Override
	public ServerPing generate(Protocol protocol) {
		/*String description = ServerManager.PLAYER_REALMS_TITLE+" "+ChatColor.WHITE+"自分のサーバーが作れます！\n"
				+ ChatColor.GREEN+ChatColor.WHITE+ServerManager.getInstance().getClient().getServers().size()+ChatColor.GREEN+"枚もサーバーがあります!";
		*/
		
		String motd = JedisAPI.getCachedValue("bungee_desc", 30000);
		
		String description = ServerManager.PLAYER_ISLANDS_TITLE+" "+ChatColor.GOLD+"Create your own server!\n"
				+ ChatColor.GREEN+ChatColor.WHITE+"Lets enjoy!";
		
		if(motd != null) {
			description = ChatColor.translateAlternateColorCodes('&', description);
		}
		
		ServerPing ping = super.generate(protocol);
		
		ping.setDescriptionComponent(new TextComponent(description));
		
		return ping;
	}
	
}
