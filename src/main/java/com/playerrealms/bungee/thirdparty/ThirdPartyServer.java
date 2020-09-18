package com.playerrealms.bungee.thirdparty;

import com.playerrealms.bungee.ServerManager;
import com.playerrealms.common.PacketDecoder;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.thirdparty.ThirdPartyConnection;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.util.AttributeKey;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.util.UUID;

@Sharable
public class ThirdPartyServer extends ChannelInboundHandlerAdapter implements Runnable {

	private static final AttributeKey<String> SERVER_KEY = AttributeKey.newInstance("SERVER_NAME");
	private static String PREFIX = ChatColor.WHITE+"["+ChatColor.BLUE+"Player"+ChatColor.LIGHT_PURPLE+"Islands"+ChatColor.WHITE+"] ";
	
	@Override
	public void run() {
		int port = ThirdPartyConnection.PORT;
		
		EventLoopGroup worker, boss;
		
		worker = new NioEventLoopGroup();
		boss = new NioEventLoopGroup();
		
		ServerBootstrap bs = new ServerBootstrap();
		
		try{
			bs.group(boss, worker)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new PacketDecoder(), ThirdPartyServer.this);
				}
				
			})
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.childOption(ChannelOption.TCP_NODELAY, true);
			
			ChannelFuture future = bs.bind(port).sync();
			
			future.channel().closeFuture().sync();
		}catch(Exception e){
			//e.printStackTrace();
		}finally{
			worker.shutdownGracefully();
			boss.shutdownGracefully();
		}
		
	}

	private String readString(ByteBuf buf){
		short length = buf.readShort();
		
		byte[] strBuf = new byte[length];
		
		buf.readBytes(strBuf);
		
		return new String(strBuf);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		String server = ctx.channel().attr(SERVER_KEY).get();
		
		if(server != null){
			ServerManager.getInstance().getClient().setMetadata(server, "tponline", "");
			ServerManager.getInstance().getClient().setMetadata(server, "tpip", "");
			ServerManager.getInstance().getClient().setMetadata(server, "tpport", "");
		}
		
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
		if (msg instanceof HAProxyMessage){
			HAProxyMessage proxy = (HAProxyMessage) msg;
			address = new InetSocketAddress(proxy.sourceAddress(), proxy.sourcePort());
		}
		ByteBuf buf = (ByteBuf) msg;
		
		int op = buf.readByte();
		
		if(op == ThirdPartyConnection.CODE_PACKET){
			
			int port = buf.readUnsignedShort();
			
			String code = readString(buf);
			
			ServerInformation target = ServerManager.getInstance().getClient().getServerByCode(code);
			
			if(target == null){
				ctx.channel().disconnect();
			}else{
				String server = target.getName();
				ctx.channel().attr(SERVER_KEY).set(server);
				ServerManager.getInstance().getLogger().info(target.getName()+" "+target.getThirdPartyTimeLeft());
				
				if(target.getThirdPartyTimeLeft() > 0){
					if(target.isBan()){
						UUID owner = target.getOwner();
						if(owner != null){
							ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(owner);
							if(pp != null)
								pp.sendMessage(new TextComponent(PREFIX+ChatColor.RED+"Your server has been banned."));
								pp.sendMessage(new TextComponent(PREFIX+ChatColor.RED+"Reason: "+target.getBanReason()));
						}
						ctx.channel().disconnect();
						return;
					}
					ServerInfo info = ProxyServer.getInstance().constructServerInfo(server, new InetSocketAddress(address.getAddress(), port), "", false);
					
					ProxyServer.getInstance().getServers().put(server, info);
					
					ServerManager.getInstance().getLogger().info("Added third party server with code "+code+" name "+server+" address "+info.getAddress());
				
					
					ServerManager.getInstance().getClient().setMetadata(target.getName(), "tponline", "y");
					ServerManager.getInstance().getClient().setMetadata(target.getName(), "tpip", address.getAddress().getHostAddress());
					ServerManager.getInstance().getClient().setMetadata(target.getName(), "tpport", String.valueOf(port));
					
					UUID owner = target.getOwner();
					
					if(owner != null){
						ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(owner);
						
						if(pp != null)
							pp.sendMessage(new TextComponent(PREFIX+ChatColor.GREEN+"Third Party Server Connected!"));
					}
					
				}else{
					UUID owner = target.getOwner();
					
					if(owner != null){
						ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(owner);
						
						if(pp != null)
							pp.sendMessage(new TextComponent(PREFIX+ChatColor.RED+"Third Party not purchased."));
							pp.sendMessage(new TextComponent(PREFIX+ChatColor.RED+"Store: "+ChatColor.WHITE+"http://store.playerislands.com/category/899940"));
					}
					ctx.channel().disconnect();
				}

			}
		}else if(op == ThirdPartyConnection.MOTD_PACKET){

			String server = ctx.channel().attr(SERVER_KEY).get();
			
			if(server != null){
				String motd = readString(buf);
				
				ServerManager.getInstance().getClient().setMetadata(server, "motd", motd);
				ServerManager.getInstance().getLogger().info("Third party server "+server+" motd set to "+motd);
			}
			

			
		}else if(op == ThirdPartyConnection.PLAYER_PACKET){
			
			String server = ctx.channel().attr(SERVER_KEY).get();
			
			int players = buf.readInt();
			int max = buf.readInt();
			
			ServerManager.getInstance().getClient().setPlayers(server, players, max);
			
			ServerInfo info = ProxyServer.getInstance().getServerInfo(server);
			
			if(info != null){
				ServerManager.getInstance().getClient().setPlayers(server, info.getPlayers().size(), max);
			}
			
			
			
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}
	
}
