package com.playerrealms.bungee.command;

import com.playerrealms.bungee.ServerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class FallbackServerCommand extends Command {
    public FallbackServerCommand() {
        super("hub", "", "lobby");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)){
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        ServerInfo hub = ServerManager.findHub(null);
        if(hub != null) {
            player.sendMessage(new TextComponent(ChatColor.GREEN + "Connecting to Hub..."));
            player.connect(hub);
        }else {
            player.sendMessage(new TextComponent(ChatColor.RED + "No available lobby. Please try again later."));
        }
    }
}
