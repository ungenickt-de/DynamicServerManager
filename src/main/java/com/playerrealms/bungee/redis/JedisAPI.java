package com.playerrealms.bungee.redis;

import com.playerrealms.bungee.ServerManager;
import com.playerrealms.client.redis.JedisListener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.*;
import java.util.function.Consumer;

public class JedisAPI {

	private static JedisPool pool;
	private static Map<String, CachedValue> cache = Collections.synchronizedMap(new HashMap<>());
	public static Map<UUID, Integer> rewards = Collections.synchronizedMap(new HashMap<>());
	private static JedisPubSub pubSub;
	private static ScheduledTask task;
	private static List<JedisListener> listeners;

	public static void setup(String host, int port, String password){
		if(pool != null){
			throw new IllegalStateException("Jedis already initialized");
		}
		JedisPoolConfig config = new JedisPoolConfig();
		pool = new JedisPool(config, host, port, 200, password);
		listeners = Collections.synchronizedList(new ArrayList<>());

		registerListener(new LinkListener());
		if(ServerManager.isPluginLoaded("RedisBungee")) {
			registerListener(new DonorChatListener());
			registerListener(new AdminChatListener());
			registerListener(new ReportListener());
			registerListener(new GlobalTellListener());
		}

		task = ServerManager.getInstance().getProxy().getScheduler().runAsync(ServerManager.getInstance(), new Runnable() {
			@Override
			public void run() {
				pubSub = null;
				try (Jedis jedis = getJedis()) {
					pubSub = new JedisPubSub() {
						@Override
						public void onMessage(String channel, String message) {
							if(!ServerManager.getInstance().isEnabled()) {
								pubSub.unsubscribe();
								return;
							}
							try{
								if (channel.equals("cacheUpdate")){
									synchronized (cache){
										cache.remove(message);
									}
								}else{
									synchronized (listeners) {
										for (JedisListener jl : listeners){
											for(String ch : jl.getChannel()) {
												if(ch.equals(channel)) {
													ServerManager.getInstance().getProxy().getScheduler().runAsync(ServerManager.getInstance(), () -> jl.onMessage(channel, message));
													break;
												}
											}
										}
									}
								}
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					};
					jedis.subscribe(pubSub,
							"cacheUpdate",
							"adminchat",
							"globaltell",
							"donorchat",
							"rankReload",
							"report",
							"link");
				}finally {
					pubSub.unsubscribe();
				}
				ServerManager.getInstance().getLogger().info("Jedis pubsub ended");
			}
		});
	}
	
	public static void subscribe(String ch, Consumer<String> onGet) {
		try(Jedis jedis = getJedis()){
			jedis.subscribe(new JedisPubSub() {
				@Override
				public void onMessage(String channel, String message) {
					onGet.accept(channel);
				}
			}, ch);
		}
	}
	
	public static void subscribe(String[] ch, JedisPubSub pubsub) {
		try(Jedis jedis = getJedis()){
			jedis.subscribe(pubsub, ch);
		}
	}
	
	public static void publish(String ch, String msg){
		try(Jedis jedis = getJedis()){
			jedis.publish(ch, msg);
		}
	}

	public static void registerListener(JedisListener listener){
		if(!isValid()) {
			return;
		}
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public static boolean isValid(){
		try(Jedis jedis = getJedis()){
			return jedis.isConnected();
		}
	}
	
	public static void destroy(){
		task.cancel();
		pool.destroy();
		pool = null;
	}
	
	private static Jedis getJedis(){
		return pool.getResource();
	}
	
	public static void setKey(String key, String value){
		try(Jedis jedis = getJedis()){
			jedis.set(key, value);
			synchronized (cache) {
				if(cache.containsKey(key)){
					CachedValue old = cache.get(key);
					jedis.publish("cacheUpdate", key);
					cache.put(key, old);
				}
			}
			
		}
	}
	
	public static String getValue(String key){
		try(Jedis jedis = getJedis()){
			return jedis.get(key);
		}
	}
	
	public static String getCachedValue(String key, long cacheTime){
		synchronized (cache) {
			if(cache.containsKey(key)){
				CachedValue value = cache.get(key);
				if(value.expireTime - System.currentTimeMillis() > 0){
					return value.value;
				}
			}
			String value = getValue(key);
			CachedValue cv = new CachedValue();
			cv.value = value;
			cv.expireTime = System.currentTimeMillis() + cacheTime;
			cache.put(key, cv);
			return value;
		}
	}

	public static void setKeyExpire(String key, String value, int expireTime){
		try(Jedis jedis = getJedis()){
			jedis.setex(key, expireTime, value);
			synchronized (cache) {
				if(cache.containsKey(key)){
					CachedValue old = cache.get(key);
					jedis.publish("cacheUpdate", key);
					cache.put(key, old);
				}
			}
		}
	}

	public static void removeKey(String key) {
		try(Jedis jedis = getJedis()){
			jedis.del(key);
			cache.remove(key);
		}
	}
	
	public static boolean keyExists(String key){
		try(Jedis jedis = getJedis()){
			return jedis.exists(key);
		}
	}
	
	public static void cacheKey(String key, long time) {
		getCachedValue(key, time);
	}
	
	public static void updateCache(){
		synchronized (cache) {
			List<String> remove = new LinkedList<>();
			for(String str : cache.keySet()){
				CachedValue value = cache.get(str);
				if(value.expireTime - System.currentTimeMillis() < 0){
					remove.add(str);
				}
			}
			ServerManager.getInstance().getLogger().info("Cleared "+remove.size()+" values from cache.");
			for(String re : remove){
				cache.remove(re);
			}
		}
	}

	static class CachedValue {
		private String value;
		private long expireTime;
	}
}
