package com.playerrealms.bungee.other;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Language {

    private static final Map<UUID, String> playerLanguages = new HashMap<>();

    private static final Map<String, Language> registeredLanguage = new HashMap<>();

    private static final String DEFAULT_LANGUAGE = "ja_jp";

    private Map<String, String> text;

    private String languageName;

    private Language(Configuration config, String languageName) {

        this.languageName = languageName;

        text = new HashMap<>();
        for(String key : config.getKeys()){
            Object value = config.get(key);

            if(value instanceof String){
                text.put(key, ChatColor.translateAlternateColorCodes('&', value.toString()));
            }
        }

        this.languageName = languageName;

    }

    public static Language getLanguage(String name){
        return registeredLanguage.get(name);
    }

    public static void registerLanguage(InputStream reader, String name){
        Language language = new Language(ConfigurationProvider.getProvider(YamlConfiguration.class).load(reader), name);

        if(registeredLanguage.containsKey(language.getLanguageName())){
            registeredLanguage.get(language.getLanguageName()).text.putAll(language.text);
        }else{
            registeredLanguage.put(language.getLanguageName(), language);
        }

    }

    public static void setLanguage(ProxiedPlayer player, String lang){
        playerLanguages.put(player.getUniqueId(), lang);
    }

    public static void sendMessage(ProxiedPlayer player, String key, Object... values){
        getLanguage(player.getUniqueId()).send(player, key, values);
    }

    public static String getText(ProxiedPlayer player, String key, Object... values){
        return getLanguage(player.getUniqueId()).getText(key, values);
    }

    public static boolean hasKey(ProxiedPlayer player, String key){
        return getLanguage(player.getUniqueId()).text.containsKey(key);
    }

    public static Language getLanguage(UUID uuid){
        String lang = playerLanguages.get(uuid);

        if(!registeredLanguage.containsKey(lang)){
            return getDefault();
        }

        return registeredLanguage.get(lang);
    }

    public static String getLocale(ProxiedPlayer player){
        Language lang = getLanguage(player.getUniqueId());

        return lang.getLanguageName();
    }

    private static Language getDefault(){
        return registeredLanguage.get(DEFAULT_LANGUAGE);
    }

    public String getLanguageName() {
        return languageName;
    }

    public Collection<String> getKeys(){
        return text.keySet();
    }

    public void send(ProxiedPlayer player, String key, Object... values){
        player.sendMessage(new TextComponent(getText(key, values)));
    }

    public boolean hasKey(String key){
        return text.containsKey(key);
    }

    public String getText(String key, Object... values){
        if(!text.containsKey(key)){
            if(this != getDefault()){
                return getDefault().getText(key, values);
            }
            return key;
        }

        String line = text.get(key);

        for(int i = 0; i < values.length;i++){
            line = line.replaceAll("%"+i, values[i].toString());
        }

        return line;
    }

}
