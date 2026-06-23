package ru.voidrp.anticheat.mixin;

import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.voidrp.anticheat.check.CheckManager;
import ru.voidrp.anticheat.data.PlayerData;

@Mixin(ServerGamePacketListenerImpl.class)
public class ResourcePackResponseMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleResourcePackResponse", at = @At("HEAD"))
    private void voidrp_captureResourcePackStatus(ServerboundResourcePackPacket packet, CallbackInfo ci) {
        if (player == null) return;
        PlayerData data = CheckManager.getOrCreate(player.getUUID());
        data.resourcePackStatus = packet.action().name();
    }
}
