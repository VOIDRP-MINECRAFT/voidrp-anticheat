package ru.voidrp.anticheat.check;

import net.minecraft.server.level.ServerPlayer;
import ru.voidrp.anticheat.VoidRpAnticheat;
import ru.voidrp.anticheat.backend.BackendClient;
import ru.voidrp.anticheat.config.AnticheatConfig;
import ru.voidrp.anticheat.config.RemoteConfigManager;
import ru.voidrp.anticheat.data.PlayerData;
import ru.voidrp.anticheat.data.ViolationType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CheckManager {

    private static final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    private static BackendClient backendClient;

    public static void init(BackendClient client) {
        backendClient = client;
    }

    public static PlayerData getOrCreate(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData());
    }

    public static void remove(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public static void onDecayTick(long tickCount) {
        if (tickCount % 100 != 0) return; // every 5 seconds
        for (PlayerData data : playerDataMap.values()) {
            data.decayVl();
        }
    }

    /**
     * Called after a check triggers a violation. Adds VL and reports to backend
     * if VL threshold is met.
     */
    public static void flag(ServerPlayer player, PlayerData data, ViolationType type,
                             String details, double actualValue, double expectedMax) {
        if (!AnticheatConfig.ENABLED.get()) return;

        int vl = data.addVl(type);
        int threshold = (int) RemoteConfigManager.get("vl_threshold", (double) AnticheatConfig.VL_THRESHOLD.get());

        if (vl >= threshold) {
            long now = System.currentTimeMillis();
            long lastReport = data.lastReportTime.getOrDefault(type.name(), 0L);
            if (now - lastReport < 10_000) return; // don't spam — max once per 10s per check

            data.lastReportTime.put(type.name(), now);
            data.resetVl(type);

            String severity = vl >= threshold * 3 ? "HIGH" : vl >= threshold * 2 ? "MEDIUM" : type.defaultSeverity;

            VoidRpAnticheat.LOG.warn("[AntiCheat] {} | {} | actual={} max={} vl={} | {}",
                    player.getName().getString(), type.displayName, actualValue, expectedMax, vl, details);

            if (backendClient != null) {
                backendClient.reportViolation(
                        player.getStringUUID(),
                        player.getName().getString(),
                        type.name(),
                        details,
                        actualValue,
                        expectedMax,
                        vl,
                        severity
                );
            }
        }
    }
}
