package com.playerrealms.bungee.command;

import com.playerrealms.bungee.ServerManager;
import com.playerrealms.client.ServerManagerClient;
import com.playerrealms.common.ServerInformation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class SetUltraPremiumCommand extends Command {

	public SetUltraPremiumCommand() {
		super("setultra");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length < 2){
			sender.sendMessage(new TextComponent(ChatColor.RED+"/setultra [uuid] [on / off]"));
			return;
		}
		ServerManagerClient client = ServerManager.getInstance().getClient();
		UUID id = UUID.fromString(args[0]);
		String option = args[1].equals("on") ? "y" : "n";
		for(ServerInformation server : client.getServers(server -> id.equals(server.getOwner()))){
			client.setMetadata(server.getName(), "ultra", option);
			sender.sendMessage(new TextComponent(ChatColor.GREEN+"Set "+server.getName()+" to ultra premium."));
		}
		sender.sendMessage(new TextComponent(ChatColor.GREEN+"Done."));
	}
}
