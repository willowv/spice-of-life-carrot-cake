package dev.willowv.solcc;

import net.fabricmc.api.ModInitializer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpiceOfLifeCarrotCake implements ModInitializer {
	public static final String MOD_ID = "solcc";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String REPO_LOCATION = "https://github.com/willowv/spice-of-life-carrot-cake";

	// Any food below this threshold does not count toward max health progress.
	// Helpful for filtering out things like raw meat.
	public static final float SATURATION_THRESHOLD = 2;

	// This is the maximum amount of additional hearts the mod can grant.
	// The bonus thresholds are calculated by dividing the total unique saturation
	// max of the registered food items by this number + 1.
	public static final int MAX_BONUS_HEARTS = 10;

	// NBT Data Storage constants
	public static final String NBT_VERSION_ID = MOD_ID + "_version";
	public static final int NBT_VERSION = 1;
	public static final UUID PLAYER_HEALTH_MODIFIER_UUID = UUID.nameUUIDFromBytes(MOD_ID.getBytes(StandardCharsets.UTF_8));
	public static final String UNIQUE_EATEN_NBT_KEY = MOD_ID + "_uniqueFoodsEaten";

	@Override
	public void onInitialize() {
		LOGGER.info("Report issues at {}", REPO_LOCATION);
		SOLCCCommands.register();
	}

	private static float getSaturation(FoodProperties props) {
		assert props != null;
		return props.getNutrition() * props.getSaturationModifier() * 2;
	}

	private static float getTotalSaturation(Stream<Item> items) {
		return items.parallel()
				.filter(SpiceOfLifeCarrotCake::isProductiveFood)
				.map(Item::getFoodProperties).filter(Objects::nonNull)
				.map(SpiceOfLifeCarrotCake::getSaturation)
				.reduce(0f, Float::sum);
	}

	public static boolean isProductiveFood(Item item) {
		return item.isEdible() &&
				(item.getFoodProperties() != null) &&
				getSaturation(item.getFoodProperties()) >= SATURATION_THRESHOLD;
	}

	public static Set<Item> getUneatenFoods(Set<Item> uniqueFoodsEaten) {
		return BuiltInRegistries.ITEM.stream().parallel()
				.filter(SpiceOfLifeCarrotCake::isProductiveFood)
				.filter((item) -> !uniqueFoodsEaten.contains(item))
				.collect(Collectors.toSet());
	}

	public static double getHealthBonus(Set<Item> uniqueFoodsEaten) {
		float uniqueSaturationCurrent = getTotalSaturation(uniqueFoodsEaten.stream());
		float uniqueSaturationMax = getTotalSaturation(BuiltInRegistries.ITEM.stream());
		float milestoneSize = uniqueSaturationMax / (MAX_BONUS_HEARTS + 1);
		double healthBonus = Math.floor(uniqueSaturationCurrent / milestoneSize) * 2;
		LOGGER.debug("Saturation: {}/{} => Bonus: +{} Hearts",
				uniqueSaturationCurrent,
				uniqueSaturationMax,
				healthBonus / 2);
		return healthBonus;
	}
}