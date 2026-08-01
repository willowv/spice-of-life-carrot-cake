package willowv.spiceoflifecarrotcake;

import net.minecraft.world.item.Item;

import java.util.Set;

public interface IFoodHistoryManager {
    Set<Item> solcc$getUniqueFoodsEaten();
    void solcc$setUniqueFoodsEaten(Set<Item> uniqueFoodsEaten);
}
