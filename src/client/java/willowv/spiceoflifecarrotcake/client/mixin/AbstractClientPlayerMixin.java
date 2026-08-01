package willowv.spiceoflifecarrotcake.client.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import willowv.spiceoflifecarrotcake.IFoodHistoryManager;

import java.util.HashSet;
import java.util.Set;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin implements IFoodHistoryManager {

	@Unique
	private Set<Item> uniqueFoodsEaten = null;

	@Unique
	public Set<Item> solcc$getUniqueFoodsEaten() { return uniqueFoodsEaten; }

	@Unique
	public void solcc$setUniqueFoodsEaten(Set<Item> uniqueFoodsEaten) { this.uniqueFoodsEaten = uniqueFoodsEaten; }

	@Inject(method = "<init>", at = @At("CTOR_HEAD"))
	public void onConstruct(CallbackInfo ci) {
		uniqueFoodsEaten = new HashSet<>();
	}
}