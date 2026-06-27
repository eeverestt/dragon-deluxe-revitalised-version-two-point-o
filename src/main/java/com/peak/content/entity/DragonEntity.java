package com.peak.content.entity;

import com.peak.manager.game.DragonFightStateManager;
import com.peak.manager.game.attack.AttackPhaseManager;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.pathing.PathMinHeap;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonEntity extends MobEntity implements Monster {
    private static final int MAX_HEALTH = 200;
    private static final int field_30429 = 400;
    private static final float TAKEOFF_THRESHOLD = 0.25F;
    public final double[][] segmentCircularBuffer = new double[64][3];
    public int latestSegment = -1;
    private final DragonPart[] parts;
    public final DragonPart head;
    private final DragonPart neck;
    private final DragonPart body;
    private final DragonPart tail1;
    private final DragonPart tail2;
    private final DragonPart tail3;
    private final DragonPart rightWing;
    private final DragonPart leftWing;
    public float prevWingPosition;
    public float wingPosition;
    public boolean slowedDownByBlock;
    public float yawAcceleration;
    @Nullable
    public EndCrystalEntity[] connectedCrystals;
    private int ticksUntilNextGrowl;
    private float damageDuringSitting;
    private final PathNode[] pathNodes;
    private final int[] pathNodeConnections;
    private final PathMinHeap pathHeap;

    AttackPhaseManager attackPhaseManager;

    public DragonEntity(EntityType<? extends DragonEntity> entityType, World world) {
        super(EntityType.ENDER_DRAGON, world);
        this.ticksUntilNextGrowl = 100;
        this.pathNodes = new PathNode[24];
        this.pathNodeConnections = new int[24];
        this.pathHeap = new PathMinHeap();
        this.head = new DragonPart(this, "head", 1.0F, 1.0F);
        this.neck = new DragonPart(this, "neck", 3.0F, 3.0F);
        this.body = new DragonPart(this, "body", 5.0F, 3.0F);
        this.tail1 = new DragonPart(this, "tail", 2.0F, 2.0F);
        this.tail2 = new DragonPart(this, "tail", 2.0F, 2.0F);
        this.tail3 = new DragonPart(this, "tail", 2.0F, 2.0F);
        this.rightWing = new DragonPart(this, "wing", 4.0F, 2.0F);
        this.leftWing = new DragonPart(this, "wing", 4.0F, 2.0F);
        this.parts = new DragonPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.rightWing, this.leftWing};
        this.setHealth(this.getMaxHealth());
        this.noClip = true;
        this.ignoreCameraFrustum = true;

        this.attackPhaseManager = new AttackPhaseManager(this, world.getServer());
    }

    @Override
    public boolean isFlappingWings() {
        float f = MathHelper.cos(this.wingPosition * ((float)Math.PI * 2F));
        float g = MathHelper.cos(this.prevWingPosition * ((float)Math.PI * 2F));
        return g <= -0.3F && f >= -0.3F;
    }

    @Override
    public void addFlapEffects() {
        if (this.getWorld().isClient && !this.isSilent()) {
            this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, this.getSoundCategory(), 5.0F, 0.8F + this.random.nextFloat() * 0.3F, false);
        }
    }

    @Override
    public void tickMovement() {
        this.addAirTravelEffects();
        if (this.getWorld().isClient) {
            this.setHealth(this.getHealth());
            if (!this.isSilent() && DragonFightStateManager.getState() == DragonFightStateManager.State.ONE && --this.ticksUntilNextGrowl < 0) {
                this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, this.getSoundCategory(), 2.5F, 0.8F + this.random.nextFloat() * 0.3F, false);
                this.ticksUntilNextGrowl = 200 + this.random.nextInt(200);
            }
        }

        if (this.isAiDisabled()) {
            this.wingPosition = 0.5f;
        } else {
            if (this.latestSegment < 0) {
                for(int i = 0; i < this.segmentCircularBuffer.length; ++i) {
                    this.segmentCircularBuffer[i][0] = (double)this.getYaw();
                    this.segmentCircularBuffer[i][1] = this.getY();
                }
            }

            if (++this.latestSegment == this.segmentCircularBuffer.length) {
                this.latestSegment = 0;
            }

            this.segmentCircularBuffer[this.latestSegment][0] = (double)this.getYaw();
            this.segmentCircularBuffer[this.latestSegment][1] = this.getY();
            if (this.getWorld().isClient) {
                if (this.bodyTrackingIncrements > 0) {
                    this.lerpPosAndRotation(this.bodyTrackingIncrements, this.serverX, this.serverY, this.serverZ, this.serverYaw, this.serverPitch);
                    --this.bodyTrackingIncrements;
                }

                this.attackPhaseManager.getCurrentPhase().clientTick();
            } else {
                Phase phase = this.attackPhaseManager.getCurrentPhase();
                phase.serverTick();
                if (this.attackPhaseManager.getCurrentPhase() != phase) {
                    phase = this.attackPhaseManager.getCurrentPhase();
                    phase.serverTick();
                }

                Vec3d vec3d2 = phase.getPathTarget();
                if (vec3d2 != null) {
                    double d = vec3d2.x - this.getX();
                    double e = vec3d2.y - this.getY();
                    double j = vec3d2.z - this.getZ();
                    double k = d * d + e * e + j * j;
                    float l = phase.getMaxYAcceleration();
                    double m = Math.sqrt(d * d + j * j);
                    if (m > (double)0.0F) {
                        e = MathHelper.clamp(e / m, (double)(-l), (double)l);
                    }

                    this.setVelocity(this.getVelocity().add((double)0.0F, e * 0.01, (double)0.0F));
                    this.setYaw(MathHelper.wrapDegrees(this.getYaw()));
                    Vec3d vec3d3 = vec3d2.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                    Vec3d vec3d4 = (new Vec3d((double)MathHelper.sin(this.getYaw() * ((float)Math.PI / 180F)), this.getVelocity().y, (double)(-MathHelper.cos(this.getYaw() * ((float)Math.PI / 180F))))).normalize();
                    float n = Math.max(((float)vec3d4.dotProduct(vec3d3) + 0.5F) / 1.5F, 0.0F);
                    if (Math.abs(d) > (double)1.0E-5F || Math.abs(j) > (double)1.0E-5F) {
                        float o = MathHelper.clamp(MathHelper.wrapDegrees(180.0F - (float)MathHelper.atan2(d, j) * (180F / (float)Math.PI) - this.getYaw()), -50.0F, 50.0F);
                        this.yawAcceleration *= 0.8F;
                        this.yawAcceleration += o * phase.getYawAcceleration();
                        this.setYaw(this.getYaw() + this.yawAcceleration * 0.1F);
                    }

                    float o = (float)((double)2.0F / (k + (double)1.0F));
                    float p = 0.06F;
                    this.updateVelocity(0.06F * (n * o + (1.0F - o)), new Vec3d((double)0.0F, (double)0.0F, (double)-1.0F));
                    if (this.slowedDownByBlock) {
                        this.move(MovementType.SELF, this.getVelocity().multiply((double)0.8F));
                    } else {
                        this.move(MovementType.SELF, this.getVelocity());
                    }

                    Vec3d vec3d5 = this.getVelocity().normalize();
                    double q = 0.8 + 0.15 * (vec3d5.dotProduct(vec3d4) + (double)1.0F) / (double)2.0F;
                    this.setVelocity(this.getVelocity().multiply(q, (double)0.91F, q));
                }
            }

            this.bodyYaw = this.getYaw();
            Vec3d[] vec3ds = new Vec3d[this.parts.length];

            for(int r = 0; r < this.parts.length; ++r) {
                vec3ds[r] = new Vec3d(this.parts[r].getX(), this.parts[r].getY(), this.parts[r].getZ());
            }

            float s = (float)(this.getSegmentProperties(5, 1.0F)[1] - this.getSegmentProperties(10, 1.0F)[1]) * 10.0F * ((float)Math.PI / 180F);
            float t = MathHelper.cos(s);
            float u = MathHelper.sin(s);
            float v = this.getYaw() * ((float)Math.PI / 180F);
            float w = MathHelper.sin(v);
            float x = MathHelper.cos(v);
            this.body.setPos((double)(w * 0.5F), (double)0.0F, (double)(-x * 0.5F));
            this.rightWing.setPos((double)(x * 4.5F), (double)2.0F, (double)(w * 4.5F));
            this.leftWing.setPos((double)(x * -4.5F), (double)2.0F, (double)(w * -4.5F));

            if (this.getWorld() instanceof ServerWorld world) {
                if (this.hurtTime == 0) {
                    this.launchLivingEntities(world, world.getOtherEntities(this, this.rightWing.getBoundingBox().expand((double)4.0F, (double)2.0F, (double)4.0F).offset((double)0.0F, (double)-2.0F, (double)0.0F), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
                    this.launchLivingEntities(world, world.getOtherEntities(this, this.leftWing.getBoundingBox().expand((double)4.0F, (double)2.0F, (double)4.0F).offset((double)0.0F, (double)-2.0F, (double)0.0F), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
                    this.damageLivingEntities(world.getOtherEntities(this, this.head.getBoundingBox().expand((double)1.0F), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
                    this.damageLivingEntities(world.getOtherEntities(this, this.neck.getBoundingBox().expand((double)1.0F), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
                }
            }

            float y = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180F) - this.yawAcceleration * 0.01F);
            float z = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180F) - this.yawAcceleration * 0.01F);
            float aa = this.getHeadVerticalMovement();
            this.head.setPos((double)(y * 6.5F * t), (double)(aa + u * 6.5F), (double)(-z * 6.5F * t));
            this.neck.setPos((double)(y * 5.5F * t), (double)(aa + u * 5.5F), (double)(-z * 5.5F * t));
            double[] ds = this.getSegmentProperties(5, 1.0F);

            for(int ab = 0; ab < 3; ++ab) {
                DragonPart enderDragonPart = null;
                if (ab == 0) {
                    enderDragonPart = this.tail1;
                }

                if (ab == 1) {
                    enderDragonPart = this.tail2;
                }

                if (ab == 2) {
                    enderDragonPart = this.tail3;
                }

                double[] es = this.getSegmentProperties(12 + ab * 2, 1.0F);
                float ac = this.getYaw() * ((float)Math.PI / 180F) + (float) MathHelper.wrapDegrees(es[0] - ds[0]) * ((float)Math.PI / 180F);
                float n = MathHelper.sin(ac);
                float o = MathHelper.cos(ac);
                float p = 1.5F;
                float ad = (float)(ab + 1) * 2.0F;
                enderDragonPart.setPos((double)(-(w * 1.5F + n * ad) * t), es[1] - ds[1] - (double)((ad + 1.5F) * u) + (double)1.5F, (double)((x * 1.5F + o * ad) * t));
            }

            if (!this.getWorld().isClient) {
                this.slowedDownByBlock = this.destroyBlocks(this.head.getBoundingBox()) | this.destroyBlocks(this.neck.getBoundingBox()) | this.destroyBlocks(this.body.getBoundingBox());
                // TODO: ADD BOSS BAR
            }

            for(int ab = 0; ab < this.parts.length; ++ab) {
                this.parts[ab].prevX = vec3ds[ab].x;
                this.parts[ab].prevY = vec3ds[ab].y;
                this.parts[ab].prevZ = vec3ds[ab].z;
                this.parts[ab].lastRenderX = vec3ds[ab].x;
                this.parts[ab].lastRenderY = vec3ds[ab].y;
                this.parts[ab].lastRenderZ = vec3ds[ab].z;
            }

        }
    }

    private boolean destroyBlocks(Box box) {
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.floor(box.minY);
        int k = MathHelper.floor(box.minZ);
        int l = MathHelper.floor(box.maxX);
        int m = MathHelper.floor(box.maxY);
        int n = MathHelper.floor(box.maxZ);
        boolean bl = false;
        boolean bl2 = false;

        for(int o = i; o <= l; ++o) {
            for(int p = j; p <= m; ++p) {
                for(int q = k; q <= n; ++q) {
                    BlockPos blockPos = new BlockPos(o, p, q);
                    BlockState blockState = this.getWorld().getBlockState(blockPos);
                    if (!blockState.isAir() && !blockState.isIn(BlockTags.DRAGON_TRANSPARENT)) {
                        if (this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) && !blockState.isIn(BlockTags.DRAGON_IMMUNE)) {
                            bl2 = this.getWorld().removeBlock(blockPos, false) || bl2;
                        } else {
                            bl = true;
                        }
                    }
                }
            }
        }

        if (bl2) {
            BlockPos blockPos2 = new BlockPos(i + this.random.nextInt(l - i + 1), j + this.random.nextInt(m - j + 1), k + this.random.nextInt(n - k + 1));
            this.getWorld().syncWorldEvent(2008, blockPos2, 0);
        }

        return bl;
    }

    private void launchLivingEntities(ServerWorld world, List<Entity> entities) {
        double d = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / (double)2.0F;
        double e = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / (double)2.0F;

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                double f = entity.getX() - d;
                double g = entity.getZ() - e;
                double h = Math.max(f * f + g * g, 0.1);
                entity.addVelocity(f / h * (double)4.0F, (double)0.2F, g / h * (double)4.0F);
            }
        }
    }

    private void damageLivingEntities(List<Entity> entities) {
        for(Entity entity : entities) {
            if (entity instanceof LivingEntity) {
                DamageSource damageSource = this.getDamageSources().mobAttack(this);
                entity.damage(damageSource, 10.0F);
                World var6 = this.getWorld();
                if (var6 instanceof ServerWorld) {
                    ServerWorld serverWorld = (ServerWorld)var6;
                    EnchantmentHelper.onTargetDamaged(serverWorld, entity, damageSource);
                }
            }
        }
    }

    public double[] getSegmentProperties(int segmentNumber, float tickDelta) {
        if (this.isDead()) {
            tickDelta = 0.0F;
        }

        tickDelta = 1.0F - tickDelta;
        int i = this.latestSegment - segmentNumber & 63;
        int j = this.latestSegment - segmentNumber - 1 & 63;
        double[] ds = new double[3];
        double d = this.segmentCircularBuffer[i][0];
        double e = MathHelper.wrapDegrees(this.segmentCircularBuffer[j][0] - d);
        ds[0] = d + e * (double)tickDelta;
        d = this.segmentCircularBuffer[i][1];
        e = this.segmentCircularBuffer[j][1] - d;
        ds[1] = d + e * (double)tickDelta;
        ds[2] = MathHelper.lerp((double)tickDelta, this.segmentCircularBuffer[i][2], this.segmentCircularBuffer[j][2]);
        return ds;
    }

    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        DragonPart[] enderDragonParts = this.parts;

        for(int i = 0; i < enderDragonParts.length; ++i) {
            enderDragonParts[i].setId(i + packet.getEntityId());
        }
    }


    private float getHeadVerticalMovement() {
        if (attackPhaseManager.getCurrentPhase().isSittingOrHovering()) {
            return -1.0F;
        } else {
            double[] ds = this.getSegmentProperties(5, 1.0F);
            double[] es = this.getSegmentProperties(0, 1.0F);
            return (float)(ds[1] - es[1]);
        }
    }
}
