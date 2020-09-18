package com.playerrealms.bungee.redis;

import com.playerrealms.client.redis.JedisListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class AdminChatListener implements JedisListener {
    @Override
    public String[] getChannel() {
        return new String[] { "adminchat" };
    }

    public void onMessage(String channel, String message){
        String[] args = message.split(" ");

        String server = args[0];

        String player = args[1];

        String msg = "";

        for(int i = 2; i < args.length;i++) {
            msg += args[i] + " ";
        }

        TextComponent tc = new TextComponent(ChatColor.DARK_RED+"[AC] "+ChatColor.BLUE+"["+server+"] "+ChatColor.GREEN+player+ChatColor.GRAY+" "+msg);
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/server "+server));
        for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()) {
            if(pl.hasPermission("playerrealms.adminchat")) {
                pl.sendMessage(tc);
            }
        }
    }
}
