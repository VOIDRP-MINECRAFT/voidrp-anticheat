package ru.voidrp.anticheat.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import ru.voidrp.anticheat.VoidRpAnticheat;
import ru.voidrp.anticheat.check.CheckManager;
import ru.voidrp.anticheat.data.PlayerData;

public class AnticheatNetworking {

    public static void register(IEventBus modBus) {
        modBus.addListener(AnticheatNetworking::onRegisterPayloads);
    }

    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // C2S: client sends mod list on join
        registrar.playToServer(
                ModListPayload.TYPE,
                ModListPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> handleModList(payload, (ServerPlayer) ctx.player()))
        );

        // C2S: client sends injection detection results on join
        registrar.playToServer(
                InjectionReportPayload.TYPE,
                InjectionReportPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> handleInjectionReport(payload, (ServerPlayer) ctx.player()))
        );
    }

    private static void handleModList(ModListPayload payload, ServerPlayer player) {
        if (player == null) return;
        PlayerData data = CheckManager.getOrCreate(player.getUUID());
        data.clientMods = payload.mods();
        VoidRpAnticheat.LOG.info("[AntiCheat] Received mod list from {} ({} mods)",
                player.getName().getString(), payload.mods().size());
    }

    private static void handleInjectionReport(InjectionReportPayload payload, ServerPlayer player) {
        if (player == null) return;
        PlayerData data = CheckManager.getOrCreate(player.getUUID());
        data.injectionReport = payload;

        if (payload.agentsDetected() || !payload.suspiciousLibraries().isEmpty()) {
            VoidRpAnticheat.LOG.warn("[AntiCheat] INJECTION DETECTED for {} | agents={} libs={}",
                    player.getName().getString(), payload.javaAgents(), payload.suspiciousLibraries());
        }
    }
}
