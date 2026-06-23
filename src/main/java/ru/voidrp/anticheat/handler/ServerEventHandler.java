package ru.voidrp.anticheat.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import ru.voidrp.anticheat.VoidRpAnticheat;
import ru.voidrp.anticheat.backend.BackendClient;
import ru.voidrp.anticheat.check.*;
import ru.voidrp.anticheat.config.AnticheatConfig;
import ru.voidrp.anticheat.data.PlayerData;

public class ServerEventHandler {

    private long serverTickCount = 0;
    private final BackendClient backendClient;

    public ServerEventHandler() {
        backendClient = new BackendClient();
        CheckManager.init(backendClient);
    }

    // ── Player tick — movement checks ──────────────────────────────────────
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!AnticheatConfig.ENABLED.get()) return;

        PlayerData data = CheckManager.getOrCreate(player.getUUID());

        // Detect server-side teleports (e.g. /home, /tp): if player moved > 10 blocks in one
        // tick that is physically impossible by movement — skip movement checks this tick.
        boolean teleported = false;
        if (data.lastPosition != null) {
            double dx = player.getX() - data.lastPosition.x;
            double dy = player.getY() - data.lastPosition.y;
            double dz = player.getZ() - data.lastPosition.z;
            if (Math.sqrt(dx * dx + dy * dy + dz * dz) > 10.0) {
                teleported = true;
                data.airTicks = 0;
            }
        }

        if (!teleported) {
            SpeedCheck.check(player, data);
            FlyCheck.check(player, data);
        }

        // Update state for next tick
        data.lastPosition = player.position();
        data.wasOnGround = player.onGround();
        if (player.onGround()) {
            data.airTicks = 0;
            data.fallStartY = player.getY();
        }

        long elapsed = System.currentTimeMillis() - data.connectedAt;

        // Send pending mod snapshot to backend (once per session, after 5s)
        if (!data.modSnapshotSent && data.clientMods != null && elapsed > 5000) {
            data.modSnapshotSent = true;
            backendClient.reportModSnapshot(
                    player.getStringUUID(),
                    player.getName().getString(),
                    data.clientMods
            );
        }

        // Send injection report to backend (once per session, after 5s)
        if (!data.injectionReportSent && data.injectionReport != null && elapsed > 5000) {
            data.injectionReportSent = true;
            var report = data.injectionReport;
            backendClient.reportInjection(
                    player.getStringUUID(),
                    player.getName().getString(),
                    report.javaAgents(),
                    report.suspiciousLibraries(),
                    report.agentsDetected()
            );
        }
    }

    // ── Server tick — decay, housekeeping, config polling ─────────────────
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        serverTickCount++;
        CheckManager.onDecayTick(serverTickCount);
        // Fetch remote config on startup and every 5 minutes (6000 ticks @ 20 TPS)
        if (serverTickCount == 20 || serverTickCount % 6000 == 0) {
            backendClient.fetchConfig();
        }
    }

    // ── Combat — reach / killaura / CPS ────────────────────────────────────
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onAttackEntity(AttackEntityEvent event) {
        if (!AnticheatConfig.ENABLED.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer attacker)) return;

        Entity victim = event.getTarget();
        PlayerData data = CheckManager.getOrCreate(attacker.getUUID());

        ReachCheck.check(attacker, victim, data);
        KillAuraCheck.check(attacker, victim, data);
        CpsCheck.check(attacker, data);
    }

    // ── Player join ────────────────────────────────────────────────────────
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerData data = CheckManager.getOrCreate(player.getUUID());
        data.connectedAt = System.currentTimeMillis();
        data.modSnapshotSent = false;
        data.clientMods = null;
        data.injectionReportSent = false;
        data.injectionReport = null;

        VoidRpAnticheat.LOG.info("[AntiCheat] Tracking player: {}", player.getName().getString());
    }

    // ── Player leave ───────────────────────────────────────────────────────
    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CheckManager.remove(player.getUUID());
    }
}
