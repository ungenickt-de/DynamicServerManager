package com.playerrealms.bungee.command;

import com.google.gson.Gson;
import com.playerrealms.bungee.ServerManager;
import com.playerrealms.bungee.other.Link;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class LinkCommand extends Command {
    public LinkCommand() {
        super("link");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            sender.sendMessage(new TextComponent(ChatColor.GREEN + "Please wait..."));
            ServerManager.getInstance().getProxy().getScheduler().runAsync(ServerManager.getInstance(), () -> {
                try {
                    URL url = new URL("");
                    URLConnection con = url.openConnection();
                    con.setRequestProperty("User-Agent", "");
                    con.setRequestProperty("API-Key", "");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    Gson gson = new Gson();
                    Link link = gson.fromJson(in.readLine(), Link.class);
                    if(link.result){
                        TextComponent message = new TextComponent(ChatColor.GREEN + "Click here (Open Link)");
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ""+link.message));
                        sender.sendMessage(message);
                    }else{
                        sender.sendMessage(new TextComponent(ChatColor.RED + link.message));
                    }
                } catch (IOException e) {
                    sender.sendMessage(new TextComponent(ChatColor.DARK_RED + "Internal Server Error. Please contact to staff."));
                    e.printStackTrace();
                }
            });
        }
    }
}
