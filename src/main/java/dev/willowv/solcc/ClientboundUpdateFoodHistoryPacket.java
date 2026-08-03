package dev.willowv.solcc;

import java.util.Set;
import java.util.stream.Collectors;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.network.FriendlyByteBuf;

import static dev.willowv.solcc.SpiceOfLifeCarrotCake.MOD_ID;

public class ClientboundUpdateFoodHistoryPacket implements FabricPacket {
    public static final ResourceLocation PAYLOAD_ID = new ResourceLocation(MOD_ID, "sync_food_history");
    public static final PacketType<ClientboundUpdateFoodHistoryPacket> TYPE = PacketType.create(PAYLOAD_ID, ClientboundUpdateFoodHistoryPacket::new);

    public final Set<Item> uniqueFoodsEaten;

    public ClientboundUpdateFoodHistoryPacket(Set<Item> uniqueFoodsEaten) {
        this.uniqueFoodsEaten = uniqueFoodsEaten;
    }

    public ClientboundUpdateFoodHistoryPacket (FriendlyByteBuf buf) {
        IntList itemIds = buf.readIntIdList();
        uniqueFoodsEaten = itemIds.intStream()
                .mapToObj(BuiltInRegistries.ITEM::byId)
                .collect(Collectors.toSet());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        int[] itemIds = uniqueFoodsEaten.stream()
                .map(BuiltInRegistries.ITEM::getId)
                .mapToInt(Integer::intValue)
                .toArray();

        friendlyByteBuf.writeIntIdList(IntList.of(itemIds));
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
