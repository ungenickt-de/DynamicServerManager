package com.playerrealms.bungee.command;

import com.playerrealms.bungee.redis.JedisAPI;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class GiveCrateCommand extends Command {

	public GiveCrateCommand() {
		super("givecrates");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String uuid = args[0];
		String type = args[1];
		int amount = Integer.parseInt(args[2]);
		JedisAPI.publish("givecrates", uuid+" "+type+" "+amount);
	}
}
