package com.peak.content.entity;

import com.google.common.collect.*;
import com.peak.Main;
import com.peak.manager.util.BossBarManager;
import com.peak.manager.game.DragonFightStateManager;
import com.peak.manager.game.attack.AttackPhaseManager;
import com.peak.manager.rendering.screenshake.ScreenshakeRenderer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathMinHeap;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class DragonEntity extends EnderDragonEntity {
    public static final int MAX_HEALTH = 1000;
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
    public int ticksSinceDeath;
    @Nullable
    private BlockPos exitPortalLocation;
    private int endCrystalsAlive;
    private final ObjectArrayList<Integer> gateways;
    private final BlockPattern endPortalPattern;
    public int playerUpdateTimer;

    @Nullable
    public EndCrystalEntity connectedCrystal; // TODO

    public AttackPhaseManager attackPhaseManager;
    public boolean isCustomDragon = true;

    public DragonEntity(EntityType<? extends DragonEntity> entityType, World world) {
        super(entityType, world);
        this.attackPhaseManager = new AttackPhaseManager(this, world);

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

        this.gateways = new ObjectArrayList();
        BossBarManager.init();
        this.endPortalPattern = BlockPatternBuilder.start().aisle(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "}).aisle(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "}).aisle(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "}).aisle(new String[]{"  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  "}).aisle(new String[]{"       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       "}).where('#', CachedBlockPosition.matchesBlockState(BlockPredicate.make(Blocks.BEDROCK))).build();
    }

    public static DefaultAttributeContainer.Builder createDragonAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, (double)MAX_HEALTH);
    }

    @Override
    public EnderDragonPart[] getBodyParts() {
        return parts;
    }

    @Override
    public void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("isCustomDragon")) {
            this.isCustomDragon = nbt.getBoolean("isCustomDragon");
        }
    }

    @Override
    public void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("isCustomDragon", this.isCustomDragon);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        EnderDragonPart[] dragonParts = this.getBodyParts();

        for(int i = 0; i < dragonParts.length; ++i) {
            dragonParts[i].setId(i + packet.getEntityId());
        }
    }

    @Override
    public boolean isFlappingWings() {
        float f = MathHelper.cos(this.wingPosition * ((float)Math.PI * 2F));
        float g = MathHelper.cos(this.prevWingPosition * ((float)Math.PI * 2F));
        return g <= -0.3F && f >= -0.3F;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public void addFlapEffects() {
        if (this.getWorld().isClient && !this.isSilent()) {
            this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, this.getSoundCategory(), 5.0F, 0.8F + this.random.nextFloat() * 0.3F, false);
        }
    }

    @Override
    public void tickMovement() {
        if (this.getHealth() <= this.getMaxHealth() / 2) {
            DragonFightStateManager.setState(DragonFightStateManager.State.TWO);
        }

        if (this.getWorld().isClient) {
            this.setHealth(this.getHealth());
            if (!this.isSilent() && --this.ticksUntilNextGrowl < 0) {
                this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, this.getSoundCategory(), 2.5F, 0.8F + this.random.nextFloat() * 0.3F, false);
                this.ticksUntilNextGrowl = 200 + this.random.nextInt(200);
            }
        }

        this.tickWithEndCrystals();

        // --- WINGS ---
        this.prevWingPosition = this.wingPosition;
        if (DragonFightStateManager.getState() == DragonFightStateManager.State.ONE) {
            Vec3d vec3d = this.getVelocity();
            float g = 0.2F / ((float)vec3d.horizontalLength() * 10.0F + 1.0F);
            g *= (float)Math.pow((double)2.0F, vec3d.y);

            if (this.attackPhaseManager.getCurrentPhase().isSittingOrHovering()) {
                this.wingPosition += 0.1F;
            } else if (this.slowedDownByBlock) {
                this.wingPosition += g * 0.5F;
            } else {
                this.wingPosition += g;
            }

            this.addAirTravelEffects();
        }

        if (this.isAiDisabled()) {
            this.wingPosition = 0.5f;
        } else {
            this.attackPhaseManager.getCurrentPhase().tick();

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
            this.movePart(this.body, (double)(w * 0.5F), (double)0.0F, (double)(-x * 0.5F));
            this.movePart(this.rightWing, (double)(x * 4.5F), (double)2.0F, (double)(w * 4.5F));
            this.movePart(this.leftWing, (double)(x * -4.5F), (double)2.0F, (double)(w * -4.5F));

            if (this.getWorld() instanceof ServerWorld world) {
                if (this.hurtTime == 0) {
                    this.damageLivingEntities(world.getOtherEntities(this, this.head.getBoundingBox().expand((double)1.0F), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR), 30F);
                    this.damageLivingEntities(world.getOtherEntities(this, this.neck.getBoundingBox().expand((double)1.0F), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR), 30F);
                    this.launchLivingEntities(world, world.getOtherEntities(this, this.rightWing.getBoundingBox().expand((double)4.0F, (double)2.0F, (double)4.0F).offset((double)0.0F, (double)-2.0F, (double)0.0F), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
                    this.launchLivingEntities(world, world.getOtherEntities(this, this.leftWing.getBoundingBox().expand((double)4.0F, (double)2.0F, (double)4.0F).offset((double)0.0F, (double)-2.0F, (double)0.0F), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
                    this.damageLivingEntities(world.getOtherEntities(this, this.rightWing.getBoundingBox().expand((double)2.0F, 1.0F, 2.0F).offset((double)0.0F, (double)-1.0F, (double)0.0F), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR), 15F);
                    this.damageLivingEntities(world.getOtherEntities(this, this.leftWing.getBoundingBox().expand((double)2.0F, 1.0F, 2.0F).offset((double)0.0F, (double)-1.0F, (double)0.0F), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR), 15F);
                }
            }

            // --- SCREEN SHAKE ---
            for (Entity entity : this.getWorld().getOtherEntities(this, this.rightWing.getBoundingBox().expand((double)4.0F, (double)2.0F, (double)4.0F).offset((double)0.0F, (double)-2.0F, (double)0.0F), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
                if (entity instanceof PlayerEntity playerEntity && getWorld().isClient) {
                    if (MinecraftClient.getInstance().player == playerEntity) {
                        ScreenshakeRenderer.getInstance().addShake(200, 10, ScreenshakeRenderer.ShakeType.ROUGH);
                    }
                }
            } for (Entity entity : this.getWorld().getOtherEntities(this, this.leftWing.getBoundingBox().expand((double)4.0F, (double)2.0F, (double)4.0F).offset((double)0.0F, (double)-2.0F, (double)0.0F), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)) {
                if (entity instanceof PlayerEntity playerEntity && getWorld().isClient) {
                    if (MinecraftClient.getInstance().player == playerEntity) {
                        ScreenshakeRenderer.getInstance().addShake(200, 10, ScreenshakeRenderer.ShakeType.ROUGH);
                    }
                }
            }


            float y = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180F) - this.yawAcceleration * 0.01F);
            float z = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180F) - this.yawAcceleration * 0.01F);
            float aa = this.getHeadVerticalMovement();
            this.movePart(this.head, (double)(y * 6.5F * t), (double)(aa + u * 6.5F), (double)(-z * 6.5F * t));
            this.movePart(this.neck, (double)(y * 5.5F * t), (double)(aa + u * 5.5F), (double)(-z * 5.5F * t));
            double[] ds = this.getSegmentProperties(5, 1.0F);

            for(int ab = 0; ab < 3; ++ab) {
                EnderDragonPart enderDragonPart = null;
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
                this.movePart(enderDragonPart, (double)(-(w * 1.5F + n * ad) * t), es[1] - ds[1] - (double)((ad + 1.5F) * u) + (double)1.5F, (double)((x * 1.5F + o * ad) * t));
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

        double verticalSpeed = this.getVelocity().y;

        // Negative pitch = nose up, positive pitch = nose down
        float targetPitch = MathHelper.clamp((float)(-verticalSpeed * 35.0F), -20.0F, 20.0F);
        this.setPitch(MathHelper.lerp(0.15F, this.getPitch(), targetPitch));
    }

    private void movePart(EnderDragonPart dragonPart, double dx, double dy, double dz) {
        dragonPart.setPosition(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
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
        double centerX = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0D;
        double centerZ = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0D;

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity) {
                double dx = entity.getX() - centerX;
                double dz = entity.getZ() - centerZ;
                double dist = Math.sqrt(Math.max(dx * dx + dz * dz, 0.001D));

                double horizontalmod = new java.util.Random().nextDouble(2, 4.5);

                double pushX = (dx / dist) * horizontalmod;
                double pushZ = (dz / dist) * horizontalmod;
                double launchY = new java.util.Random().nextDouble(1, 3);

                entity.setVelocity(pushX, launchY, pushZ);
                entity.velocityModified = true;
                entity.velocityDirty = true;
                entity.fallDistance = 0.0F;
            }
        }
    }

    private void damageLivingEntities(List<Entity> entities, float amount) {
        for(Entity entity : entities) {
            if (entity instanceof LivingEntity) {
                DamageSource damageSource = this.getDamageSources().mobAttack(this);
                entity.damage(damageSource, amount);
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


    private float getHeadVerticalMovement() {
        if (attackPhaseManager.getCurrentPhase().isSittingOrHovering()) {
            return -1.0F;
        } else {
            double[] ds = this.getSegmentProperties(5, 1.0F);
            double[] es = this.getSegmentProperties(0, 1.0F);
            return (float)(ds[1] - es[1]);
        }
    }

    public float getChangeInNeckPitch(int segmentOffset, double[] segment1, double[] segment2) {
        Phase phase = attackPhaseManager.getCurrentPhase();
        PhaseType<? extends Phase> phaseType = phase.getType();
        double e = 0F;
        if (phaseType != PhaseType.LANDING && phaseType != PhaseType.TAKEOFF) {
            if (phase.isSittingOrHovering()) {
                e = (double)segmentOffset;
            } else if (segmentOffset == 6) {
                e = (double)0.0F;
            } else {
                e = segment2[1] - segment1[1];
            }
        }

        return (float)e;
    }

    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDER_DRAGON_HURT;
    }

    protected float getSoundVolume() {
        return 5.0F;
    }

    @Override
    public boolean damagePart(EnderDragonPart part, DamageSource source, float amount) {
        if (this.attackPhaseManager.getCurrentPhase().getType() == PhaseType.DYING) {
            return false;
        } else {
            amount = this.attackPhaseManager.getCurrentPhase().modifyDamageTaken(source, amount);
            if (part != this.head) {
                amount = amount / 4.0F + Math.min(amount, 1.0F);
            }

            if (amount < 0.01F) {
                return false;
            } else {
                if (source.getAttacker() instanceof PlayerEntity || source.isIn(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS)) {
                    this.parentDamage(source, amount);
                    if (this.isDead()) {
                        this.setHealth(1.0F);
                        this.attackPhaseManager.die();
                    }
                }

                return true;
            }
        }
    }

    public int getNearestPathNodeIndex() {
        if (this.pathNodes[0] == null) {
            for(int i = 0; i < 24; ++i) {
                int j = 5;
                int l;
                int m;
                if (i < 12) {
                    l = MathHelper.floor(60.0F * MathHelper.cos(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
                    m = MathHelper.floor(60.0F * MathHelper.sin(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
                } else if (i < 20) {
                    int k = i - 12;
                    l = MathHelper.floor(40.0F * MathHelper.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)k)));
                    m = MathHelper.floor(40.0F * MathHelper.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)k)));
                    j += 10;
                } else {
                    int var7 = i - 20;
                    l = MathHelper.floor(20.0F * MathHelper.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)var7)));
                    m = MathHelper.floor(20.0F * MathHelper.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)var7)));
                }

                int n = Math.max(this.getWorld().getSeaLevel() + 10, this.getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(l, 0, m)).getY() + j);
                this.pathNodes[i] = new PathNode(l, n, m);
            }

            this.pathNodeConnections[0] = 6146;
            this.pathNodeConnections[1] = 8197;
            this.pathNodeConnections[2] = 8202;
            this.pathNodeConnections[3] = 16404;
            this.pathNodeConnections[4] = 32808;
            this.pathNodeConnections[5] = 32848;
            this.pathNodeConnections[6] = 65696;
            this.pathNodeConnections[7] = 131392;
            this.pathNodeConnections[8] = 131712;
            this.pathNodeConnections[9] = 263424;
            this.pathNodeConnections[10] = 526848;
            this.pathNodeConnections[11] = 525313;
            this.pathNodeConnections[12] = 1581057;
            this.pathNodeConnections[13] = 3166214;
            this.pathNodeConnections[14] = 2138120;
            this.pathNodeConnections[15] = 6373424;
            this.pathNodeConnections[16] = 4358208;
            this.pathNodeConnections[17] = 12910976;
            this.pathNodeConnections[18] = 9044480;
            this.pathNodeConnections[19] = 9706496;
            this.pathNodeConnections[20] = 15216640;
            this.pathNodeConnections[21] = 13688832;
            this.pathNodeConnections[22] = 11763712;
            this.pathNodeConnections[23] = 8257536;
        }

        return this.getNearestPathNodeIndex(this.getX(), this.getY(), this.getZ());
    }

    public int getNearestPathNodeIndex(double x, double y, double z) {
        float f = 10000.0F;
        int i = 0;
        PathNode pathNode = new PathNode(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
        int j = 0;

        for(int k = j; k < 24; ++k) {
            if (this.pathNodes[k] != null) {
                float g = this.pathNodes[k].getSquaredDistance(pathNode);
                if (g < f) {
                    f = g;
                    i = k;
                }
            }
        }

        return i;
    }

    @Nullable
    public Path findPath(int from, int to, @Nullable PathNode pathNode) {
        for(int i = 0; i < 24; ++i) {
            PathNode pathNode2 = this.pathNodes[i];
            pathNode2.visited = false;
            pathNode2.heapWeight = 0.0F;
            pathNode2.penalizedPathLength = 0.0F;
            pathNode2.distanceToNearestTarget = 0.0F;
            pathNode2.previous = null;
            pathNode2.heapIndex = -1;
        }

        PathNode pathNode3 = this.pathNodes[from];
        PathNode pathNode2 = this.pathNodes[to];
        pathNode3.penalizedPathLength = 0.0F;
        pathNode3.distanceToNearestTarget = pathNode3.getDistance(pathNode2);
        pathNode3.heapWeight = pathNode3.distanceToNearestTarget;
        this.pathHeap.clear();
        this.pathHeap.push(pathNode3);
        PathNode pathNode4 = pathNode3;

        while(!this.pathHeap.isEmpty()) {
            PathNode pathNode5 = this.pathHeap.pop();
            if (pathNode5.equals(pathNode2)) {
                if (pathNode != null) {
                    pathNode.previous = pathNode2;
                    pathNode2 = pathNode;
                }

                return this.getPathOfAllPredecessors(pathNode3, pathNode2);
            }

            if (pathNode5.getDistance(pathNode2) < pathNode4.getDistance(pathNode2)) {
                pathNode4 = pathNode5;
            }

            pathNode5.visited = true;
            int k = 0;

            for(int l = 0; l < 24; ++l) {
                if (this.pathNodes[l] == pathNode5) {
                    k = l;
                    break;
                }
            }

            for(int l = 0; l < 24; ++l) {
                if ((this.pathNodeConnections[k] & 1 << l) > 0) {
                    PathNode pathNode6 = this.pathNodes[l];
                    if (!pathNode6.visited) {
                        float f = pathNode5.penalizedPathLength + pathNode5.getDistance(pathNode6);
                        if (!pathNode6.isInHeap() || f < pathNode6.penalizedPathLength) {
                            pathNode6.previous = pathNode5;
                            pathNode6.penalizedPathLength = f;
                            pathNode6.distanceToNearestTarget = pathNode6.getDistance(pathNode2);
                            if (pathNode6.isInHeap()) {
                                this.pathHeap.setNodeWeight(pathNode6, pathNode6.penalizedPathLength + pathNode6.distanceToNearestTarget);
                            } else {
                                pathNode6.heapWeight = pathNode6.penalizedPathLength + pathNode6.distanceToNearestTarget;
                                this.pathHeap.push(pathNode6);
                            }
                        }
                    }
                }
            }
        }

        if (pathNode4 == pathNode3) {
            return null;
        } else {
            Main.LOGGER.debug("Failed to find path from {} to {}", from, to);
            if (pathNode != null) {
                pathNode.previous = pathNode4;
                pathNode4 = pathNode;
            }

            return this.getPathOfAllPredecessors(pathNode3, pathNode4);
        }
    }

    private Path getPathOfAllPredecessors(PathNode unused, PathNode node) {
        List<PathNode> list = Lists.newArrayList();
        PathNode pathNode = node;
        list.add(0, node);

        while(pathNode.previous != null) {
            pathNode = pathNode.previous;
            list.add(0, pathNode);
        }

        return new Path(list, new BlockPos(node.x, node.y, node.z), true);
    }

    private void tickWithEndCrystals() {
        if (this.connectedCrystal != null) {
            if (this.connectedCrystal.isRemoved()) {
                this.connectedCrystal = null;
            } else if (this.age % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0F);
            }
        }

        if (this.random.nextInt(10) == 0) {
            List<EndCrystalEntity> list = this.getWorld().getNonSpectatingEntities(EndCrystalEntity.class, this.getBoundingBox().expand((double)32.0F));
            EndCrystalEntity endCrystalEntity = null;
            double d = Double.MAX_VALUE;

            for(EndCrystalEntity endCrystalEntity2 : list) {
                double e = endCrystalEntity2.squaredDistanceTo(this);
                if (e < d) {
                    d = e;
                    endCrystalEntity = endCrystalEntity2;
                }
            }

            this.connectedCrystal = endCrystalEntity;
        }

    }
}
