package com.playerrealms.bungee.redis;

import com.playerrealms.bungee.ServerManager;
import com.playerrealms.client.redis.JedisListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class DonorChatListener implements JedisListener {

    @Override
    public String[] getChannel() {
        return new String[] { "donorchat" };
    }

    @Override
    public void onMessage(String channel, String message) {
        String[] args = message.split(" ");
        String server = args[0];
        String player = args[1];
        String msg = "";

        for(int i = 2; i < args.length;i++) {
            msg += args[i] + " ";
        }

        TextComponent tc = new TextComponent(ChatColor.BLUE+"["+server+"] "+ChatColor.GREEN+ChatColor.translateAlternateColorCodes('&',player)+" "+msg);
        //tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/s "+server));
        for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()) {
            boolean display = true;
            if(JedisAPI.keyExists("donatorchat.toggle."+pl.getUniqueId())){
                display = false;
                JedisAPI.cacheKey("donatorchat.toggle."+pl.getUniqueId(), 60000);
            }
            if(display && ServerManager.isDonor(pl)) { //should be fixed.
                pl.sendMessage(tc);
            }
        }
    }
}