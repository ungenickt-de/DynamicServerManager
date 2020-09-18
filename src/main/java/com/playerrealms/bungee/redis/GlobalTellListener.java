package com.playerrealms.bungee.redis;

import com.playerrealms.client.redis.JedisListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GlobalTellListener implements JedisListener {
    @Override
    public String[] getChannel() {
        return new String[] { "globaltell" };
    }

    public void onMessage(String channel, String message){
        String[] args = message.split(" ");

        String send = args[0];

        String to = args[1];

        String server = args[2];

        String msg = "";

        for(int i = 3; i < args.length;i++) {
            msg += args[i] + " ";
        }

        TextComponent tc = new TextComponent(ChatColor.GRAY+"["+send+" -> "+to+"] "+msg);
        //tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/server "+server));
        for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()) {
            if(pl.getDisplayName().equals(to)) {
                pl.sendMessage(tc);
            }
        }
    }
}
