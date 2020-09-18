package com.playerrealms.bungee.command;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.playerrealms.bungee.ServerManager;
import com.playerrealms.bungee.redis.JedisAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ReportCommand extends Command implements TabExecutor {
    private String webhook = "";

    public ReportCommand() {
        super("pireport", "", "pirp");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if(args.length < 2){
                player.sendMessage(new TextComponent(ChatColor.RED+"Usage: /pireport <Player> <Message>"));
                return;
            }
            long last = 0;
            if(JedisAPI.keyExists("lastreport."+player.getUniqueId())) {
                last = Long.parseLong(JedisAPI.getCachedValue("lastreport."+player.getUniqueId(), 60000));
            }
            if(System.currentTimeMillis() - last < TimeUnit.MINUTES.toMillis(10)){
                if(player.getLocale().toString().equals("ja_JP")){
                    player.sendMessage(new TextComponent(ChatColor.GREEN+"10分ごとに一回しか/pireport クールダウン: "+((last + TimeUnit.MINUTES.toMillis(10)) - System.currentTimeMillis()) / 1000+" 秒"));
                }else{
                    player.sendMessage(new TextComponent(ChatColor.GREEN+"/pireport is only once in 10 minutes. Cooldown: "+((last + TimeUnit.MINUTES.toMillis(10)) - System.currentTimeMillis()) / 1000+" seconds"));
                }
                return;
            }
            String msg = "";
            for(int i = 0; i < args.length;i++) {
                msg += args[i] + " ";
            }
            msg = msg.substring(0, msg.length() - 1);
            if(player.getLocale().toString().equals("ja_JP")){
                player.sendMessage(new TextComponent(ChatColor.GREEN+"スタッフにレポートを送信しました。"));
            }else{
                player.sendMessage(new TextComponent(ChatColor.GREEN+"Report has been sent to Staff."));
            }
            String server = player.getServer().getInfo().getName();
            if(ServerManager.isPluginLoaded("RedisBungee")) {
                JedisAPI.publish("report", server + " " + player.getName() + " " + msg);
            }else {
                TextComponent tc = new TextComponent(ChatColor.DARK_RED + "[Report] " + ChatColor.BLUE + "[" + server + "] " + ChatColor.GREEN + player.getName() + ": " + msg);
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/server " + server));
                for (ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()) {
                    if (pl.hasPermission("playerrealms.adminchat")) {
                        pl.sendMessage(tc);
                    }
                }
            }
            JedisAPI.setKey("lastreport."+player.getUniqueId(), String.valueOf(System.currentTimeMillis()));
            String content = "[REPORT] "+player.getName()+" ("+server+"):"+msg;
            ProxyServer.getInstance().getScheduler().runAsync(ServerManager.getInstance(), () -> {
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost request = new HttpPost(webhook);
                request.addHeader("Content-Type", "application/json");
                String jsonMessage = "{\"content\": \"" + content + "\"}";
                try{
                    StringEntity params = new StringEntity(jsonMessage, "UTF-8");
                    request.setEntity(params);
                    httpClient.execute(request);
                }catch (IOException e){
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args)
    {
        final String lastArg = ( args.length > 0 ) ? args[args.length - 1].toLowerCase() : "";
        return Iterables.transform( Iterables.filter( ProxyServer.getInstance().getPlayers(), new Predicate<ProxiedPlayer>()
        {
            @Override
            public boolean apply(ProxiedPlayer player)
            {
                return player.getName().toLowerCase().startsWith( lastArg );
            }
        } ), new Function<ProxiedPlayer, String>()
        {
            @Override
            public String apply(ProxiedPlayer player)
            {
                return player.getName();
            }
        } );
    }
}
