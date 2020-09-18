package com.playerrealms.bungee;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.mongodb.MongoClient;
import com.playerrealms.bungee.command.*;
import com.playerrealms.bungee.favicon.FaviconGenerator;
import com.playerrealms.bungee.favicon.SimpleFaviconGenerator;
import com.playerrealms.bungee.other.Language;
import com.playerrealms.bungee.ping.JapanesePingResponseGenerator;
import com.playerrealms.bungee.ping.PingResponseGenerator;
import com.playerrealms.bungee.ping.SimplePingResponseGenerator;
import com.playerrealms.bungee.redis.JedisAPI;
import com.playerrealms.bungee.sql.DatabaseAPI;
import com.playerrealms.bungee.sql.QueryResult;
import com.playerrealms.bungee.thirdparty.ThirdPartyServer;
import com.playerrealms.client.ServerManagerClient;
import com.playerrealms.client.ServerUpdateListener;
import com.playerrealms.client.redis.JedisListener;
import com.playerrealms.client.redis.RedisInterface;
import com.playerrealms.client.redis.RedisMongoManagerClient;
import com.playerrealms.common.ResponseCodes;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.ServerStatus;
import com.playerrealms.common.ServerType;
import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing.Protocol;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.JedisPubSub;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ServerManager extends Plugin implements ServerUpdateListener, Listener {

	public static final String PLAYER_REALMS_TITLE = ChatColor.AQUA+"Player"+ChatColor.LIGHT_PURPLE+"Realms";

	public static final String PLAYER_ISLANDS_TITLE = ChatColor.AQUA+"Player"+ChatColor.LIGHT_PURPLE+"Islands";

	public static final String PREFIX = ChatColor.WHITE+"["+ChatColor.AQUA+"Player"+ChatColor.LIGHT_PURPLE+"Islands"+ChatColor.WHITE+"] ";
	
	private static ServerManager instance = null;
	
	protected static RedisMongoManagerClient client;
	
	private static String wanIp = "";
	
	private FaviconGenerator faviconGenerator;
	
	private Map<UUID, String> desiredServer;

	private static List<Integer> donateranks;
	
	private DatabaseReader ipLookup;
	
	private MongoClient mongoClient;

	private boolean enabled;

	private static HashMap<Integer,String> ranks = new HashMap<>();



	@Override
	public void onEnable() {
		instance = this;
		desiredServer = new HashMap<>();
		
		Configuration config;
		try {
			config = downloadConfig();
			mongoClient = new MongoClient(config.getString("mongo_remote"), 27017);
		} catch (IOException e2) {
			e2.printStackTrace();
			return;
		}
		
		ProxyServer.getInstance().registerChannel("PlayerRealms");
		//ProxyServer.getInstance().registerChannel("PlayerIslands");

		JedisAPI.setup(config.getString("redis_remote"), 6379, config.getString("redis_password"));
		
		client = new RedisMongoManagerClient(new RedisInterface() {
			@Override
			public void subscribe(JedisListener listener) {
				getProxy().getScheduler().runAsync(getInstance(), new Runnable() {
					@Override
					public void run() {
						try {
							JedisAPI.subscribe(listener.getChannel(), new JedisPubSub() {
								@Override
								public void onMessage(String channel, String message) {
									listener.onMessage(channel, message);
								}
							});
						}catch (IllegalStateException e){

						}
					}
					
				});
			}
			
			@Override
			public void shutdown() {
				
			}
			
			@Override
			public void publish(String ch, String msg) {
				JedisAPI.publish(ch, msg);
			}
			
		}, mongoClient.getDatabase("playerrealms"));
		
		try {
			File database = new File("ip_database.mmdb");
			
			ipLookup = new DatabaseReader.Builder(database).build();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if(!getDataFolder().exists()){
			getDataFolder().mkdir();
		}
		
		setupConfig();
		
		client.addListener(this);
		
		/*try {
			Configuration config = downloadConfig();
			
			for(Object obj : config.getList("servers")){
				Map<?,?> map = (Map<?,?>) obj;
				String ip = map.get("ip").toString();
				int port = Integer.parseInt(map.get("port").toString());

				client.addConnection(ip, port);
				
				getLogger().info("Preparing to connect to "+ip+":"+port);
				
			}
			JedisAPI.setup(config.getString("redis"), 6379);

		} catch (IOException e1) {
			e1.printStackTrace();
			client.addConnection("127.0.0.1", 8484);
		}*/
		
		client.connect("", 0);//Doesn't matter for redis mongo one
		
		getProxy().getPluginManager().registerListener(this, this);
		
		getProxy().setReconnectHandler(new BetterReconnectHandler(this));
		
		try (InputStream is = getResourceAsStream("Minecraft.ttf")){
			BufferedImage blocks = load("blocks.png");
			BufferedImage base = load("base.png");
			Font font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16F);
			
			faviconGenerator = new SimpleFaviconGenerator(blocks, base, font);
			
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}

		Language.registerLanguage(getResourceAsStream("lang/en_US.yml"), "en_us");
		Language.registerLanguage(getResourceAsStream("lang/ja_JP.yml"), "ja_jp");

		getProxy().getPluginManager().registerCommand(this, new AddPremiumTimeCommand());
		getProxy().getPluginManager().registerCommand(this, new AddCoinBonusCommand());
		getProxy().getPluginManager().registerCommand(this, new SetRankGlobal());
		getProxy().getPluginManager().registerCommand(this, new GiveCoinsCommand());
		getProxy().getPluginManager().registerCommand(this, new SetUltraPremiumCommand());
		getProxy().getPluginManager().registerCommand(this, new GiveCrateCommand());
		getProxy().getPluginManager().registerCommand(this, new GrantGemCommand());
		getProxy().getPluginManager().registerCommand(this, new GrantThirdPartyCommand());
		//getProxy().getPluginManager().registerCommand(this, new ReconnectRedisCommand());
		getProxy().getPluginManager().registerCommand(this, new FallbackServerCommand());
		getProxy().getPluginManager().registerCommand(this, new AdminChatCommand());
		getProxy().getPluginManager().registerCommand(this, new SetProxyRank());
		getProxy().getPluginManager().registerCommand(this, new ReportCommand());
		getProxy().getPluginManager().registerCommand(this, new ProxyManageCommand());
		getProxy().getPluginManager().registerCommand(this ,new PlayBanCommand());
		getProxy().getPluginManager().registerCommand(this ,new PlayUnBanCommand());
		getProxy().getPluginManager().registerCommand(this, new LinkCommand());
		getProxy().getPluginManager().registerCommand(this, new UnlinkCommand());
		getProxy().getPluginManager().registerCommand(this, new GlobalTellCommand());
		getProxy().getPluginManager().registerCommand(this, new DonorChatCommand());
		getProxy().getPluginManager().registerCommand(this, new ServerCommand());
		
		getProxy().getScheduler().schedule(this, new OnlineTimeTracker(), 0, 2, TimeUnit.SECONDS);
		
		ThirdPartyServer tps = new ThirdPartyServer();
		
		ProxyServer.getInstance().getScheduler().runAsync(this, tps);

		donateranks = config.getIntList("donor");

		DatabaseAPI.setup("jdbc:mysql://"+config.getString("mysql.remote_ip")+"/playerrealms?useUnicode=yes&characterEncoding=UTF-8", config.getString("mysql.remote_password"));

		loadRanks();

		JedisAPI.registerListener(new JedisListener() {
			@Override
			public void onMessage(String ch, String message) {
				loadRanks();
			}

			@Override
			public String[] getChannel() {
				return new String[] {"rankReload"};
			}
		});

		try{
			DatabaseAPI.execute("UPDATE `players` SET `online`=0");
		}catch (SQLException e){
			e.printStackTrace();
		}

		enabled = true;
	}

	private void loadRanks(){
		ranks.clear();

		try{
			List<QueryResult> ranksResults = DatabaseAPI.query("SELECT `id`,`prefix` FROM `ranks`");

			for(QueryResult result : ranksResults){
				int id = result.get("id");
				String prefix = result.get("prefix");
				ranks.put(id, prefix);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void runAsync(Runnable runnable) {
		getInstance().getProxy().getScheduler().runAsync(getInstance(), runnable);
	}
	
	public static Configuration downloadConfig() throws IOException {
		URL url = new URL("");
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent", "");
		con.setRequestProperty("API-Key", "");
		Configuration config;
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))){
			config = YamlConfiguration.getProvider(YamlConfiguration.class).load(reader);
		}
		
		return config;
	}
	
	public DatabaseReader getIpLookup() {
		return ipLookup;
	}
	
	private PingResponseGenerator getPingGenerator(InetSocketAddress inetSocketAddress) throws IOException, GeoIp2Exception{
		
		CountryResponse response = ipLookup.country(inetSocketAddress.getAddress());

		if(response == null || response.getCountry().getName().equalsIgnoreCase("japan")){
			return new JapanesePingResponseGenerator(faviconGenerator);
		}else{
			return new SimplePingResponseGenerator(faviconGenerator);
		}
		
	}
	
	public ServerManagerClient getClient() {
		return client;
	}
	
	public static ServerManager getInstance() {
		return instance;
	}
	
	@Override
	public void onDisable() {
		enabled = false;
		for(ServerInformation info : client.getServers()){
			if(info.isThirdParty()){
				ServerManager.getInstance().getClient().setMetadata(info.getName(), "tponline", "");
				ServerManager.getInstance().getClient().setMetadata(info.getName(), "tpip", "");
				ServerManager.getInstance().getClient().setMetadata(info.getName(), "tpport", "");
			}
		}
		JedisAPI.destroy();
		mongoClient.close();
		client.shutdown();
	}

	public static ServerInfo findHub(ServerInfo excluding){
		ServerInformation hub = null;
		for(ServerInformation info : instance.client.getServers()){
			if(excluding != null && info.getName().equals(excluding.getName())) {
				continue;
			}
			if(info.getServerType() == ServerType.HUB && info.getStatus() == ServerStatus.ONLINE){
				if(info.isClosedForDevelopment()){
					continue;
				}
				if(hub == null)	{
					hub = info;
				}else if(hub.getPlayersOnline() > info.getPlayersOnline()) {
					hub = info;
				}
			}
		}
		if(hub == null){
			return null;
		}
		return instance.getProxy().getServerInfo(hub.getName());
	}
	
	@EventHandler
	public void onPluginChannelOpen(net.md_5.bungee.api.event.PluginMessageEvent event){
		/*System.out.println("Plugin message "+event.getTag()+" "+event.getSender()+" "+event.getReceiver());*/
		if(event.getTag().equals("PlayerRealms")){
			
			ByteArrayInputStream bis = new ByteArrayInputStream(event.getData());
			
			DataInputStream dis = new DataInputStream(bis);
			
			String type;
			try {
				type = dis.readUTF();
				
				if(type.equalsIgnoreCase("SendToHub")){
					String player = dis.readUTF();
					
					ProxiedPlayer pl = ProxyServer.getInstance().getPlayer(player);
					
					if(event.getSender() instanceof Server){
						Server server = (Server) event.getSender();
						
						if(pl.getServer().equals(server)){
							pl.connect(findHub(null));
						}
						event.setCancelled(true);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			
			
		}else if(event.getTag().equals("BungeeCord")){
			if(event.getSender() instanceof Server){
				Server server = (Server) event.getSender();
				
				String name = server.getInfo().getName();
				ServerInformation prData = client.getServerByName(name);
				
				if(prData.isThirdParty()){
					
					ByteArrayInputStream bis = new ByteArrayInputStream(event.getData());
					
					DataInputStream dis = new DataInputStream(bis);
					
					String channel;
					try {
						channel = dis.readUTF();
						if(
								channel.equalsIgnoreCase("Connect")
								|| 
								channel.equalsIgnoreCase("ConnectOther")
								|| 
								channel.equalsIgnoreCase("PlayerCount")
								|| 
								channel.equalsIgnoreCase("PlayerList")
								|| 
								channel.equalsIgnoreCase("GetServers")
								|| 
								channel.equalsIgnoreCase("Message")
								|| 
								channel.equalsIgnoreCase("GetServer")
								|| 
								channel.equalsIgnoreCase("Forward")
								|| 
								channel.equalsIgnoreCase("ForwardToPlayer")
								|| 
								channel.equalsIgnoreCase("ServerIP")
								|| 
								channel.equalsIgnoreCase("KickPlayer")){
							
							event.setCancelled(true);
							getLogger().warning(name+" tried to send a plugin message "+channel+" but it was blocked.");
							
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					
					
				}
			}
		}
	}
	
	@EventHandler
	public void onVote(VotifierEvent event){
		Vote vote = event.getVote();
		
		JedisAPI.publish("vote", vote.getUsername()+" 300");
	}
	
	@EventHandler
	public void onPlayerJoin(LoginEvent event) {
		int acc = 0;
		String ip = event.getConnection().getAddress().getAddress().getHostAddress();
		for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()) {
			if(!pl.getUniqueId().equals(event.getConnection().getUniqueId())) {
				if(pl.getAddress().getHostString().equals(ip)){
					acc += 1;
				}
			}
		}
		if(acc >= 3) {
			event.setCancelled(true);
			event.setCancelReason("同じIPからの接続は3つまでです");
			return;
		}
	}
	
	@EventHandler
	public void onPlayerConnect(PostLoginEvent event){
		if(isPluginLoaded("RedisBungee")) {
			JedisAPI.setKey("onlinecount", String.valueOf(RedisBungee.getApi().getPlayerCount() + 1));
		}else{
			JedisAPI.setKey("onlinecount", String.valueOf(getProxy().getOnlineCount()));
		}

		try {
			DatabaseAPI.execute("UPDATE `players` SET `online`=1 WHERE `UUID`=?", event.getPlayer().getUniqueId());
		}catch (SQLException e){
			e.printStackTrace();
		}

		try{
			Language.setLanguage(event.getPlayer(), event.getPlayer().getLocale().toString());
		} catch (NullPointerException e){
			Language.setLanguage(event.getPlayer(), "ja_JP");
		}
	}
	
	@EventHandler
	public void onDisconnet(PlayerDisconnectEvent event){
		if(isPluginLoaded("RedisBungee")){
			JedisAPI.setKey("onlinecount", String.valueOf(RedisBungee.getApi().getPlayerCount() - 1));
		}else {
			JedisAPI.setKey("onlinecount", String.valueOf(getProxy().getOnlineCount() - 1));
		}

		try{
			DatabaseAPI.execute("UPDATE `players` SET `online`=0 WHERE `UUID`=?", event.getPlayer().getUniqueId());
		}catch (SQLException e){
			e.printStackTrace();
		}
	}

	public static boolean isPluginLoaded(String name) {
		if(ProxyServer.getInstance().getPluginManager().getPlugin(name) != null){
			return true;
		}
		return false;
	}

	@EventHandler
	public void onKick(ServerKickEvent event){
		//JedisAPI.setKey("cname_connect."+event.getPlayer().getUniqueId(), "true");
		if(JedisAPI.keyExists("cname_connect."+event.getPlayer().getUniqueId())) {
			event.setCancelled(false);
			event.setCancelServer(null);
			return;
		}
		
		if(event.getPlayer().getServer() != null){
			ServerInformation information = client.getServerByName(event.getKickedFrom().getName());
			ServerInformation current = client.getServerByName(event.getPlayer().getServer().getInfo().getName());
			
			if(information != null && information.getServerType() == ServerType.HUB && current != null && current.getServerType() == ServerType.HUB){
				event.setCancelled(false);
				event.setCancelServer(null);
				return;
			}
		}
		
		
		
		event.setCancelled(true);
		event.setCancelServer(findHub(event.getKickedFrom()));
		if(event.getCancelServer() == null){
			getLogger().info("Redirecting disconnect to "+event.getCancelServer().getName());
			event.getPlayer().sendMessage(event.getKickReasonComponent());
			event.setCancelled(false);
		}else if(event.getCancelServer().equals(event.getPlayer().getServer().getInfo())) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onConnectToServer(ServerConnectedEvent event){
		TextComponent header = new TextComponent(PLAYER_ISLANDS_TITLE);
		TextComponent footer = new TextComponent(ChatColor.YELLOW+"Playing on "+ChatColor.AQUA+event.getServer().getInfo().getName());
		event.getPlayer().setTabHeader(header, footer);
		if(JedisAPI.rewards.containsKey(event.getPlayer().getUniqueId())) {
			int amount = JedisAPI.rewards.get(event.getPlayer().getUniqueId());
			JedisAPI.publish("givecrates", event.getPlayer().getUniqueId()+" cm_reward "+amount);
			JedisAPI.rewards.remove(event.getPlayer().getUniqueId());
		}
	}
	
	private BufferedImage load(String name) throws IOException{
		try(InputStream is = getResourceAsStream(name)){
			return ImageIO.read(is);
		}
	}
	
	@EventHandler
	public void onLogin(PostLoginEvent event) {
		InetSocketAddress address = event.getPlayer().getPendingConnection().getVirtualHost();
		String subdomain = getCName(address);
		//JedisAPI.removeKey("cname_connect."+event.getPlayer().getUniqueId());
		if (subdomain != null) {
			ServerInfo info = getProxy().getServerInfo(subdomain);
			
			if(info != null){
				desiredServer.put(event.getPlayer().getUniqueId(), info.getName());
				//JedisAPI.setKey("cname_connect."+event.getPlayer().getUniqueId(), "true");
			}
		}
		if(event.getPlayer().hasPermission("playerrealms.admin")){
			if(!JedisAPI.isValid() || !DatabaseAPI.isValid()){
				event.getPlayer().sendMessage(new TextComponent(ChatColor.RED + "--- Immediately ---"));
				event.getPlayer().sendMessage(new TextComponent(ChatColor.RED + "The proxy need to be rebooted."));
			}
		}
	}
	
	private String getCName(InetSocketAddress address){
		if(address == null){
			return null;
		}
		String domain = address.getHostName();

		String[] splits = domain.split("\\.");
		
		if (splits.length == 3) {
			String subdomain = splits[0];

			return subdomain;
		}
		return null;
	}

	private boolean isIslands(InetSocketAddress address){
		if(address == null){
			return false;
		}
		String domain = address.getHostName();

		List lists = Arrays.asList(domain.split("\\."));
		Collections.reverse(lists);
		String[] splits = (String[]) lists.toArray();

		if (splits[1].equals("playerislands")) {
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onServerPing(ProxyPingEvent event){
		Protocol protocol = event.getResponse().getVersion();
		String subdomain = getCName(event.getConnection().getVirtualHost());
		ServerInformation selected = null;
		if(subdomain != null){
			selected = client.getServerByName(subdomain);
		}
		
		try {
			PingResponseGenerator pingGenerator = getPingGenerator(event.getConnection().getAddress());
			
			if(selected != null){
				event.setResponse(pingGenerator.generate(protocol, selected));
			}else{
				event.setResponse(pingGenerator.generate(protocol));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeoIp2Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static String getIP(){
		if(!wanIp.isEmpty()){
			return wanIp;
		}
		try {
			URL whatismywanIp = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(
			                whatismywanIp.openStream()));

			wanIp = in.readLine(); //you get the wanIp as a String
			return wanIp;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private void setupConfig(){
		try {
			
			File configFile = new File(getDataFolder(), "config.yml");
			
			boolean created = false;
			
			if(!configFile.exists()){
				configFile.createNewFile();
				created = true;
			}
			
			Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
			
			if(created){
				config.set("ip", "127.0.0.1");
				config.set("port", "8484");
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configFile);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onServerListReceive(Collection<ServerInformation> info) {
		
		for(ServerInformation i : info){
			onServerReceive(i);
		}
		
	}

	@Override
	public void onServerReceive(ServerInformation info) {
		
		InetSocketAddress address = new InetSocketAddress(info.getIp(), info.getPort());
		
		/*if(info.getIp().equals(getIP())){
			address = new InetSocketAddress("127.0.0.1", info.getPort());
		}*/
		
		ServerInfo existing = getProxy().getServerInfo(info.getName());
		if(existing != null){
			if(existing.getAddress().getPort() == info.getPort()){
				return;
			}
		}
		
		if(info.getStatus() == ServerStatus.ONLINE){
			getProxy().getServers().put(info.getName(), getProxy().constructServerInfo(info.getName(), address, "No MOTD", false));
		}else{
			removeServer(info.getName());
		}
		
		
	}

	@Override
	public void onServerDeleted(String name) {
		removeServer(name);
		getLogger().info("Removed server "+name);
	}

	@Override
	public void onReply(long responseId, ResponseCodes code) {
		
	}

	@Override
	public void onConsoleRead(long responseId, String line) {
		
	}

	@Override
	public void onDisconnectFromServerManager() {
		getLogger().info("Disconnected from server manager.");
		getProxy().stop();
	}

	@Override
	public void onServerStatusChange(ServerInformation info, ServerStatus old) {
		if(info.getStatus() == ServerStatus.ONLINE){
			InetSocketAddress address = new InetSocketAddress(info.getIp(), info.getPort());
			
			/*if(info.getIp().equals(getIP())){
				address = new InetSocketAddress("127.0.0.1", info.getPort());
			}*/
			getProxy().getServers().put(info.getName(), getProxy().constructServerInfo(info.getName(), address, "No MOTD", false));
		}
	}

	public static ServerInfo getDesiredServer(ProxiedPlayer player) {
		String desired = instance.desiredServer.remove(player.getUniqueId());
		if(desired != null){
			ServerInfo info = instance.getProxy().getServerInfo(desired);
			return info;
		}
		return null;
	}
	
	private static void removeServer(String name){
		
		ServerInfo info = ProxyServer.getInstance().getServerInfo(name);
		
		if(info != null){
			for(ProxiedPlayer pp : info.getPlayers()){
				pp.disconnect(new TextComponent(ChatColor.RED+"Server shutdown."));
			}
			
			ProxyServer.getInstance().getServers().remove(name);
		}
		
	}

	public int getOnlinePlayers() {
		int players = 0;
		for(ServerInformation info : client.getServers()){
			if(info.getStatus() == ServerStatus.ONLINE){
				players += info.getPlayersOnline();
			}
		}
		return players;
	}

	public static boolean isDonor(ProxiedPlayer player){
		if(donateranks.contains(DropletAPI.getRank(player))){
			return true;
		}
		return false;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public static String getPrefixById(int id){
		return ranks.get(id);
	}


}
