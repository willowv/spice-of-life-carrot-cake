package dev.willowv.solcc.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import dev.willowv.solcc.ClientboundUpdateFoodHistoryPacket;
import dev.willowv.solcc.IFoodHistoryManager;

import java.util.List;

import static dev.willowv.solcc.SpiceOfLifeCarrotCake.*;

public class SpiceOfLifeCarrotCakeClient implements ClientModInitializer {

	private static final Component NEVER_EATEN_TOOLTIP = Component.translatable("item.tooltip."+ MOD_ID + ".never_eaten").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC);

	@Override
	public void onInitializeClient() {
		LOGGER.debug("[Client] Initialized.");
		ItemTooltipCallback.EVENT.register(SpiceOfLifeCarrotCakeClient::itemTooltipCallback);
		ClientPlayNetworking.registerGlobalReceiver(ClientboundUpdateFoodHistoryPacket.TYPE, new ClientboundUpdateFoodHistoryPacketHandler());
	}

	private static void itemTooltipCallback(ItemStack stack, TooltipFlag flag, List<Component> lines) {
		if (Minecraft.getInstance().player instanceof IFoodHistoryManager foodHistory
				&& isProductiveFood(stack.getItem())
				&& !foodHistory.solcc$getUniqueFoodsEaten().contains(stack.getItem()))
		{
			lines.add(1, NEVER_EATEN_TOOLTIP);
		}
	}
}