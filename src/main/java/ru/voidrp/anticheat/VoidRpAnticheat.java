package ru.voidrp.anticheat;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import ru.voidrp.anticheat.config.AnticheatConfig;
import ru.voidrp.anticheat.handler.ServerEventHandler;
import ru.voidrp.anticheat.network.AnticheatNetworking;

@Mod(VoidRpAnticheat.MOD_ID)
public class VoidRpAnticheat {

    public static final String MOD_ID = "voidrp_anticheat";
    public static final Logger LOG = LogUtils.getLogger();

    public VoidRpAnticheat(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, AnticheatConfig.SPEC, "voidrp_anticheat-server.toml");

        AnticheatNetworking.register(modBus);

        NeoForge.EVENT_BUS.register(new ServerEventHandler());

        LOG.info("[VoidRP Anticheat] Loaded — monitoring player behaviour.");
    }
}
