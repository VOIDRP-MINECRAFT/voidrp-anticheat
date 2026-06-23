package ru.voidrp.anticheat.check;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import ru.voidrp.anticheat.config.AnticheatConfig;
import ru.voidrp.anticheat.config.RemoteConfigManager;
import ru.voidrp.anticheat.data.PlayerData;
import ru.voidrp.anticheat.data.ViolationType;

public class KillAuraCheck {

    public static void check(ServerPlayer attacker, Entity victim, PlayerData data) {
        if (attacker.isCreative() || attacker.isSpectator()) return;

        long now = System.currentTimeMillis();

        // ── Multi-target check ──────────────────────────────────────────────
        // Remove victims older than 1 second
        data.recentVictims.entrySet().removeIf(e -> now - e.getValue() > 1000);
        data.recentVictims.put(victim.getUUID(), now);

        int targets = data.recentVictims.size();
        int maxTargets = (int) RemoteConfigManager.get("killaura_targets_per_second", (double) AnticheatConfig.KILLAURA_TARGETS_PER_SECOND.get());

        if (targets > maxTargets) {
            String details = String.format("targets=%d/s pos=(%.1f,%.1f,%.1f)",
                    targets, attacker.getX(), attacker.getY(), attacker.getZ());
            CheckManager.flag(attacker, data, ViolationType.KILLAURA, details, targets, maxTargets);
            return;
        }

        // ── Angle check — target behind attacker ───────────────────────────
        Vec3 look = attacker.getLookAngle();
        Vec3 toVictim = victim.position().subtract(attacker.position()).normalize();
        double dot = look.dot(toVictim);
        // dot < -0.3 means target is more than ~107° behind attacker (stricter filter)
        if (dot < -0.3) {
            String details = String.format("dot=%.2f victim=%s", dot, victim.getName().getString());
            CheckManager.flag(attacker, data, ViolationType.KILLAURA, details, dot, -0.3);
        }
    }
}
