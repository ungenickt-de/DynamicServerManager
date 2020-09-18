package com.playerrealms.bungee.command;

import com.google.common.collect.Iterables;
import com.playerrealms.bungee.ping.SimplePingResponseGenerator;
import com.playerrealms.bungee.task.ShutdownTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.lang.reflect.Field;

public class ProxyManageCommand extends Command implements TabExecutor {
    public ProxyManageCommand() { super("proxy");}

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("playerrealms.admin")){
            String arg = "";
            for(int i = 0; i < args.length;i++) {
                arg += args[i] + " ";
            }
            ((ProxiedPlayer) sender).chat("/proxy " + arg);
            return;
        }
        if(args.length < 1){
            sender.sendMessage(new TextComponent(ChatColor.RED+"/proxy <shutdown/reboot/cancel/setmaxplayers/getping>"));
            return;
        }

        if(args[0].equals("shutdown")){
            String arg = null;
            if(args[1] != null){
                arg = args[1];
            }
            int time = 120;
            if(arg != null && isNumeric(arg)){
                time = Integer.parseInt(arg);
            }
            ShutdownTask.runCountdown(time, true);
            return;
        }else if(args[0].equals("reboot")){
            String arg = null;
            if(args[1] != null){
                arg = args[1];
            }
            int time = 120;
            if(arg != null && isNumeric(arg)){
                time = Integer.parseInt(arg);
            }
            ShutdownTask.runCountdown(time, false);
            return;
        }else if(args[0].equals("cancel")){
            ShutdownTask.stopCountdown();
            sender.sendMessage(new TextComponent(ChatColor.GREEN+"Task has been canceled."));
        }else if(args[0].equals("setmaxplayers")){
            if(args.length == 1){
                sender.sendMessage(new TextComponent(ChatColor.RED+"/proxy setmaxplayers <number>"));
                return;
            }
            if(!isNumeric(args[1])){
                sender.sendMessage(new TextComponent(ChatColor.RED+"/proxy setmaxplayers <number>"));
                return;
            }
            SimplePingResponseGenerator.maxplayer = Integer.parseInt(args[1]);
            try {
                changeLimits(Integer.parseInt(args[1]));
            } catch (ReflectiveOperationException e) {
                sender.sendMessage(new TextComponent(ChatColor.RED+"An internal server error occurred."));
            }
            sender.sendMessage(new TextComponent(ChatColor.GREEN+"Max player has been changed."));
        }else if(args[0].equals("getping")){
            if(args.length == 1){
                ProxiedPlayer player = (ProxiedPlayer) sender;
                player.sendMessage(new TextComponent(ChatColor.GREEN+player.getDisplayName()+"'s Ping: "+player.getPing()));
                return;
            }
            ProxiedPlayer check = ProxyServer.getInstance().getPlayer(args[1]);
            if(check == null){
                sender.sendMessage(new TextComponent(ChatColor.RED+"Player is not online."));
                return;
            }
            sender.sendMessage(new TextComponent(ChatColor.GREEN+check.getName()+"'s Ping: "+check.getPing()));
            return;
        }
    }

    private void changeLimits(int slots) throws ReflectiveOperationException{
        Class<?> configClass = ProxyServer.getInstance().getConfig().getClass();
        if(!configClass.getSuperclass().equals(Object.class)){
            configClass = configClass.getSuperclass();
        }

        Field playerLimit = configClass.getDeclaredField("playerLimit");
        playerLimit.setAccessible(true);
        playerLimit.set(ProxyServer.getInstance().getConfig(), slots);
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        final String lastArg = ( args.length > 0 ) ? args[args.length - 1].toLowerCase() : "";
        return Iterables.transform( Iterables.filter( ProxyServer.getInstance().getPlayers(), player -> player.getName().toLowerCase().startsWith( lastArg )), player -> player.getName());
    }
}
