package net.raptorzizi.fangs_n_claws.entity.catching_claw;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

public class CatchingClawHookEntity extends ThrowableProjectile {

    // Variables

    private static final EntityDataAccessor<Integer> HOOKED_ENTITY_ID =
            SynchedEntityData.defineId(CatchingClawHookEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IN_GROUND =
            SynchedEntityData.defineId(CatchingClawHookEntity.class, EntityDataSerializers.BOOLEAN);

    private static final float  HOOK_DAMAGE_FALLBACK = 3.0f;
    private static final double PULL_FORCE_BASE      = 1.0;
    private static final double PULL_FORCE_DIST      = 0.15;
    private static final int    MAX_LIFETIME         = 800;

    // Spawn

    public CatchingClawHookEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public CatchingClawHookEntity(Player owner, Level level) {
        this(EntityRegistry.CATCHING_CLAW_HOOK.get(), owner, level);
    }

    protected CatchingClawHookEntity(EntityType<? extends ThrowableProjectile> type, Player owner, Level level) {
        super(type, level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(HOOKED_ENTITY_ID, 0);
        this.entityData.define(IN_GROUND, false);
    }

    public Entity getHookedEntity() {
        int id = this.entityData.get(HOOKED_ENTITY_ID);
        return id > 0 ? this.level().getEntity(id - 1) : null;
    }

    private void setHookedEntity(Entity entity) {
        this.entityData.set(HOOKED_ENTITY_ID, entity != null ? entity.getId() + 1 : 0);
    }

    public boolean isHooked()   { return this.entityData.get(HOOKED_ENTITY_ID) > 0; }
    public boolean isInGround() { return this.entityData.get(IN_GROUND); }
    private void setInGround(boolean value) { this.entityData.set(IN_GROUND, value); }

    private float getHookDamage() {
        Entity owner = this.getOwner();
        if (owner instanceof Player player) {
            return (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        }
        return HOOK_DAMAGE_FALLBACK;
    }

    // Tick

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            if (this.tickCount > MAX_LIFETIME) { this.discard(); return; }
            Entity owner = this.getOwner();
            if (owner == null || !owner.isAlive()) { this.discard(); return; }
        }

        Entity hooked = getHookedEntity();
        if (hooked != null) {
            if (!this.level().isClientSide && !hooked.isAlive()) {
                setHookedEntity(null);
            } else {
                this.setPos(hooked.getX(),
                        hooked.getY() + hooked.getBbHeight() * 0.5,
                        hooked.getZ());
                this.setDeltaMovement(Vec3.ZERO);
                this.baseTick();
                return;
            }
        }

        if (isInGround()) {
            this.baseTick();
            return;
        }

        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.929));
    }

    @Override
    protected float getGravity() {
        return 0.03f;
    }

    // Collision

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        Entity target = result.getEntity();
        Entity owner  = this.getOwner();
        if (target == owner || isHooked()) return;

        if (target instanceof LivingEntity living) {
            living.hurt(this.level().damageSources().thrown(this, owner), getHookDamage());
            setHookedEntity(target);
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.level().isClientSide) return;
        setInGround(true);
        this.setDeltaMovement(Vec3.ZERO);
    }

    // Retrieve

    public void retrieve(Player player) {
        if (this.level().isClientSide) return;
        Entity hooked = getHookedEntity();

        if (hooked instanceof LivingEntity living) {
            Vec3 toPlayer = player.position().subtract(living.position());
            double dist = toPlayer.length();
            Vec3 toPlayerNorm = dist > 1e-6 ? toPlayer.normalize() : Vec3.ZERO;
            Vec3 lookDir = player.getLookAngle();
            Vec3 impulse = lookDir.scale(2.0).add(toPlayerNorm).normalize();

            double force = PULL_FORCE_BASE + Math.sqrt(dist) * PULL_FORCE_DIST;
            living.setDeltaMovement(impulse.scale(force));
            living.invulnerableTime = 0;
            living.hurt(this.level().damageSources().thrown(this, player), getHookDamage());
        }

        this.discard();
    }

    // Save / Load

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("InGround", isInGround());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        setInGround(tag.getBoolean("InGround"));
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }
}
