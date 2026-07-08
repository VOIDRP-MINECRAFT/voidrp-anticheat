package ru.voidrp.anticheat.network;

import ru.voidrp.anticheat.compat.ClientCompat;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import ru.voidrp.anticheat.VoidRpAnticheat;
import ru.voidrp.anticheat.detection.InjectionDetector;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = VoidRpAnticheat.MOD_ID, value = Dist.CLIENT)
public class AnticheatClientEvents {

    @SubscribeEvent
    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        // Send mod list
        List<String> mods = ModList.get().getMods().stream()
                .map(info -> info.getModId() + ":" + info.getVersion().toString())
                .collect(Collectors.toList());
        ClientCompat.sendToServer(new ModListPayload(mods));
        VoidRpAnticheat.LOG.debug("[AntiCheat] Sent mod list to server ({} mods)", mods.size());

        // Run injection detection off the main thread, then send result
        CompletableFuture.supplyAsync(InjectionDetector::detect).thenAccept(result -> {
            ClientCompat.sendToServer(new InjectionReportPayload(
                    result.javaAgents(),
                    result.suspiciousLibraries(),
                    result.agentsDetected()
            ));
            if (result.agentsDetected()) {
                VoidRpAnticheat.LOG.warn("[AntiCheat] Java agents detected: {}", result.javaAgents());
            }
            if (!result.suspiciousLibraries().isEmpty()) {
                VoidRpAnticheat.LOG.warn("[AntiCheat] Suspicious native libs: {}", result.suspiciousLibraries());
            }
        });
    }
}
