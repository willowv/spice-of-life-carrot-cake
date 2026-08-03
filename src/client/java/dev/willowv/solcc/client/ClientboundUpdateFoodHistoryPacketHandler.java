package dev.willowv.solcc.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

import net.minecraft.client.player.LocalPlayer;
import dev.willowv.solcc.ClientboundUpdateFoodHistoryPacket;
import dev.willowv.solcc.IFoodHistoryManager;

import static dev.willowv.solcc.SpiceOfLifeCarrotCake.LOGGER;

public class ClientboundUpdateFoodHistoryPacketHandler implements ClientPlayNetworking.PlayPacketHandler<ClientboundUpdateFoodHistoryPacket> {

    @Override
    public void receive(ClientboundUpdateFoodHistoryPacket packet, LocalPlayer player, PacketSender responseSender) {
        if (player instanceof IFoodHistoryManager foodHistory) {
            LOGGER.debug("[Client] Received packet from server");
            if(!foodHistory.solcc$getUniqueFoodsEaten().equals(packet.uniqueFoodsEaten)) {
                LOGGER.debug("[Client] Updating food history from {} to {}", foodHistory.solcc$getUniqueFoodsEaten(), packet.uniqueFoodsEaten);
                foodHistory.solcc$setUniqueFoodsEaten(packet.uniqueFoodsEaten);
            }
            else {
                LOGGER.debug("[Client] No food history update necessary");
            }
        }
    }
}