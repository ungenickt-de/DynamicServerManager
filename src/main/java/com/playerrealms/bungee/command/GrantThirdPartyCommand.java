package com.playerrealms.bungee.command;

import com.playerrealms.bungee.ServerManager;
import com.playerrealms.common.ServerInformation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GrantThirdPartyCommand extends Command {

	public GrantThirdPartyCommand() {
		super("givetp");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("playerrealms.admin")){
			String arg = "";
			for(int i = 0; i < args.length;i++) {
				arg += args[i] + " ";
			}
			((ProxiedPlayer) sender).chat("/givetp " + arg);
			return;
		}
		if(args.length < 2){
			sender.sendMessage(new TextComponent(ChatColor.RED+"/givetp [uuid] [days]"));
			return;
		}
		
		UUID uuid = UUID.fromString(args[0]);
		int days = Integer.parseInt(args[1]);
		
		int servers = 0;
		
		for(ServerInformation server : ServerManager.getInstance().getClient().getServers()){
			if(uuid.equals(server.getOwner())){
				long premium = server.getThirdPartyTimeLeft();
				
				premium += TimeUnit.DAYS.toMillis(days);
				
				premium += System.currentTimeMillis();
				
				ServerManager.getInstance().getClient().setMetadata(server.getName(), "tptime", String.valueOf(premium));
				
				servers++;
			}
		}
		
		sender.sendMessage(new TextComponent(ChatColor.GREEN+"Time added to "+servers+" servers."));
		
	}

}
