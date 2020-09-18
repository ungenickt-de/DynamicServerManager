package com.playerrealms.bungee.command;

		import com.playerrealms.bungee.redis.JedisAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class SetRankGlobal extends Command {

	public SetRankGlobal() {
		super("setrankglobal");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		UUID uuid = UUID.fromString(args[0]);
		String rank = args[1];
		JedisAPI.publish("setrank", uuid+" "+rank);
		sender.sendMessage(new TextComponent("Set rank for "+uuid+" to "+rank));
	}
}
