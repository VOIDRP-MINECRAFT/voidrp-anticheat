package ru.voidrp.anticheat.check;

import ru.voidrp.anticheat.compat.Compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import ru.voidrp.anticheat.config.AnticheatConfig;
import ru.voidrp.anticheat.config.RemoteConfigManager;
import ru.voidrp.anticheat.data.PlayerData;
import ru.voidrp.anticheat.data.ViolationType;

public class FlyCheck {

    public static void check(ServerPlayer player, PlayerData data) {
        if (shouldSkip(player)) {
            data.airTicks = 0;
            return;
        }

        if (!player.onGround()) {
            data.airTicks++;
        } else {
            data.airTicks = 0;
            return;
        }

        int threshold = (int) RemoteConfigManager.get("fly_ticks_threshold", (double) AnticheatConfig.FLY_TICKS_THRESHOLD.get());
        // Jump Boost extends hang-time: each amplifier level adds ~10 ticks
        if (player.hasEffect(Compat.jumpBoostEffect())) {
            int amp = player.getEffect(Compat.jumpBoostEffect()).getAmplifier();
            threshold += (amp + 1) * 12;
        }
        if (data.airTicks < threshold) return;

        // Check y-velocity: legit gravity pulls player down ~0.08 b/t per tick
        // If player is NOT descending meaningfully, they're hovering
        Vec3 delta = player.getDeltaMovement();
        if (delta.y > -0.05) {
            String details = String.format("airTicks=%d dy=%.3f pos=(%.1f,%.1f,%.1f)",
                    data.airTicks, delta.y, player.getX(), player.getY(), player.getZ());
            CheckManager.flag(player, data, ViolationType.FLY, details, data.airTicks, threshold);
        }
    }

    private static boolean shouldSkip(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return true;
        if (player.isPassenger()) return true;
        if (player.isFallFlying()) return true;
        if (player.hasEffect(MobEffects.LEVITATION)) return true;
        if (player.hasEffect(MobEffects.SLOW_FALLING)) return true;
        // Riptide enchantment in water/rain
        if (player.isInWaterOrRain()) return true;
        // Climbing (ladders, vines, scaffolding)
        if (player.onClimbable()) return true;
        return false;
    }
}
