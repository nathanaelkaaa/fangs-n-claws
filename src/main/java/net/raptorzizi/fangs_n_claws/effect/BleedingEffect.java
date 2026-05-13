package net.raptorzizi.fangs_n_claws.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.registries.ParticlesRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BleedingEffect extends MobEffect {

    public static final ResourceKey<DamageType> DAMAGE_TYPE =
            ResourceKey.create(Registries.DAMAGE_TYPE, FangsClawsMod.id("bleeding"));

    private static final int    SNEAK_TICKS_TO_REMOVE = 40;
    private static final double MOVEMENT_THRESHOLD    = 0.04;
    private static final float  MOVEMENT_DAMAGE       = 1.0f;
    private static final double SLOWNESS_AMOUNT       = -0.35;
    private static final int    AMBIENT_PARTICLE_INTERVAL = 4;
    private static final int SPLASH_DAMAGE_INTERVAL = 10;

    public static final Set<UUID> PENDING_REMOVAL = ConcurrentHashMap.newKeySet();
    public static final Set<UUID> BLEEDING_DAMAGE_ACTIVE = ConcurrentHashMap.newKeySet();

    private final Map<UUID, Vec3>    lastPos     = new HashMap<>();
    private final Map<UUID, Integer> sneakTicks  = new HashMap<>();
    private final Map<UUID, Integer> damageTicks = new HashMap<>();

    public BleedingEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(FangsClawsMod.MOD_ID, "bleeding_slowness"),
                SLOWNESS_AMOUNT,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return true;
        UUID uuid = entity.getUUID();
        ServerLevel serverLevel = (ServerLevel) entity.level();

        if (entity.isCrouching()) {
            int ticks = sneakTicks.getOrDefault(uuid, 0) + 1;
            sneakTicks.put(uuid, ticks);
            if (ticks >= SNEAK_TICKS_TO_REMOVE) {
                PENDING_REMOVAL.add(uuid);
                cleanup(uuid);
                return true;
            }
        } else {
            sneakTicks.put(uuid, 0);
        }

        if (!PENDING_REMOVAL.contains(uuid)) {
            if (entity.tickCount % AMBIENT_PARTICLE_INTERVAL == 0) {
                spawnAmbientBlood(serverLevel, entity);
            }

            Vec3 prev = lastPos.get(uuid);
            Vec3 curr = entity.position();

            if (prev != null) {
                double dx   = curr.x - prev.x;
                double dz   = curr.z - prev.z;
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > MOVEMENT_THRESHOLD) {
                    BLEEDING_DAMAGE_ACTIVE.add(uuid);
                    entity.hurt(bleedingDamage(serverLevel), MOVEMENT_DAMAGE);
                    BLEEDING_DAMAGE_ACTIVE.remove(uuid);
                    int dmgTick = damageTicks.getOrDefault(uuid, 0) + 1;
                    damageTicks.put(uuid, dmgTick);
                    if (dmgTick >= SPLASH_DAMAGE_INTERVAL) {
                        spawnDamageSplash(serverLevel, entity);
                        damageTicks.put(uuid, 0);
                    }
                }
            }

            lastPos.put(uuid, curr);
        }

        return true;
    }

    private void spawnAmbientBlood(ServerLevel level, LivingEntity entity) {
        double halfW = entity.getBbWidth() * 0.5;
        double x  = entity.getX() + (level.random.nextDouble() - 0.5) * halfW * 1.5;
        double y  = entity.getY() + entity.getBbHeight() * (0.6 + level.random.nextDouble() * 0.3);
        double z  = entity.getZ() + (level.random.nextDouble() - 0.5) * halfW * 1.5;
        double vx = (level.random.nextDouble() - 0.5) * 0.08;
        double vy = level.random.nextDouble() * 0.04;
        double vz = (level.random.nextDouble() - 0.5) * 0.08;
        level.sendParticles(ParticlesRegistry.BLOOD_PARTICLE.get(), x, y, z, 0, vx, vy, vz, 1.0);
    }

    private void spawnDamageSplash(ServerLevel level, LivingEntity entity) {
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.5;
        double z = entity.getZ();
        for (int i = 0; i < 3; i++) {
            double vx = (level.random.nextDouble() - 0.5) * 0.35;
            double vy = level.random.nextDouble() * 0.25 + 0.05;
            double vz = (level.random.nextDouble() - 0.5) * 0.35;
            level.sendParticles(ParticlesRegistry.BLOOD_PARTICLE.get(), x, y, z, 0, vx, vy, vz, 1.0);
        }
    }

    private static DamageSource bleedingDamage(ServerLevel level) {
        return new DamageSource(
                level.registryAccess()
                     .registryOrThrow(Registries.DAMAGE_TYPE)
                     .getHolderOrThrow(DAMAGE_TYPE)
        );
    }

    public void cleanup(UUID uuid) {
        lastPos.remove(uuid);
        sneakTicks.remove(uuid);
        damageTicks.remove(uuid);
    }
}
