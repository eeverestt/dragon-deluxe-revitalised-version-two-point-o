package com.peak.init;

import com.peak.Main;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class DragonNetworking {
    public static final Identifier DRAGON_PART_ATTACK = Identifier.of(Main.MODID, "dragon_part_attack");

    public static void sendDragonPartAttack(int entityId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entityId);
        ClientPlayNetworking.send((CustomPayload) buf);
    }
}
