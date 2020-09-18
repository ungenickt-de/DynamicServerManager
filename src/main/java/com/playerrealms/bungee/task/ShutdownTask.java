package com.playerrealms.bungee.task;

import com.playerrealms.bungee.ServerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.concurrent.TimeUnit;

public class ShutdownTask {
    private static TaskScheduler task = ProxyServer.getInstance().getScheduler();
    private static int timer;
    private static boolean shutdown;
    private static String PREFIX = ChatColor.WHITE+"["+ChatColor.AQUA+"Player"+ChatColor.LIGHT_PURPLE+"Islands"+ChatColor.WHITE+"] ";

    public static void runCountdown(int count, boolean mode){
        timer = count;
        shutdown = mode;
        task.schedule(ServerManager.getInstance(), new Runnable() {
            @Override
            public void run() {
                if(timer > 0) {
                    sendMessage(timer, shutdown);
                }
                if(timer <= 0){
                    task.cancel(ServerManager.getInstance());
                    ProxyServer.getInstance().stop();
                }
                --timer;
            }
        },1, 1, TimeUnit.SECONDS);
    }

    private static void sendMessage(int count, boolean shutdown){
        String jtype = "再起動";
        String etype = "reboot";
        if(shutdown) {
            jtype = "停止";
            etype = "stop";
        }
        if(count == 600) {
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを10分後に" + jtype + "します。 / Proxy " + etype + " in 10 minutes."));
            for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()){
                if(pl != null){
                    BaseComponent[] msg = TextComponent.fromLegacyText((String)"Proxyを10分後に" + jtype + "します。 / Proxy " + etype + " in 10 minutes.");
                    pl.sendMessage(ChatMessageType.ACTION_BAR, msg);
                }
            }
        }else if(count == 300) {
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを5分後に" + jtype + "します。 / Proxy " + etype + " in 5 minutes."));
            for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()){
                if(pl != null){
                    BaseComponent[] msg = TextComponent.fromLegacyText((String)"Proxyを5分後に" + jtype + "します。 / Proxy " + etype + " in 5 minutes.");
                    pl.sendMessage(ChatMessageType.ACTION_BAR, msg);
                }
            }
        }else if(count == 240) {
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを4分後に" + jtype + "します。 / Proxy " + etype + " in 4 minutes."));
        } else if(count == 180) {
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを3分後に" + jtype + "します。 / Proxy " + etype + " in 3 minutes."));
        }else if(count == 120) {
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを2分後に" + jtype + "します。 / Proxy " + etype + " in 2 minutes."));
        }else if(count == 60){
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを1分後に" + jtype + "します。 / Proxy " + etype + " in 1 minute."));
            for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()){
                if(pl != null){
                    BaseComponent[] msg = TextComponent.fromLegacyText((String)"Proxyを1分後に" + jtype + "します。 / Proxy " + etype + " in 1 minute.");
                    pl.sendMessage(ChatMessageType.ACTION_BAR, msg);
                }
            }
        }else if(count == 30){
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを30秒後に" + jtype + "します。 / Proxy " + etype + " in 30 seconds."));
        }else if(count == 10){
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを10秒後に" + jtype + "します。 / Proxy " + etype + " in 10 seconds."));
        }else if(count == 5){
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを5秒後に" + jtype + "します。 / Proxy " + etype + " in 5 seconds."));
            for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()){
                if(pl != null){
                    BaseComponent[] msg = TextComponent.fromLegacyText((String)"Proxyを5秒後に" + jtype + "します。 / Proxy " + etype + " in 5 seconds.");
                    pl.sendMessage(ChatMessageType.ACTION_BAR, msg);
                }
            }
        }else if(count == 4){
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを4秒後に" + jtype + "します。 / Proxy " + etype + " in 4 seconds."));
            for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()){
                if(pl != null){
                    BaseComponent[] msg = TextComponent.fromLegacyText((String)"Proxyを4秒後に" + jtype + "します。 / Proxy " + etype + " in 4 seconds.");
                    pl.sendMessage(ChatMessageType.ACTION_BAR, msg);
                }
            }
        }else if(count == 3){
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを3秒後に" + jtype + "します。 / Proxy " + etype + " in 3 seconds."));
            for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()){
                if(pl != null){
                    BaseComponent[] msg = TextComponent.fromLegacyText((String)"Proxyを3秒後に" + jtype + "します。 / Proxy " + etype + " in 3 seconds.");
                    pl.sendMessage(ChatMessageType.ACTION_BAR, msg);
                }
            }
        }else if(count == 2){
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを2秒後に" + jtype + "します。 / Proxy " + etype + " in 2 seconds."));
            for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()){
                if(pl != null){
                    BaseComponent[] msg = TextComponent.fromLegacyText((String)"Proxyを2秒後に" + jtype + "します。 / Proxy " + etype + " in 2 seconds.");
                    pl.sendMessage(ChatMessageType.ACTION_BAR, msg);
                }
            }
        }else if(count == 1){
            ProxyServer.getInstance().broadcast(new TextComponent(PREFIX+ChatColor.RED + "Proxyを1秒後に" + jtype + "します。 / Proxy " + etype + " in 1 second."));
            for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()){
                if(pl != null){
                    BaseComponent[] msg = TextComponent.fromLegacyText((String)"Proxyを1秒後に" + jtype + "します。 / Proxy " + etype + " in 1 second.");
                    pl.sendMessage(ChatMessageType.ACTION_BAR, msg);
                }
            }
        }
    }

    public static void stopCountdown(){
        task.cancel(ServerManager.getInstance());
    }
}
