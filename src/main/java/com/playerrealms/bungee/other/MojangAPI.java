package com.playerrealms.bungee.other;

import com.google.gson.JsonArray;
import com.playerrealms.bungee.redis.JedisAPI;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MojangAPI {

    private static Map<UUID, String> cache = new HashMap<>();

    private static final String MOJANG_REQUEST_NAMES = "https://api.mojang.com/user/profiles/<uuid>/names";
    private static final String MOJANG_REQUEST_UUID = "https://api.mojang.com/profiles/minecraft";

    private static final Pattern FIX_UUID_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    public static UUID getUUID(String name) throws NameLookupException {

        if(ProxyServer.getInstance() != null && ProxyServer.getInstance().getPlayer(name) != null) {
            return ProxyServer.getInstance().getPlayer(name).getUniqueId();
        }

        JsonArray array = new JsonArray();
        array.add(name);

        try {
            JSONArray output = requestPost(MOJANG_REQUEST_UUID, array.toString());

            if(output.length() == 0) {
                throw new NameNotFoundException();
            }

            String mojangUUID = output.getJSONObject(0).getString("id");

            String fixed = FIX_UUID_PATTERN.matcher(mojangUUID).replaceAll("$1-$2-$3-$4-$5");

            return UUID.fromString(fixed);
        } catch (IOException e) {
        }

        throw new NameLookupException();

    }

    public static String getUsername(UUID id){

        String name = null;

        ProxiedPlayer online = null;

        if(ProxyServer.getInstance() != null){
            online = ProxyServer.getInstance().getPlayer(id);
        }

        if(cache.containsKey(id)) {
            return cache.get(id);
        }

        if(online != null){
            name = online.getName();
        }else if(JedisAPI.isValid() && JedisAPI.keyExists("name."+id.toString())){
            name = JedisAPI.getValue("name."+id.toString());
        }else{
            try {
                JSONArray array = request(MOJANG_REQUEST_NAMES.replace("<uuid>", id.toString().replaceAll("-", "")));
                name = array.getJSONObject(0).getString("name");
            } catch (Exception e) { }
        }

        if(name == null){
            return "[Unknown]";
        }

        cache.put(id, name);
        if(JedisAPI.isValid())
            JedisAPI.setKeyExpire("name."+id.toString(), name, (int) TimeUnit.HOURS.toSeconds(24));

        return name;
    }

    private static JSONArray requestPost(String urlText, String data) throws IOException{
        URL url = new URL(urlText);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", String.valueOf(data.length()));

        connection.getOutputStream().write(data.getBytes("UTF-8"));

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){

            String line;

            String text = "";

            while((line = reader.readLine()) != null){
                text += line + "\n";
            }

            return new JSONArray(text);
        }catch(Exception e) {
            throw e;
        }

    }

    private static JSONArray request(String urlText) throws IOException{
        URL url = new URL(urlText);

        URLConnection connection = url.openConnection();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){

            String line;

            String text = "";

            while((line = reader.readLine()) != null){
                text += line + "\n";
            }

            return new JSONArray(text);
        }

    }

    public static class NameNotFoundException extends NameLookupException {

        /**
         *
         */
        private static final long serialVersionUID = -770599733798997525L;

    }

    public static class NameLookupException extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = -770599733798997525L;

    }

}
