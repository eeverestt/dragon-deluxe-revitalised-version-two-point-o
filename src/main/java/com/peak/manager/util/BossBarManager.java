package com.peak.manager.util;

import com.google.common.collect.Sets;
import com.peak.content.entity.DragonEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Set;

public class BossBarManager {
    public static ServerBossBar bossBar;

    public static void init() {
        bossBar = (ServerBossBar)(new ServerBossBar(Text.translatable("entity.minecraft.ender_dragon"), BossBar.Color.PINK, BossBar.Style.PROGRESS)).setDragonMusic(true).setThickenFog(true);
    }

    public static void tick(DragonEntity dragon) {
        if (dragon.attackPhaseManager.getCurrentPhase().getType() != PhaseType.DYING) {
            bossBar.setVisible(!dragon.isDead());

            Set<ServerPlayerEntity> set = Sets.newHashSet();

            for (PlayerEntity playerEntity : dragon.getWorld().getPlayers()) {
                if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
                    bossBar.addPlayer(serverPlayerEntity);
                    set.add(serverPlayerEntity);
                }
            }

            Set<ServerPlayerEntity> set2 = Sets.newHashSet(bossBar.getPlayers());
            set2.removeAll(set);

            for (ServerPlayerEntity serverPlayerEntity2 : set2) {
                bossBar.removePlayer(serverPlayerEntity2);
            }
            dragon.playerUpdateTimer = 0;

            bossBar.setPercent(dragon.getHealth() / dragon.getMaxHealth());
        }
    }

    public static void die() {
        bossBar.setVisible(false);
        bossBar.clearPlayers();
    }
}
