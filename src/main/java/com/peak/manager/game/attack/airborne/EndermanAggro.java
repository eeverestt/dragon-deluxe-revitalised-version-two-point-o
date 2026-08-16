package com.peak.manager.game.attack.airborne;

import com.peak.content.entity.DragonEntity;
import com.peak.manager.game.attack.AttackPhase;
import com.peak.manager.game.attack.AttackPhaseManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class EndermanAggro extends AttackPhase {
    private static final double AGGRO_RADIUS = 20.0;
    private static final double DRAGON_RADIUS = 100.0;
    private static final double PARTICLE_BEAM_SPEED = 0.2;

    static boolean hasAgroed = false;
    
    private int ticks;
    private int aggroCounter;
    private List<PlayerEntity> targetPlayers;
    private Map<UUID, Integer> aggroCounts;

    public EndermanAggro(AttackPhaseManager manager, DragonEntity dragon, World world) {
        super(manager, dragon, world);
        this.ticks = 0;
        this.aggroCounter = 0;
    }

    @Override
    public void beginPhase() {
        ticks = 0;
        aggroCounter = 0;

        targetPlayers = world.getPlayers().stream()
                .filter(p -> p.getPos().isInRange(dragon.getPos(), DRAGON_RADIUS))
                .collect(Collectors.toUnmodifiableList());

        aggroCounts = new HashMap<>();
        if (!world.isClient()) {
            Box aggroBox = Box.of(dragon.getPos(), DRAGON_RADIUS * 2, DRAGON_RADIUS * 2, DRAGON_RADIUS * 2);
            List<EndermanEntity> endermen = world.getEntitiesByClass(EndermanEntity.class, aggroBox, e -> e.isAlive());
            double maxSqInit = AGGRO_RADIUS * AGGRO_RADIUS;
            for (EndermanEntity end : endermen) {
                if (end.getTarget() instanceof PlayerEntity) {
                    PlayerEntity p = (PlayerEntity) end.getTarget();
                    UUID id = p.getUuid();
                    if (targetPlayers.stream().anyMatch(tp -> tp.getUuid().equals(id)) && end.squaredDistanceTo(p) <= maxSqInit) {
                        aggroCounts.put(id, aggroCounts.getOrDefault(id, 0) + 1);
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        ticks++;
        
        if (world.isClient()) return;

        ServerWorld serverWorld = (ServerWorld) world;

        if (ticks % 15 == 0) {
            aggroCounter++;

            Box aggroBox = Box.of(dragon.getPos(), DRAGON_RADIUS * 2, DRAGON_RADIUS * 2, DRAGON_RADIUS * 2);
            List<EndermanEntity> endermen = world.getEntitiesByClass(
                    EndermanEntity.class,
                    aggroBox,
                    e -> e.isAlive()
            );

            for (EndermanEntity end : endermen) {
                double maxSqForEnd = AGGRO_RADIUS * AGGRO_RADIUS;
                List<PlayerEntity> sortedPlayers = targetPlayers.stream()
                        .filter(PlayerEntity::isAlive)
                        .filter(p -> end.squaredDistanceTo(p) <= maxSqForEnd)
                        .sorted((a, b) -> Double.compare(end.squaredDistanceTo(a), end.squaredDistanceTo(b)))
                        .collect(Collectors.toList());

                if (end.getTarget() instanceof PlayerEntity) {
                    PlayerEntity old = (PlayerEntity) end.getTarget();
                    UUID oldId = old.getUuid();
                    if (aggroCounts.containsKey(oldId)) {
                        aggroCounts.put(oldId, Math.max(0, aggroCounts.get(oldId) - 1));
                    }
                }

                PlayerEntity chosen = null;
                for (PlayerEntity candidate : sortedPlayers) {
                    UUID id = candidate.getUuid();
                    int current = aggroCounts.getOrDefault(id, 0);
                    if (current < 4) {
                        chosen = candidate;
                        aggroCounts.put(id, current + 1);
                        break;
                    }
                }

                if (chosen != null) {
                    end.setTarget(chosen);
                    end.setAngryAt(chosen.getUuid());
                    spawnParticleBeam(serverWorld, dragon.getPos().add(0, 2, 0), end.getPos(), 200);
                }
            }
        }
        
        if (ticks >= 100) {
            this.manager.nextPhase();
        }
    }

    private void spawnParticleBeam(ServerWorld world, Vec3d origin, Vec3d target, int particleCount) {
        Vec3d direction = target.subtract(origin);
        double distance = direction.length();
        
        if (distance < 0.1) return;
        
        Vec3d normalized = direction.normalize();
        
        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            Vec3d particlePos = origin.add(normalized.multiply(distance * progress));
            
            double offsetX = Math.random() * 0.1;
            double offsetY = Math.random() * 0.1;
            double offsetZ = Math.random() * 0.1;
            
            world.spawnParticles(
                    ParticleTypes.DRAGON_BREATH,
                    particlePos.x + offsetX,
                    particlePos.y + offsetY,
                    particlePos.z + offsetZ,
                    1,
                    PARTICLE_BEAM_SPEED * normalized.x,
                    PARTICLE_BEAM_SPEED * normalized.y,
                    PARTICLE_BEAM_SPEED * normalized.z,
                    0.05
            );
        }
    }

    @Override
    public float getMaxYAcceleration() {
        return 0.6F;
    }

    @Override
    public @Nullable Vec3d getPathTarget() {
        return null;
    }

    @Override
    public PhaseType<? extends Phase> getType() {
        return null;
    }
}
