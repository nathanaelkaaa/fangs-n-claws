package net.raptorzizi.fangs_n_claws.entity.projectile;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;

import java.util.UUID;

public class BlockProjectile extends ThrowableProjectile {

    private static final EntityDataAccessor<BlockState> BLOCK_STATE = SynchedEntityData.defineId(BlockProjectile.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Boolean> REGEN_MODE = SynchedEntityData.defineId(BlockProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PARENT_GOLEM_ID = SynchedEntityData.defineId(BlockProjectile.class, EntityDataSerializers.INT);

    private static final double REGEN_ARRIVAL_DIST = 1.0;
    private static final float  REGEN_LERP_SPEED   = 0.15f;
    private static final double REGEN_MIN_SPEED     = 0.045;

    private float damage = 8.0f;
    private UUID parentGolemUUID = null;

    public BlockProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public static BlockProjectile create(Level level, LivingEntity owner, BlockState blockState,
                                         float speed, float damage, Vec3 targetPos) {
        BlockProjectile proj = new BlockProjectile(EntityRegistry.BLOCK_PROJECTILE.get(), level);
        proj.setOwner(owner);
        proj.setBlockState(blockState);
        proj.damage = damage;

        Vec3 look = owner.getLookAngle();
        proj.setPos(owner.getX() + look.x * 0.6,
                    owner.getEyeY() - 0.15,
                    owner.getZ() + look.z * 0.6);

        Vec3 from = new Vec3(proj.getX(), proj.getY(), proj.getZ());
        Vec3 dir  = targetPos.subtract(from).normalize();
        proj.setDeltaMovement(dir.x * speed, dir.y * speed + 0.12, dir.z * speed);

        return proj;
    }

    public static BlockProjectile createRegenBlock(Level level, GolemEntity golem,
                                                    double bx, double by, double bz,
                                                    BlockState state) {
        BlockProjectile e = new BlockProjectile(EntityRegistry.BLOCK_PROJECTILE.get(), level);
        e.setPos(bx, by, bz);
        e.setBlockState(state);
        e.setRegenMode(true);
        e.setParentGolemId(golem.getId());
        e.parentGolemUUID = golem.getUUID();
        e.setNoGravity(true);
        e.noPhysics = true;
        return e;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BLOCK_STATE,     Blocks.DIRT.defaultBlockState());
        builder.define(REGEN_MODE,      false);
        builder.define(PARENT_GOLEM_ID, -1);
    }

    public BlockState getBlockState()              { return this.entityData.get(BLOCK_STATE);     }
    public void       setBlockState(BlockState s)  { this.entityData.set(BLOCK_STATE, s);         }
    public boolean    isRegenMode()                { return this.entityData.get(REGEN_MODE);      }
    public void       setRegenMode(boolean v)      { this.entityData.set(REGEN_MODE, v);          }
    public int        getParentGolemId()           { return this.entityData.get(PARENT_GOLEM_ID); }
    public void       setParentGolemId(int id)     { this.entityData.set(PARENT_GOLEM_ID, id);    }

    private static final int MAX_LIFETIME = 100; // 5 s

    @Override
    public void tick() {
        if (this.tickCount >= MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (isRegenMode()) {
            tickRegen();
        } else {
            super.tick();
        }

        if (this.level().isClientSide && this.isAlive()) {
            spawnTrailParticles();
        }
    }

    private void tickRegen() {
        this.baseTick();

        if (this.level().isClientSide) return;

        Entity parent = this.level().getEntity(getParentGolemId());

        if (!(parent instanceof GolemEntity) && parentGolemUUID != null
                && this.level() instanceof ServerLevel sl) {
            parent = sl.getEntity(parentGolemUUID);
            if (parent instanceof GolemEntity g) setParentGolemId(g.getId());
        }

        if (!(parent instanceof GolemEntity golem) || !golem.isAlive()) {
            this.discard();
            return;
        }

        double yRad    = Math.toRadians(golem.getYRot());
        double targetX = golem.getX() - Math.sin(yRad) * 0.6 - Math.cos(yRad) * 0.5;
        double targetY = golem.getY() + 2.2;
        double targetZ = golem.getZ() + Math.cos(yRad) * 0.6 - Math.sin(yRad) * 0.5;

        Vec3 target  = new Vec3(targetX, targetY, targetZ);
        Vec3 current = this.position();
        double dist  = current.distanceTo(target);

        if (dist < REGEN_ARRIVAL_DIST) {
            golem.onRegenBlockArrived();
            this.discard();
            return;
        }

        Vec3 lerped  = current.lerp(target, REGEN_LERP_SPEED);
        Vec3 delta   = lerped.subtract(current);
        double moved = delta.length();

        Vec3 newPos;
        if (moved < REGEN_MIN_SPEED && dist > REGEN_MIN_SPEED) {
            Vec3 dir = target.subtract(current).normalize();
            newPos   = current.add(dir.scale(REGEN_MIN_SPEED));
            delta    = newPos.subtract(current);
        } else {
            newPos = lerped;
        }

        this.setDeltaMovement(delta);
        this.setPos(newPos.x, newPos.y, newPos.z);
    }

    private void spawnTrailParticles() {
        BlockParticleOption particle = new BlockParticleOption(
                ParticleTypes.BLOCK, Blocks.PACKED_MUD.defaultBlockState());

        Vec3   motion     = this.getDeltaMovement();
        int    count      = isRegenMode() ? 2 : 5;
        double trailScale = isRegenMode() ? 0.5 : 0.65;
        double spread     = isRegenMode() ? 0.12 : 0.18;

        for (int i = 0; i < count; i++) {
            double ox = (this.random.nextDouble() - 0.5) * spread;
            double oy = (this.random.nextDouble() - 0.5) * spread;
            double oz = (this.random.nextDouble() - 0.5) * spread;

            this.level().addParticle(particle,
                    this.getX() - motion.x * trailScale + ox,
                    this.getY() - motion.y * trailScale + oy,
                    this.getZ() - motion.z * trailScale + oz,
                    -motion.x * 0.25 + ox * 0.15,
                    -motion.y * 0.25 + oy * 0.15 - 0.03,
                    -motion.z * 0.25 + oz * 0.15);
        }
    }

    private static final byte EVENT_IMPACT_EXPLODE = 60;

    @Override
    protected void onHit(HitResult result) {
        if (!isRegenMode()) {
            this.level().broadcastEntityEvent(this, EVENT_IMPACT_EXPLODE);
        }
        super.onHit(result);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_IMPACT_EXPLODE && this.level().isClientSide) {
            BlockParticleOption particle = new BlockParticleOption(
                    ParticleTypes.BLOCK, getBlockState());
            double cx = getX();
            double cy = getY() + getBbHeight() * 0.5;
            double cz = getZ();

            for (int i = 0; i < 80; i++) {
                double vx = (this.random.nextDouble() * 2.0 - 1.0) * 0.8;
                double vy = (this.random.nextDouble() * 2.0 - 1.0) * 0.8;
                double vz = (this.random.nextDouble() * 2.0 - 1.0) * 0.8;
                this.level().addParticle(particle, cx, cy, cz, vx, vy, vz);
            }
            for (int i = 0; i < 20; i++) {
                double vx = (this.random.nextDouble() * 2.0 - 1.0) * 1.4;
                double vy = (this.random.nextDouble() * 2.0 - 1.0) * 1.4;
                double vz = (this.random.nextDouble() * 2.0 - 1.0) * 1.4;
                this.level().addParticle(particle, cx, cy, cz, vx, vy, vz);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide || isRegenMode()) return;

        if (result.getEntity() instanceof LivingEntity target) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), this.damage);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", this.damage);
        if (parentGolemUUID != null) tag.putUUID("ParentGolem", parentGolemUUID);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Damage"))     this.damage = tag.getFloat("Damage");
        if (tag.hasUUID("ParentGolem")) parentGolemUUID = tag.getUUID("ParentGolem");
    }
}
