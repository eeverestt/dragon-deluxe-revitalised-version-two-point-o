package com.peak.content.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import org.jetbrains.annotations.Nullable;

public class DragonPart extends EnderDragonPart {
    public DragonPart(EnderDragonEntity owner, String name, float width, float height) {
        super(owner, name, width, height);
    }

    protected void initDataTracker(DataTracker.Builder builder) {
    }

    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Nullable
    public ItemStack getPickBlockStack() {
        return this.owner.getPickBlockStack();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        System.out.println("DragonPart.damage called on " + this.name + " from " + source.getName());
        if (this.isInvulnerableTo(source)) return false;
        return ((DragonEntity) this.owner).damagePart(this, source, amount);
    }

    public boolean isPartOf(Entity entity) {
        return this == entity || this.owner == entity;
    }

    public boolean shouldSave() {
        return false;
    }
}
