package ru.voidrp.anticheat.network;

import ru.voidrp.anticheat.compat.Compat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/** Client → Server: client sends its installed mod list on join. */
public record ModListPayload(List<String> mods) implements CustomPacketPayload {

    public static final Type<ModListPayload> TYPE =
            Compat.payloadType("mod_list");

    public static final StreamCodec<FriendlyByteBuf, ModListPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
                    ModListPayload::mods,
                    ModListPayload::new
            );

    @Override
    public Type<ModListPayload> type() { return TYPE; }
}
