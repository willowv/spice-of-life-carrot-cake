package dev.willowv.solcc.mixin;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.willowv.solcc.ClientboundUpdateFoodHistoryPacket;
import dev.willowv.solcc.IFoodHistoryManager;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    public void onConnected(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
        if(connection != null
                && ServerPlayNetworking.canSend(serverPlayer, ClientboundUpdateFoodHistoryPacket.TYPE)
                && serverPlayer instanceof IFoodHistoryManager foodHistory)
            ServerPlayNetworking.send(serverPlayer, new ClientboundUpdateFoodHistoryPacket(foodHistory.solcc$getUniqueFoodsEaten()));
    }
}
