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

public class AddCoinBonusCommand extends Command {

	public AddCoinBonusCommand() {
		super("addcoinbonus");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("playerrealms.admin")){
			String arg = "";
			for(int i = 0; i < args.length;i++) {
				arg += args[i] + " ";
			}
			((ProxiedPlayer) sender).chat("/addcoinbonus " + arg);
			return;
		}
		
		if(args.length < 3){
			sender.sendMessage(new TextComponent(ChatColor.RED+"/addcoinbonus [uuid] [bonus] [days]"));
			return;
		}
		
		UUID uuid = UUID.fromString(args[0]);
		double bonus = Double.parseDouble(args[1]);
		int days = Integer.parseInt(args[2]);
		
		int servers = 0;
		
		for(ServerInformation server : ServerManager.getInstance().getClient().getServers()){
			if(uuid.equals(server.getOwner())){
				
				long bonusTime = server.getCoinMultiplierTimeLeft();
				
				bonusTime += TimeUnit.DAYS.toMillis(days);
				
				bonusTime += System.currentTimeMillis();
				
				ServerManager.getInstance().getClient().setMetadata(server.getName(), "multi", String.valueOf(bonus));
				ServerManager.getInstance().getClient().setMetadata(server.getName(), "multitime", String.valueOf(bonusTime));
				
				servers++;
			}
		}
		
		sender.sendMessage(new TextComponent(ChatColor.GREEN+"Bonus given to "+servers+" servers."));
		
	}

}
