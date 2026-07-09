package net.raptorzizi.fangs_n_claws.entity.frozen_box;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public class FrozenBoxEntity extends Entity implements GeoEntity {

    private static final EntityDataAccessor<Integer> TARGET_ID =
            SynchedEntityData.defineId(FrozenBoxEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID targetUUID = null;

    public FrozenBoxEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics  = true;
        this.setNoGravity(true);
        this.setInvisible(false);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TARGET_ID, -1);
    }

    public void setTarget(LivingEntity entity) {
        this.targetUUID = entity.getUUID();
        this.entityData.set(TARGET_ID, entity.getId());
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    @Nullable
    public LivingEntity getTarget() {
        int id = this.entityData.get(TARGET_ID);
        if (id < 0) return null;
        Entity e = level().getEntity(id);
        return e instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = getTarget();

        if (target == null || !target.isAlive() || !target.hasEffect(MobEffectsRegistry.FROZEN.get())) {
            if (!level().isClientSide()) discard();
            return;
        }

        if (!level().isClientSide() && targetUUID != null && entityData.get(TARGET_ID) < 0) {
            if (level() instanceof net.minecraft.server.level.ServerLevel sl) {
                Entity found = sl.getEntity(targetUUID);
                if (found != null) entityData.set(TARGET_ID, found.getId());
            }
        }

        this.setPos(target.getX(), target.getY(), target.getZ());
    }

    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    public boolean isPickable()    { return false; }

    @Override
    public boolean isPushable()    { return false; }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("TargetUUID")) this.targetUUID = tag.getUUID("TargetUUID");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (targetUUID != null) tag.putUUID("TargetUUID", targetUUID);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
