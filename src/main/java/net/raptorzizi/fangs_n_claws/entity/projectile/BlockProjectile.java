package net.raptorzizi.fangs_n_claws.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BlockProjectile extends ThrowableProjectile {

    // Variables

    private static final EntityDataAccessor<BlockState> BLOCK_STATE     = SynchedEntityData.defineId(BlockProjectile.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Boolean>    REGEN_MODE      = SynchedEntityData.defineId(BlockProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer>    PARENT_GOLEM_ID = SynchedEntityData.defineId(BlockProjectile.class, EntityDataSerializers.INT);

    private static final double REGEN_ARRIVAL_DIST  = 1.0;
    private static final float  REGEN_LERP_SPEED    = 0.15f;
    private static final double REGEN_MIN_SPEED     = 0.045;
    private static final int    MAX_LIFETIME        = 100;
    private static final byte   EVENT_IMPACT_EXPLODE = 60;

    private static final float MUD_RADIUS   = 1.5f;
    private static final int   MUD_ATTEMPTS = 40;

    private float      damage          = 8.0f;
    private UUID       parentGolemUUID = null;
    private final Set<UUID> hitEntities = new HashSet<>();

    // Spawn

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
    protected void defineSynchedData() {
        this.entityData.define(BLOCK_STATE,      Blocks.DIRT.defaultBlockState());
        this.entityData.define(REGEN_MODE,       false);
        this.entityData.define(PARENT_GOLEM_ID, -1);
    }

    public BlockState getBlockState()             { return this.entityData.get(BLOCK_STATE);     }
    public void       setBlockState(BlockState s) { this.entityData.set(BLOCK_STATE, s);         }
    public boolean    isRegenMode()               { return this.entityData.get(REGEN_MODE);      }
    public void       setRegenMode(boolean v)     { this.entityData.set(REGEN_MODE, v);          }
    public int        getParentGolemId()          { return this.entityData.get(PARENT_GOLEM_ID); }
    public void       setParentGolemId(int id)    { this.entityData.set(PARENT_GOLEM_ID, id);    }

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

    // Tick

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

    // Combat

    @Override
    protected void onHit(HitResult result) {
        if (isRegenMode()) {
            super.onHit(result);
            return;
        }
        if (result.getType() == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult) result);
        } else if (result.getType() == HitResult.Type.BLOCK) {
            this.level().broadcastEntityEvent(this, EVENT_IMPACT_EXPLODE);
            onHitBlock((BlockHitResult) result);
            this.discard();
        }
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
        if (this.level().isClientSide || isRegenMode()) return;

        Entity entity = result.getEntity();
        if (!(entity instanceof LivingEntity target)) return;
        if (target == this.getOwner()) return;
        if (hitEntities.contains(target.getUUID())) return;

        hitEntities.add(target.getUUID());

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundsRegistry.ROCK_IMPACT.get(), SoundSource.NEUTRAL,
                1.2F, 0.85F + this.random.nextFloat() * 0.3F);

        target.hurt(this.level().damageSources().thrown(this, this.getOwner()), this.damage);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.level().isClientSide || isRegenMode()) return;

        ServerLevel serverLevel = (ServerLevel) this.level();
        Vec3 pos = result.getLocation();

        serverLevel.playSound(null, pos.x, pos.y, pos.z,
                SoundsRegistry.ROCK_IMPACT.get(), SoundSource.NEUTRAL,
                1.4F, 0.75F + this.random.nextFloat() * 0.25F);
        int target  = 2 + this.random.nextInt(4);
        BlockPos center = BlockPos.containing(pos);
        int placed   = 0;
        int attempts = 0;

        while (placed < target && attempts < MUD_ATTEMPTS) {
            attempts++;
            double ox = (this.random.nextDouble() * 2.0 - 1.0) * MUD_RADIUS;
            double oy = (this.random.nextDouble() * 2.0 - 1.0) * MUD_RADIUS;
            double oz = (this.random.nextDouble() * 2.0 - 1.0) * MUD_RADIUS;
            if (ox * ox + oy * oy + oz * oz > MUD_RADIUS * MUD_RADIUS) continue;

            BlockPos candidate = center.offset(
                    (int) Math.round(ox), (int) Math.round(oy), (int) Math.round(oz));
            BlockState state = serverLevel.getBlockState(candidate);
            if (state.isAir() || state.canBeReplaced()) {
                serverLevel.setBlock(candidate, Blocks.PACKED_MUD.defaultBlockState(), 3);
                placed++;
            }
        }
    }
}
