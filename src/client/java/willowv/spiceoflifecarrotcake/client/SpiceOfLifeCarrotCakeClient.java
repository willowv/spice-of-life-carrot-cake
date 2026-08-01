package willowv.spiceoflifecarrotcake.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import willowv.spiceoflifecarrotcake.IFoodHistoryManager;

import java.util.List;

import static willowv.spiceoflifecarrotcake.SpiceOfLifeCarrotCake.MOD_ID;
import static willowv.spiceoflifecarrotcake.SpiceOfLifeCarrotCake.isProductiveFood;

public class SpiceOfLifeCarrotCakeClient implements ClientModInitializer {
	private static final Component NEVER_EATEN_TOOLTIP = Component.translatable(MOD_ID + ".item.tooltip.never_eaten");

	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.register(SpiceOfLifeCarrotCakeClient::itemTooltipCallback);
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