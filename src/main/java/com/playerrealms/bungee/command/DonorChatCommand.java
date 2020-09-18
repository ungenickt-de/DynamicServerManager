package com.playerrealms.bungee.command;

import com.playerrealms.bungee.DropletAPI;
import com.playerrealms.bungee.ServerManager;
import com.playerrealms.bungee.redis.JedisAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class DonorChatCommand extends Command {
    public DonorChatCommand() {super("kifuchat", "", "kfc","kc");}

    @Override
    public void execute(CommandSender sender, String[] args){
        if(!(sender instanceof ProxiedPlayer)){
            return;
        }
        ProxiedPlayer send = (ProxiedPlayer) sender;
        if(!ServerManager.isDonor(send)){
            send.sendMessage(new TextComponent(ChatColor.RED+"You must own a donor rank. https://store.playerislands.com/category/rank"));
            return;
        }
        if(args.length < 1) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "/kifuchat [msg]"));
            return;
        }
        String msg = "";
        for(int i = 0; i < args.length;i++) {
            msg += args[i] + " ";
        }
        msg = msg.substring(0, msg.length() - 1);
        if(msg.equals("off")){
            JedisAPI.setKey("donatorchat.toggle."+send.getUniqueId(), "off");
            send.sendMessage(new TextComponent(ChatColor.RED+"Disabled donor chat. You can enable again with /kifuchat on"));
            return;
        }else if(msg.equals("on")){
            JedisAPI.removeKey("donatorchat.toggle."+send.getUniqueId());
            send.sendMessage(new TextComponent(ChatColor.GREEN+"Enabled donor chat. You can disable again with /kifuchat off"));
            return;
        }
        final String finalmsg = msg;
        ServerManager.getInstance().getProxy().getScheduler().runAsync(ServerManager.getInstance(), () -> {
            String msg1 = finalmsg;
            String oldmsg = finalmsg;
            msg1 = ChatColor.translateAlternateColorCodes('&', msg1);
            if(msg1 == null){
                msg1 = oldmsg;
            }
            boolean pf = true;
            if(JedisAPI.keyExists("hiderank.toggle."+send.getUniqueId())){
                pf = false;
                JedisAPI.cacheKey("hiderank.toggle."+send.getUniqueId(), 60000);
            }
            if(ServerManager.isPluginLoaded("RedisBungee")) {
                if (pf) {
                    JedisAPI.publish("donorchat", send.getServer().getInfo().getName() + " " + ServerManager.getPrefixById(DropletAPI.getRank(send)) + " " + send.getDisplayName() + ChatColor.GRAY + " " + msg1);
                } else {
                    JedisAPI.publish("donorchat", send.getServer().getInfo().getName() + " " + send.getDisplayName() + ChatColor.GRAY + " " + msg1);
                }
            }else{
                String player;
                if(pf) {
                    player = ServerManager.getPrefixById(DropletAPI.getRank(send)) + " " + send.getDisplayName();
                } else {
                    player = send.getDisplayName();
                }
                TextComponent tc = new TextComponent(ChatColor.BLUE+"["+send.getServer().getInfo().getName()+"] "+ChatColor.GREEN+ChatColor.translateAlternateColorCodes('&', player) + ChatColor.GRAY +" "+ msg1);
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/s "+send.getServer().getInfo().getName()));
                for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()) {
                    boolean display = true;
                    if(JedisAPI.keyExists("donatorchat.toggle."+pl.getUniqueId())){
                        display = false;
                        JedisAPI.cacheKey("donatorchat.toggle."+pl.getUniqueId(), 60000);
                    }
                    if(display && ServerManager.isDonor(pl)){
                        pl.sendMessage(tc);
                    }
                }
            }
        });
    }
}
