package com.peak.content.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.PathMinHeap;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DragonOne extends MobEntity implements Monster {
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

    public DragonOne(EntityType<? extends DragonOne> entityType, World world) {
        super(EntityType.ENDER_DRAGON, world);
        this.fightOrigin = BlockPos.ORIGIN;
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
        this.phaseManager = new PhaseManager(this);
    }

}
