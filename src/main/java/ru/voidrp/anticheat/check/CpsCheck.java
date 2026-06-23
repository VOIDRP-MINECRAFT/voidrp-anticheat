package ru.voidrp.anticheat.check;

import net.minecraft.server.level.ServerPlayer;
import ru.voidrp.anticheat.config.AnticheatConfig;
import ru.voidrp.anticheat.config.RemoteConfigManager;
import ru.voidrp.anticheat.data.PlayerData;
import ru.voidrp.anticheat.data.ViolationType;

public class CpsCheck {

    public static void check(ServerPlayer player, PlayerData data) {
        if (player.isCreative() || player.isSpectator()) return;

        data.attackTimes.addLast(System.currentTimeMillis());
        int cps = data.currentCps();
        int maxCps = (int) RemoteConfigManager.get("cps_threshold", (double) AnticheatConfig.CPS_THRESHOLD.get());

        if (cps > maxCps) {
            String details = String.format("cps=%d pos=(%.1f,%.1f,%.1f)",
                    cps, player.getX(), player.getY(), player.getZ());
            CheckManager.flag(player, data, ViolationType.CPS, details, cps, maxCps);
        }
    }
}
