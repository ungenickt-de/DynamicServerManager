package com.playerrealms.bungee;

import com.playerrealms.bungee.redis.JedisAPI;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OnlineTimeTracker implements Runnable {

	private static final long REWARD_INTERVAL = TimeUnit.MINUTES.toMillis(30);
	private static final int REWARD_AMOUNT = 50;
	
	private Map<UUID, Long> nextReward;
	
	private long lastExecute;
	
	public OnlineTimeTracker() {
		nextReward = new HashMap<>();
		lastExecute = System.currentTimeMillis();
	}
	
	@Override
	public void run() {
		long amount = System.currentTimeMillis() - lastExecute;
		for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()){
			if(!nextReward.containsKey(player.getUniqueId())){
				nextReward.put(player.getUniqueId(), REWARD_INTERVAL);
			}
			
			long t = nextReward.get(player.getUniqueId());
			
			t -= amount;
			
			if(t <= 0){
				JedisAPI.publish("timed_reward", player.getUniqueId()+" "+REWARD_AMOUNT);
				t = REWARD_INTERVAL;
			}
			
			nextReward.put(player.getUniqueId(), t);
		}
		lastExecute = System.currentTimeMillis();
	}

}
