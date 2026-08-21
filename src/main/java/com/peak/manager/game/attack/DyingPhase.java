package com.peak.manager.game.attack;

import com.peak.content.entity.DragonEntity;
import com.peak.manager.util.BossBarManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.EndConfiguredFeatures;
import net.minecraft.world.gen.feature.EndPortalFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import org.jetbrains.annotations.Nullable;

public class DyingPhase extends AttackPhase {
    @Nullable
    private Vec3d target;
    private int ticks;

    private final ObjectArrayList<Integer> gateways;
    private BlockPos exitPortalLocation;
    private BlockPos origin = BlockPos.ORIGIN;

    public DyingPhase(AttackPhaseManager manager, DragonEntity dragon, World world) {
        super(manager, dragon, world);
        gateways = new ObjectArrayList<>();
    }

    public void clientTick() {
        if (this.ticks++ % 10 == 0) {
            float f = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
            float g = (this.dragon.getRandom().nextFloat() - 0.5F) * 4.0F;
            float h = (this.dragon.getRandom().nextFloat() - 0.5F) * 8.0F;
            this.dragon.getWorld().addParticle(ParticleTypes.EXPLOSION_EMITTER, this.dragon.getX() + (double)f, this.dragon.getY() + (double)2.0F + (double)g, this.dragon.getZ() + (double)h, (double)0.0F, (double)0.0F, (double)0.0F);
            BossBarManager.die();
        }
    }

    public void serverTick() {
        ++this.ticks;
        if (this.target == null) {
            BlockPos blockPos = this.dragon.getWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.offsetOrigin(this.dragon.getFightOrigin()));
            this.target = Vec3d.ofBottomCenter(blockPos);
        }

        double d = this.target.squaredDistanceTo(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (!(d < (double)100.0F) && !(d > (double)22500.0F) && !this.dragon.horizontalCollision && !this.dragon.verticalCollision) {
            this.dragon.setHealth(1.0F);
        } else {
            this.dragon.setHealth(0.0F);
        }

        if (!world.getBlockState(new BlockPos(-1, 59, 0)).getBlock().equals(Blocks.END_PORTAL) && dragon.getPos() == target) {
            generateEndPortal(false);
        }

        BossBarManager.die();

        generateNewEndGateway();

    }

    private void generateNewEndGateway() {
        if (!this.gateways.isEmpty()) {
            int i = (Integer)this.gateways.remove(this.gateways.size() - 1);
            int j = MathHelper.floor((double)96.0F * Math.cos((double)2.0F * (-Math.PI + 0.15707963267948966 * (double)i)));
            int k = MathHelper.floor((double)96.0F * Math.sin((double)2.0F * (-Math.PI + 0.15707963267948966 * (double)i)));
            this.generateEndGateway(new BlockPos(j, 75, k));
        }
    }

    private void generateEndGateway(BlockPos pos) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        this.world.syncWorldEvent(3000, pos, 0);
        this.world.getRegistryManager().getOptional(RegistryKeys.CONFIGURED_FEATURE).flatMap((registry) -> registry.getEntry(EndConfiguredFeatures.END_GATEWAY_DELAYED)).ifPresent((reference) -> ((ConfiguredFeature)reference.value()).generate(serverWorld, serverWorld.getChunkManager().getChunkGenerator(), Random.create(), pos));
    }

    private void generateEndPortal(boolean previouslyKilled) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        EndPortalFeature endPortalFeature = new EndPortalFeature(previouslyKilled);
        if (this.exitPortalLocation == null) {
            for(this.exitPortalLocation = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.offsetOrigin(this.origin)).down(); this.world.getBlockState(this.exitPortalLocation).isOf(Blocks.BEDROCK) && this.exitPortalLocation.getY() > this.world.getSeaLevel(); this.exitPortalLocation = this.exitPortalLocation.down()) {}
        }

        if (endPortalFeature.generateIfValid(FeatureConfig.DEFAULT, serverWorld, serverWorld.getChunkManager().getChunkGenerator(),Random.create(), this.exitPortalLocation)) {
            int i = MathHelper.ceilDiv(4, 16);
            serverWorld.getChunkManager().chunkLoadingManager.forceLighting(new ChunkPos(this.exitPortalLocation), i);
        }

    }

    public void beginPhase() {
        this.target = null;
        this.ticks = 0;
    }

    public float getMaxYAcceleration() {
        return 3.0F;
    }

    @Nullable
    public Vec3d getPathTarget() {
        return this.target;
    }

    public PhaseType<net.minecraft.entity.boss.dragon.phase.DyingPhase> getType() {
        return PhaseType.DYING;
    }
}
