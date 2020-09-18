package com.playerrealms.bungee.command;

import com.playerrealms.bungee.redis.JedisAPI;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class GiveCoinsCommand extends Command {

	public GiveCoinsCommand() {
		super("givecoins");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String uuid = args[0];
		int amount = Integer.parseInt(args[1]);
		JedisAPI.publish("givecoins", uuid+" "+amount);
	}
}
