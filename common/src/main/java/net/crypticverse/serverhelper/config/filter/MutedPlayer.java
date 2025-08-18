package net.crypticverse.serverhelper.config.filter;

import java.util.UUID;

public record MutedPlayer(UUID uuid, String reason) {
    @Override
    public String toString() {
        return "{uuid: " + uuid + " reason: " + reason + "}";
    }
}
