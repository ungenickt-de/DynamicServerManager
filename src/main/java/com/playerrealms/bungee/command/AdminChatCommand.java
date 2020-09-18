package com.playerrealms.bungee.command;

import com.playerrealms.bungee.ServerManager;
import com.playerrealms.bungee.redis.JedisAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class AdminChatCommand extends Command {
    public AdminChatCommand() {
        super("ac");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("playerrealms.adminchat")){
            String arg = "";
            for(int i = 0; i < args.length;i++) {
                arg += args[i] + " ";
            }
            ((ProxiedPlayer) sender).chat("/ac " + arg);
            return;
        }
        String msg = "";
        for(int i = 0; i < args.length;i++) {
            msg += args[i] + " ";
        }
        msg = msg.substring(0, msg.length() - 1);
        ProxiedPlayer sende = (ProxiedPlayer) sender;
        String server = sende.getServer().getInfo().getName();
        if(ServerManager.isPluginLoaded("RedisBungee")){
            JedisAPI.publish("adminchat", server + " " + sende.getName() + " " + msg);
        }else {
            TextComponent tc = new TextComponent(ChatColor.DARK_RED + "[AC] " + ChatColor.BLUE + "[" + server + "] " + ChatColor.GREEN + sende.getName() + ChatColor.GRAY + " " + msg);
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/server " + server));
            //will add toggle adminchat
            for (ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()) {
                if (pl.hasPermission("playerrealms.adminchat")) {
                    pl.sendMessage(tc);
                }
            }
        }
    }
}
