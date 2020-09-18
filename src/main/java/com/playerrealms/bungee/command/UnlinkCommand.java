package com.playerrealms.bungee.command;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.playerrealms.bungee.ServerManager;
import com.playerrealms.bungee.other.Link;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class UnlinkCommand extends Command implements TabExecutor {
    public UnlinkCommand() {
        super("unlink");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            if(!sender.hasPermission("playerrealms.admin")){
                String arg = "";
                for(int i = 0; i < args.length;i++) {
                    arg += args[i] + " ";
                }
                ((ProxiedPlayer) sender).chat("/unlink " + arg);
                return;
            }
            if(args.length != 1){
                sender.sendMessage(new TextComponent(ChatColor.RED + "/unlink <MCID / Offline UUID>"));
                return;
            }
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
            UUID uuid;
            if(player != null){
                uuid = ProxyServer.getInstance().getPlayer(args[0]).getUniqueId();
            }else {
                try {
                    uuid = UUID.fromString(args[0]);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "Invalid Minecraft ID or Invalid UUID"));
                    return;
                }
            }
            sender.sendMessage(new TextComponent(ChatColor.GREEN + "Please wait..."));
            ServerManager.getInstance().getProxy().getScheduler().runAsync(ServerManager.getInstance(), new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL("");
                        URLConnection con = url.openConnection();
                        con.setRequestProperty("User-Agent", "");
                        con.setRequestProperty("API-Key", "");
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        Gson gson = new Gson();
                        Link link = gson.fromJson(in.readLine(), Link.class);
                        if(link.result){
                            sender.sendMessage(new TextComponent(ChatColor.GREEN + "The account has been unlinked."));
                        }else{
                            sender.sendMessage(new TextComponent(ChatColor.RED + link.message));
                        }
                    } catch (IOException e) {
                        sender.sendMessage(new TextComponent(ChatColor.DARK_RED + "Internal Server Error. Please contact to staff."));
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        final String lastArg = ( args.length > 0 ) ? args[args.length - 1].toLowerCase() : "";
        return Iterables.transform( Iterables.filter( ProxyServer.getInstance().getPlayers(), player -> player.getName().toLowerCase().startsWith( lastArg )), player -> player.getName());
    }
}