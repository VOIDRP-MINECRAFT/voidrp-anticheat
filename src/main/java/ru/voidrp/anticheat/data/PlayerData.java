package ru.voidrp.anticheat.data;

import net.minecraft.world.phys.Vec3;
import ru.voidrp.anticheat.network.InjectionReportPayload;

import java.util.*;

public class PlayerData {

    // ── Movement state ──────────────────────────────────────────────────────
    public Vec3 lastPosition = null;
    public boolean wasOnGround = true;
    public int airTicks = 0;
    public double fallStartY = 0;

    // ── Combat state ────────────────────────────────────────────────────────
    // victim UUID → last hit timestamp (ms)
    public final Map<UUID, Long> recentVictims = new LinkedHashMap<>() {
        @Override protected boolean removeEldestEntry(Map.Entry<UUID, Long> e) {
            return size() > 20;
        }
    };
    // timestamps of recent attacks (ms)
    public final Deque<Long> attackTimes = new ArrayDeque<>();

    // ── Violation tracking ──────────────────────────────────────────────────
    // check name → current VL
    public final Map<String, Integer> violationLevel = new HashMap<>();
    // check name → last time we reported this check (ms)
    public final Map<String, Long> lastReportTime = new HashMap<>();

    // ── Mod list (received from client) ────────────────────────────────────
    public List<String> clientMods = null;   // null = no response yet
    public long connectedAt = System.currentTimeMillis();
    public boolean modSnapshotSent = false;

    // ── Injection detection (received from client) ──────────────────────────
    public InjectionReportPayload injectionReport = null;
    public boolean injectionReportSent = false;

    // ── Resource pack ───────────────────────────────────────────────────────
    public String resourcePackStatus = "NONE"; // NONE / ACCEPTED / DECLINED / FAILED

    // ── Helpers ────────────────────────────────────────────────────────────

    public int addVl(ViolationType type) {
        int current = violationLevel.getOrDefault(type.name(), 0);
        int updated = current + type.vlPerTrigger;
        violationLevel.put(type.name(), updated);
        return updated;
    }

    public void resetVl(ViolationType type) {
        violationLevel.put(type.name(), 0);
    }

    /** Decay all VL by 1 every 5 seconds (called on tick, ~20 ticks/sec → every 100 ticks) */
    public void decayVl() {
        violationLevel.replaceAll((k, v) -> Math.max(0, v - 1));
    }

    public int getVl(ViolationType type) {
        return violationLevel.getOrDefault(type.name(), 0);
    }

    /** Returns current CPS based on attacks in last 1000 ms */
    public int currentCps() {
        long now = System.currentTimeMillis();
        while (!attackTimes.isEmpty() && now - attackTimes.peekFirst() > 1000) {
            attackTimes.pollFirst();
        }
        return attackTimes.size();
    }
}
