package ru.voidrp.anticheat.check;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import ru.voidrp.anticheat.config.AnticheatConfig;
import ru.voidrp.anticheat.config.RemoteConfigManager;
import ru.voidrp.anticheat.data.PlayerData;
import ru.voidrp.anticheat.data.ViolationType;

public class ReachCheck {

    public static void check(ServerPlayer attacker, Entity victim, PlayerData data) {
        if (attacker.isCreative() || attacker.isSpectator()) return;

        // Distance from attacker eye to victim's bounding box (closest point)
        AABB victimBox = victim.getBoundingBox();
        double dist = Math.sqrt(victimBox.distanceToSqr(attacker.getEyePosition()));

        double threshold = RemoteConfigManager.get("reach_threshold", AnticheatConfig.REACH_THRESHOLD.get());
        if (dist > threshold) {
            String details = String.format("victim=%s pos=(%.1f,%.1f,%.1f)",
                    victim.getName().getString(),
                    attacker.getX(), attacker.getY(), attacker.getZ());
            CheckManager.flag(attacker, data, ViolationType.REACH, details, dist, threshold);
        }
    }
}
