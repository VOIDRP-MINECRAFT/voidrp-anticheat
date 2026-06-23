package ru.voidrp.anticheat.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Client → Server: JVM agent & native library injection detection results. */
public record InjectionReportPayload(
        List<String> javaAgents,
        List<String> suspiciousLibraries,
        boolean agentsDetected
) implements CustomPacketPayload {

    public static final Type<InjectionReportPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("voidrp_anticheat", "injection_report"));

    public static final StreamCodec<FriendlyByteBuf, InjectionReportPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
                    InjectionReportPayload::javaAgents,
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
                    InjectionReportPayload::suspiciousLibraries,
                    ByteBufCodecs.BOOL,
                    InjectionReportPayload::agentsDetected,
                    InjectionReportPayload::new
            );

    @Override
    public Type<InjectionReportPayload> type() { return TYPE; }
}
