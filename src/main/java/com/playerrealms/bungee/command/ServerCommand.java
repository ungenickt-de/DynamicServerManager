package com.playerrealms.bungee.command;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.playerrealms.bungee.DropletAPI;
import com.playerrealms.bungee.other.Language;
import com.playerrealms.bungee.other.MojangAPI;
import com.playerrealms.common.ServerInformation;
import com.playerrealms.common.ServerStatus;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ServerCommand extends Command implements TabExecutor {
    public ServerCommand() {super("server","", "s");}

    @Override
    public void execute(CommandSender sender, String[] args){
        if(!(sender instanceof ProxiedPlayer)){
            return;
        }
        if(args.length == 0){
            List<ServerInformation> servers = DropletAPI.getPlayerServers();
            Collections.sort(servers);

            servers.removeIf(server -> server.getStatus() != ServerStatus.ONLINE);
            servers.removeIf(server -> server.isBan());

            if(servers.size() == 0){
                Language.sendMessage((ProxiedPlayer) sender, "server_command_no_servers");
                return;
            }

            BaseComponent[] components = new BaseComponent[servers.size() + 1];
            components[0] = new TextComponent("Servers ("+servers.size()+") : ");
            components[0].setColor(ChatColor.YELLOW);
            int i = 0;
            for(ServerInformation info : servers){
                StringBuilder str = new StringBuilder();
                if(info.getStatus() == ServerStatus.ONLINE){
                    str.append(ChatColor.GREEN);
                }else if(info.getStatus() == ServerStatus.STARTING){
                    str.append(ChatColor.YELLOW);
                }else if(info.getStatus() == ServerStatus.STOPPING){
                    str.append(ChatColor.RED);
                }else{
                    str.append(ChatColor.DARK_RED);
                }
                if(info.isUltraPremium()) {
                    str.append(ChatColor.GOLD);
                    str.append(ChatColor.BOLD);
                }
                if (info.isThirdParty()) {
                    str.append(ChatColor.RESET);
                    str.append(ChatColor.YELLOW);
                }
                if (info.isWhitelistEnabled()) {
                    str.append(ChatColor.RESET);
                    str.append(ChatColor.WHITE);
                }
                str.append(info.getName());
                if(info.getStatus() == ServerStatus.ONLINE){
                    str.append(ChatColor.BLUE+"("+ChatColor.BOLD+info.getPlayersOnline()+ChatColor.BLUE+")");
                }
                str.append(" ");
                components[++i] = new TextComponent(str.toString());
                components[i].setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/server "+info.getName()));

                ComponentBuilder builder = new ComponentBuilder(info.getName()).color(ChatColor.GREEN).append("\n");
                builder.append("\n");
                builder.append(Language.getText((ProxiedPlayer) sender, "menu_items_page_entry_online", info.getPlayersOnline(), info.getMaxPlayers()));
                builder.append("\n");
                builder.append(Language.getText((ProxiedPlayer) sender, "menu_items_page_entry_votes", info.getVotes()));
                builder.append("\n");
                builder.append(Language.getText((ProxiedPlayer) sender, "menu_items_page_entry_owner", MojangAPI.getUsername(info.getOwner())));

                BaseComponent[] desc = builder.create();

                components[i].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc));
            }
            sender.sendMessage(components);
            return;
        }
        String server = args[0];

        ServerInformation info = DropletAPI.getServerInfo(server);

        if(info == null){
            Language.sendMessage((ProxiedPlayer) sender, "response_codes_server_unknown", server);
        }else if(info.getStatus() == ServerStatus.OFFLINE) {
            Language.sendMessage((ProxiedPlayer) sender, "server_command_offline", server);
        }else if(info.isBan()){
            Language.sendMessage((ProxiedPlayer) sender, "response_codes_server_banned", server);
        }else{
            DropletAPI.connectToServer((ProxiedPlayer) sender, info);
        }
    }

    @Override
    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args)
    {
        return ( args.length > 1 ) ? Collections.EMPTY_LIST : Iterables.transform( Iterables.filter( ProxyServer.getInstance().getServers().values(), new Predicate<ServerInfo>()
        {
            private final String lower = ( args.length == 0 ) ? "" : args[0].toLowerCase( Locale.ROOT );

            @Override
            public boolean apply(ServerInfo input)
            {
                return input.getName().toLowerCase( Locale.ROOT ).startsWith( lower ) && input.canAccess( sender );
            }
        } ), input -> input.getName());
    }
}
