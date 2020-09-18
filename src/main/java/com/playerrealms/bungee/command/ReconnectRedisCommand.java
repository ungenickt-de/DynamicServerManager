package com.playerrealms.bungee.command;

import com.playerrealms.bungee.redis.JedisAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class ReconnectRedisCommand extends Command {

    public ReconnectRedisCommand() {
        super("reconredis");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!sender.hasPermission("playerrealms.admin")){
            sender.sendMessage(new TextComponent(ChatColor.RED+"No permission"));
            return;
        }
        Configuration config;
        try {
            config = downloadConfig();
        } catch (IOException e2) {
            e2.printStackTrace();
            return;
        }
        JedisAPI.destroy();
        JedisAPI.setup(config.getString("redis_remote"), 6379, config.getString("redis_password"));
        sender.sendMessage(new TextComponent("Redis Client has been reconnected."));
    }

    public static Configuration downloadConfig() throws IOException {
        URL url = new URL("");

        URLConnection con = url.openConnection();
        con.setRequestProperty("User-Agent", "");

        con.setRequestProperty("API-Key", "");

        Configuration config;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))){
            config = YamlConfiguration.getProvider(YamlConfiguration.class).load(reader);
        }

        return config;
    }
}
