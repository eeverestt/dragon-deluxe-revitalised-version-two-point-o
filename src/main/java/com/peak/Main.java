package com.peak;

import com.peak.content.entity.DragonEntity;
import com.peak.init.DragonEntities;
import com.peak.init.DragonItems;
import com.peak.manager.util.BossBarManager;
import com.peak.manager.util.DragonCollisionUtil;
import com.peak.mixin.EnderDragonFightAccessor;
import com.peak.packet.C2S.DragonAttackC2S;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
	public static final String MODID = "ender-dragon-deluxe-revitalised-version-two-point-o";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static boolean canEnderDragonSpawn = true;

	@Override
	public void onInitialize() {
        DragonEntities.init();

		LOGGER.info("--- Dragon Mod Loaded ---");

        PayloadTypeRegistry.playC2S().register(
                DragonAttackC2S.ID,
                DragonAttackC2S.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                DragonAttackC2S.ID,
                (payload, context) -> {
                    ServerPlayerEntity player = context.player();

                    context.server().execute(() -> {
                        if (player == null || player.isSpectator()) return;

                        float damage = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                        DamageSource source = player.getDamageSources().playerAttack(player);

                        if (!(player.getAttackCooldownProgress(0.0F) >= 0.8F)) {
                            damage = damage / 4;
                        }

                        for (EnderDragonEntity dragon : context.server().getWorld(World.END).getEntitiesByType(DragonEntities.DRAGON_ENTITY_ENTITY_TYPE, LivingEntity::isAlive)) {
                            dragon.damage(source, damage);
                        }
                    });
                }
        );

        DragonItems.register();

        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            DragonCollisionUtil.tick();
            ServerWorld serverWorld = server.getWorld(World.END);

            for (Entity entity : serverWorld.getEntitiesByType(DragonEntities.DRAGON_ENTITY_ENTITY_TYPE, LivingEntity::isAlive)) {
                if (entity instanceof DragonEntity dragon) {
                    BossBarManager.tick(dragon);
                }
            }

            for (Entity entity : serverWorld.getEntitiesByType(EntityType.ENDER_DRAGON, LivingEntity::isAlive)) {
                if (entity instanceof EnderDragonPart) return;
                if (entity instanceof EnderDragonEntity dragon) {
                    if (dragon.getFight() == null) return;

                    Vec3d pos = dragon.getPos();
                    DragonEntity dragonEntity = new DragonEntity(DragonEntities.DRAGON_ENTITY_ENTITY_TYPE, serverWorld);
                    dragonEntity.setPos(pos.x, pos.y, pos.z);
                    dragonEntity.setYaw(dragon.getYaw());
                    dragonEntity.setPitch(dragon.getPitch());
                    dragonEntity.setPersistent();

                    ServerBossBar bossBar = ((EnderDragonFightAccessor) dragon.getFight()).getBossBar();
                    bossBar.clearPlayers();
                    bossBar.setVisible(false);
                    dragon.setFight(null);
                    dragon.remove(Entity.RemovalReason.DISCARDED);

                    if (!canEnderDragonSpawn) return;
                    serverWorld.spawnEntity(dragonEntity);
                    canEnderDragonSpawn = false;
                }
            }
        });
	}
}