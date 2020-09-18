package com.playerrealms.bungee.redis;

import com.playerrealms.bungee.ServerManager;
import com.playerrealms.client.redis.JedisListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class LinkListener implements JedisListener {
    @Override
    public String[] getChannel() {
        return new String[] { "link" };
    }

    @Override
    public void onMessage(String channel, String message) {
        String[] args = message.split(" ");
        String status = args[0];
        UUID uuid = UUID.fromString(args[1]);
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        if(player == null){
            return;
        }

        if(status.equals("link")){
            player.sendMessage(new TextComponent(ServerManager.PREFIX + ChatColor.GREEN + "Your account has been linked."));
        }else if(status.equals("unlink")){
            player.sendMessage(new TextComponent(ServerManager.PREFIX + ChatColor.RED + "Your account has been unlinked."));
        }
    }
}
