package com.playerrealms.bungee.command;

import com.playerrealms.bungee.redis.JedisAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class GrantGemCommand extends Command {

	public GrantGemCommand() {
		super("grantgems");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		UUID uuid = UUID.fromString(args[0]);
		String name = args[1];
		int amount = Integer.parseInt(args[2]);
		String reason = "";
		for(int i = 3; i < args.length;i++) {
			reason += args[i] + " ";
		}
		JedisAPI.publish("grantgems", uuid+" "+name+" "+amount+" "+reason);
	}
}
