package com.playerrealms.bungee.command;

import com.google.common.collect.Iterables;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.playerrealms.bungee.ServerManager;
import com.playerrealms.bungee.redis.JedisAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class GlobalTellCommand extends Command implements TabExecutor {
    public GlobalTellCommand() {super("gtell");}

    @Override
    public void execute(CommandSender sender, String[] args){
        if(!(sender instanceof ProxiedPlayer)){
            return;
        }
        if(args.length < 2){
            sender.sendMessage(new TextComponent(ChatColor.RED+"/gtell [player] [msg]"));
            return;
        }
        ProxiedPlayer to = ProxyServer.getInstance().getPlayer(args[0]);
        ProxiedPlayer send = (ProxiedPlayer) sender;
        String server = send.getServer().getInfo().getName();
        if(ServerManager.isPluginLoaded("RedisBungee")){
            try {
                RedisBungee.getApi().getUuidFromName(to.getDisplayName(), true);
            }catch (NullPointerException e){
                sender.sendMessage(new TextComponent(ChatColor.RED + "Unknown Player. (Is this player online?)"));
                return;
            }catch (Exception e){
                e.printStackTrace();
                sender.sendMessage(new TextComponent(ChatColor.RED + "Internal server error."));
                return;
            }
        }else {
            if (to == null) {
                sender.sendMessage(new TextComponent(ChatColor.RED + "Unknown Player. (Is this player online?)"));
                return;
            }
        }
        String msg = "";
        for(int i = 1; i < args.length;i++) {
            msg += args[i] + " ";
        }
        msg = msg.substring(0, msg.length() - 1);
        final String finalmsg = msg;
        ServerManager.getInstance().getProxy().getScheduler().runAsync(ServerManager.getInstance(), () -> {
            String msg1 = finalmsg;
            String oldmsg = finalmsg;
            msg1 = ChatColor.translateAlternateColorCodes('&', msg1);
            if(msg1 == null){
                msg1 = oldmsg;
            }
            TextComponent message = new TextComponent(ChatColor.GRAY+"["+send.getDisplayName()+" -> "+to.getDisplayName()+"] "+ msg1);
            //message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/server "+server));
            send.sendMessage(message);
            if(ServerManager.isPluginLoaded("RedisBungee")){
                JedisAPI.publish("globaltell", send.getDisplayName() + " " + to.getDisplayName() + " " + server + " " + msg1);
            }else {
                to.sendMessage(message);
            }
        });
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        final String lastArg = ( args.length > 0 ) ? args[args.length - 1].toLowerCase() : "";
        return Iterables.transform( Iterables.filter( ProxyServer.getInstance().getPlayers(), player -> player.getName().toLowerCase().startsWith( lastArg )), player -> player.getName());
    }
}
