package com.playerrealms.bungee.command;

import com.playerrealms.bungee.ServerManager;
import com.playerrealms.bungee.redis.JedisAPI;
import com.playerrealms.bungee.sql.DatabaseAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayBanCommand extends Command {
    public PlayBanCommand() {super("pban");}

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("playerrealms.helper")){
            if(sender instanceof ProxiedPlayer){
                String cmd = "/pban ";
                for(int i = 0; i < args.length; i++){
                    cmd += args[i] + " ";
                }
                ((ProxiedPlayer) sender).chat(cmd);
            }
            return;
        }

        if(args.length < 3){
            return;
        }

        String playerName = args[0];
        if(!isValidNumber(args[1])){
            return;
        }
        int time = Integer.parseInt(args[1]);
        TimeUnit unit;
        String unitName = args[2];

        String reason = "Banned! (No reason supplied)";

        if(time < 0){
            sender.sendMessage(new TextComponent(ChatColor.RED+"Time must be zero or positive (zero will unban)"));
        }

        if(args.length > 3) {
            reason = "";
            for(int i = 3; i < args.length; i++){
                reason += args[i] + " ";
            }
        }

        if(reason.contains("%")) {
            sender.sendMessage(new TextComponent(ChatColor.RED+"Reason cannnot contain '%'"));
            return;
        }

        if(unitName.equalsIgnoreCase("sec")) {
            unit = TimeUnit.SECONDS;
        }else if(unitName.equalsIgnoreCase("min")) {
            unit = TimeUnit.MINUTES;
        }else if(unitName.equalsIgnoreCase("hour")) {
            unit = TimeUnit.HOURS;
        }else if(unitName.equalsIgnoreCase("day")) {
            unit = TimeUnit.DAYS;
        }else {
            sender.sendMessage(new TextComponent(ChatColor.RED+"Unknown unit "+unitName+". Valid units: sec,min,hour,day"));
            return;
        }

        long realTime = unit.toMillis(time) + System.currentTimeMillis();

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
        if(player == null){
            sender.sendMessage(new TextComponent(ChatColor.RED+"Could not find player named "+playerName+" (Is this player online?)"));
            return;
        }
        UUID uuid = player.getUniqueId();
        try {
            DatabaseAPI.execute("UPDATE `players` SET `ban_expire_time`=?, `ban_reason`=?, `ban_moderator`=?, `ban_type`=? WHERE `uuid`=?", realTime, reason, sender.getName(), 1, uuid.toString());
            sender.sendMessage(new TextComponent(ServerManager.PREFIX + ChatColor.RED+"Banned "+playerName+" ("+uuid+") for "+time+" "+unit.name().toLowerCase()));
            JedisAPI.publish("ban", uuid+" "+reason.replace(' ', '%')+" "+realTime+" "+sender.getName());
            //player.connect(ServerManager.findHub(null));
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(new TextComponent(ChatColor.DARK_RED+"Database Error"));
        }
    }

    private static boolean isValidNumber(String s) {
        try {
            Integer.parseInt(s);
        }catch(NumberFormatException e) {
            return false;
        }
        return true;
    }
}
