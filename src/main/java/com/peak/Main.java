package com.peak;

import com.peak.content.entity.DragonEntity;
import com.peak.init.DragonEntities;
import com.peak.init.DragonItems;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
	public static final String MODID = "ender-dragon-deluxe-revitalised-version-two-point-o";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
        DragonEntities.init();

		LOGGER.info("Hello Fabric world!");

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            ServerWorld serverWorld = server.getWorld(World.END);
            for (EnderDragonEntity dragon : serverWorld.getAliveEnderDragons()) {
                if (dragon.getFight() == null) return;
                Vec3d pos = dragon.getPos();
                DragonEntity dragonEntity = new DragonEntity(DragonEntities.DRAGON_ENTITY_ENTITY_TYPE, serverWorld);
                dragonEntity.setPos(pos.x, pos.y, pos.z);
                dragonEntity.setYaw(dragon.getYaw());
                dragonEntity.setPitch(dragon.getPitch());

                dragon.remove(Entity.RemovalReason.DISCARDED);
                serverWorld.spawnEntity(dragonEntity);
            }
        });

        DragonItems.register();
	}
}