package com.playerrealms.bungee.ping;

import com.playerrealms.common.ServerInformation;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.Protocol;

public interface PingResponseGenerator {

	public ServerPing generate(Protocol protocol);
	
	public ServerPing generate(Protocol protocol, ServerInformation info);
}
