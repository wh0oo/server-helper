package net.crypticverse.serverhelper.config.ranks;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.crypticverse.serverhelper.ServerHelper;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;

public class RanksConfig {
    public static final Map<String, Role> ranks = new HashMap<>();

    public static class Role {
        String name;
        List<UUID> players = new ArrayList<>();

        Role(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public List<UUID> getPlayers() {
            return players;
        }
    }

    public static boolean isPlayerInRole(UUID uuid, String roleName) {
        Role role = ranks.get(roleName);
        if (role == null) {
            return false;
        }
        return role.players.contains(uuid);
    }

    public static boolean isRank(String role) {
        return ranks.containsKey(role);
    }

    public static void createRole(String roleId, String displayName) {
        if (!ranks.containsKey(roleId)) {
            ranks.put(roleId, new Role(displayName));
        } else {
            System.out.println("Role " + roleId + " already exists");
        }
    }

    public static void removeRole(String roleId) {
        ranks.remove(roleId);
    }

    public static void addPlayerToRole(String roleId, UUID playerUuid) {
        Role role = ranks.get(roleId);
        if (role != null) {
            role.players.add(playerUuid);
        } else {
            System.err.println("Role " + roleId + " does not exist! Player not added.");
        }
    }

    public static void removePlayerFromRole(String roleId, UUID playerUuid) {
        Role role = ranks.get(roleId);
        if (role != null) {
            role.players.remove(playerUuid);
        } else {
            System.err.println("Role " + roleId + " does not exist! Player not added.");
        }
    }

    public static Component getPlayersInRole(UUID uuid) {
        for (Map.Entry<String, Role> entry : ranks.entrySet()) {
            if (entry.getValue().players.contains(uuid)) {
                return Component.literal(entry.getValue().getName());
            }
        }

        return Component.literal("");
    }

    public static void loadConfig() {
        File file = ServerHelper.ranksConfigFile.toFile();
        if (!file.exists()) {
            save();
            return;
        }
        try (FileReader fileReader = new FileReader(file)) {
            Gson gson = new GsonBuilder().create();
            Type configType = new TypeToken<RanksConfigTemplate>() {}.getType();
            RanksConfigTemplate config = gson.fromJson(fileReader, configType);

            ranks.putAll(config.ranks);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            JsonObject ranksObject = new JsonObject();

            for (Map.Entry<String, Role> entry : ranks.entrySet()) {
                String roleId = entry.getKey();
                Role role = entry.getValue();

                JsonObject roleJson = new JsonObject();
                roleJson.addProperty("name", role.name);

                JsonArray playersArray = new JsonArray();
                for (UUID player : role.players) {
                    playersArray.add(player.toString());
                }
                roleJson.add("players", playersArray);

                ranksObject.add(roleId, roleJson);
            }
            root.add("ranks", ranksObject);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(root);
            Files.write(ServerHelper.ranksConfigFile, json.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
