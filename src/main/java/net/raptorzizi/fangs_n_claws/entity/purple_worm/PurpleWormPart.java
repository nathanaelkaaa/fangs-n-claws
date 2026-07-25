package net.raptorzizi.fangs_n_claws.entity.purple_worm;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;

public class PurpleWormPart extends PartEntity<PurpleWormEntity> {

    public final PurpleWormEntity parentWorm;
    public final String name;
    private final EntityDimensions size;

    public PurpleWormPart(PurpleWormEntity parent, String name, float width, float height) {
        super(parent);
        this.size = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
        this.parentWorm = parent;
        this.name = name;
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return this.isInvulnerableTo(source) ? false : this.parentWorm.hurtPart(this, source, amount);
    }

    @Override
    public boolean is(@NotNull Entity entity) {
        return this == entity || this.parentWorm == entity;
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return this.size;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
