package dev.willowv.solcc.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.willowv.solcc.IFoodHistoryManager;
import dev.willowv.solcc.SpiceOfLifeCarrotCake;
import dev.willowv.solcc.ClientboundUpdateFoodHistoryPacket;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static dev.willowv.solcc.SpiceOfLifeCarrotCake.*;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin extends Player implements IFoodHistoryManager {

	public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(level, blockPos, f, gameProfile);
	}

	@Shadow
	public abstract void playNotifySound(SoundEvent soundEvent, SoundSource soundSource, float f, float g);

	@Shadow
	public ServerGamePacketListenerImpl connection;

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

	@Inject(method = "restoreFrom", at = @At("RETURN"))
	public void onPlayerCopied(ServerPlayer reference, boolean exact, CallbackInfo ci) {
		uniqueFoodsEaten = ((IFoodHistoryManager) reference).solcc$getUniqueFoodsEaten();
		updateMaxHealth(false);
		updateFoodsEaten();

		// By default, on respawn your health is set to the normal max (20), so we do this to ensure the new max is followed.
		if(!exact) setHealth(getMaxHealth());
	}

	@Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
	public void onDeserialize(CompoundTag data, CallbackInfo ci) {
		if (data.contains(UNIQUE_EATEN_NBT_KEY, Tag.TAG_LIST)) {
			ListTag nbtUniqueFoodsEaten = data.getList(UNIQUE_EATEN_NBT_KEY, Tag.TAG_INT);
			for (Tag tag : nbtUniqueFoodsEaten) {
				if (!(tag instanceof IntTag itemIdTag)) continue;

				Item uniqueFood;
				int itemId = itemIdTag.getAsInt();
				try {
					uniqueFood = BuiltInRegistries.ITEM.byIdOrThrow(itemId);
				} catch (IllegalArgumentException e) {
					LOGGER.warn("Food with ID {} could not be found. Ignoring item.", itemId);
					continue;
				}
				uniqueFoodsEaten.add(uniqueFood);
			}
		}
		LOGGER.debug("[Server] Unique foods list from save: {}", uniqueFoodsEaten);

		updateMaxHealth(false);
		if (data.contains("Health", Tag.TAG_ANY_NUMERIC)) {
			this.setHealth(data.getFloat("Health"));
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
	public void onSerialize(CompoundTag data, CallbackInfo ci) {
		ListTag uniqueFoodsList = new ListTag();
		for (Item food : uniqueFoodsEaten) {
			uniqueFoodsList.add(IntTag.valueOf(BuiltInRegistries.ITEM.getId(food)));
		}
		LOGGER.debug("[Server] Saving unique foods list: {}", uniqueFoodsEaten);
		data.put(UNIQUE_EATEN_NBT_KEY, uniqueFoodsList);
		data.put(SpiceOfLifeCarrotCake.NBT_VERSION_ID, IntTag.valueOf(SpiceOfLifeCarrotCake.NBT_VERSION));
	}

	@Inject(method = "completeUsingItem", at = @At("HEAD"))
	private void recordFoodEaten(CallbackInfo ci) {
		Item eatenItem = this.getUseItem().getItem();
		if(isProductiveFood(eatenItem) && !uniqueFoodsEaten.contains(eatenItem)) {
			LOGGER.debug("[Server] Added {} to the unique foods eaten list: {}", eatenItem, uniqueFoodsEaten);
			uniqueFoodsEaten.add(eatenItem);
			updateMaxHealth(true);
			updateFoodsEaten();
		}
	}

	@Unique
	private void updateFoodsEaten() {
		if(connection != null && ServerPlayNetworking.canSend(connection, ClientboundUpdateFoodHistoryPacket.TYPE))
			ServerPlayNetworking.send(connection.player, new ClientboundUpdateFoodHistoryPacket(uniqueFoodsEaten));
	}

	@Unique
    private void updateMaxHealth(boolean announce) {
		// Don't bother with calculation if they haven't eaten any productive foods yet.
		if(uniqueFoodsEaten.isEmpty()) return;

		double currentHealthBonus = getHealthBonus(uniqueFoodsEaten);

		AttributeInstance maxHealthAttr = this.getAttribute(Attributes.MAX_HEALTH);
		assert maxHealthAttr != null;
		AttributeModifier maxHealthModifier = maxHealthAttr.getModifier(PLAYER_HEALTH_MODIFIER_UUID);
		double previousHealthBonus = maxHealthModifier == null ? 0f : maxHealthModifier.getAmount();

		if(currentHealthBonus != previousHealthBonus) {
			LOGGER.debug("[Server] Updating health bonus from {} to {} hearts.", previousHealthBonus / 2, currentHealthBonus / 2);
			maxHealthAttr.removeModifier(PLAYER_HEALTH_MODIFIER_UUID);
			maxHealthAttr.addPermanentModifier(new AttributeModifier(
					PLAYER_HEALTH_MODIFIER_UUID,
					MOD_ID,
					currentHealthBonus,
					AttributeModifier.Operation.ADDITION)
			);
			if(this.connection != null)
				connection.send(new ClientboundUpdateAttributesPacket(getId(), Collections.singleton(maxHealthAttr)));

			if (currentHealthBonus > previousHealthBonus && announce) {
				playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1F, 1F);
			}
		}
	}
}