package com.peak.packet.C2S;

import com.peak.Main;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record DragonAttackC2S(int entityId) implements CustomPayload {
    public static final CustomPayload.Id<DragonAttackC2S> ID =
            new CustomPayload.Id<>(Identifier.of(Main.MODID, "dragon_part_attack"));

    public static final PacketCodec<RegistryByteBuf, DragonAttackC2S> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER,
                    DragonAttackC2S::entityId,
                    DragonAttackC2S::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}