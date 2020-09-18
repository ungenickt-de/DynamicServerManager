package com.playerrealms.bungee.command;

import com.playerrealms.bungee.ServerManager;
import com.playerrealms.bungee.sql.DatabaseAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;
import java.util.UUID;

public class PlayUnBanCommand extends Command {
    public PlayUnBanCommand() {super("punban");}

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("playerrealms.helper")){
            if(sender instanceof ProxiedPlayer){
                String cmd = "/punban ";
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

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
        if(player == null){
            sender.sendMessage(new TextComponent(ChatColor.RED+"Could not find player named "+playerName+" (Is this player online?)"));
            return;
        }
        UUID uuid = player.getUniqueId();
        try {
            DatabaseAPI.execute("UPDATE `players` SET `ban_expire_time`=0, `ban_reason`=NULL, `ban_moderator`=NULL, `ban_type`=0 WHERE `uuid`=?", uuid.toString());
            sender.sendMessage(new TextComponent(ServerManager.PREFIX + ChatColor.RED+"Unbanned "+playerName+" ("+uuid+")"));
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

