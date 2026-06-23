package ru.voidrp.anticheat.check;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import ru.voidrp.anticheat.config.AnticheatConfig;
import ru.voidrp.anticheat.config.RemoteConfigManager;
import ru.voidrp.anticheat.data.PlayerData;
import ru.voidrp.anticheat.data.ViolationType;

public class SpeedCheck {

    public static void check(ServerPlayer player, PlayerData data) {
        if (data.lastPosition == null) return;
        if (shouldSkip(player)) return;

        Vec3 pos = player.position();
        double dx = pos.x - data.lastPosition.x;
        double dz = pos.z - data.lastPosition.z;
        double speed = Math.sqrt(dx * dx + dz * dz);

        double maxSpeed = getMaxAllowedSpeed(player);

        if (speed > maxSpeed) {
            String details = String.format("pos=(%.1f,%.1f,%.1f)", pos.x, pos.y, pos.z);
            CheckManager.flag(player, data, ViolationType.SPEED, details, speed, maxSpeed);
        }
    }

    private static double getMaxAllowedSpeed(ServerPlayer player) {
        double base = RemoteConfigManager.get("speed_threshold", AnticheatConfig.SPEED_THRESHOLD.get());

        // Adjust for Speed effect
        if (player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            int amp = player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier();
            base *= (1.0 + 0.2 * (amp + 1));
        }

        // Dolphin's Grace
        if (player.hasEffect(MobEffects.DOLPHINS_GRACE)) base *= 1.5;

        // Jump Boost provides slight horizontal momentum on landing
        if (player.hasEffect(MobEffects.JUMP)) {
            int amp = player.getEffect(MobEffects.JUMP).getAmplifier();
            base *= (1.0 + 0.05 * (amp + 1));
        }

        // Slow falling / Slime block bounces — give extra buffer
        if (!player.onGround()) base *= 1.3;

        return base;
    }

    private static boolean shouldSkip(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return true;
        if (player.isPassenger()) return true;                    // vehicle
        if (player.isFallFlying()) return true;                   // elytra
        if (player.hasEffect(MobEffects.LEVITATION)) return true;
        // Ice, slime, honey, soul sand/soil — too unreliable to check
        var below = player.level().getBlockState(player.blockPosition().below());
        var id = below.getBlock().getDescriptionId();
        if (id.contains("ice") || id.contains("slime") || id.contains("honey")
                || id.contains("soul_sand") || id.contains("soul_soil")) return true;
        return false;
    }
}
