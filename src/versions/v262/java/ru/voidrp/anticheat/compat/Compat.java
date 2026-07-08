package ru.voidrp.anticheat.compat;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import ru.voidrp.anticheat.VoidRpAnticheat;

/**
 * Версионный адаптер API (Minecraft 26.2 / NeoForge 26.2).
 * Общий код обращается только к этому классу; различия версий живут здесь.
 */
public final class Compat {
    private Compat() {}

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String path) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(VoidRpAnticheat.MOD_ID, path));
    }


    public static Holder<MobEffect> speedEffect() {
        return MobEffects.SPEED;
    }

    public static Holder<MobEffect> jumpBoostEffect() {
        return MobEffects.JUMP_BOOST;
    }
}
