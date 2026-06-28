package net.raptorzizi.fangs_n_claws.entity.horse;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ChargeAbility {

    public static final int IDLE = 0, WINDUP = 1, CHARGE = 2;

    private final HorseMob horse;

    public final double speed;
    public final double backupSpeed;
    public final double engageRange;
    public final double knockback;
    public final int    cooldownTicks;
    public final int    windupTicks;
    public final int    chargeTicks;
    public final int    fireSeconds;

    @Nullable private final Runnable onWindupTick;
    @Nullable private final Runnable onChargeTick;

    private int  phase    = IDLE;
    private Vec3 dir       = Vec3.ZERO;
    private int  cooldown  = 0;
    private int  windup    = 0;
    private int  charge    = 0;
    private final Set<Integer> hits = new HashSet<>();

    public ChargeAbility(HorseMob horse, double speed, double backupSpeed, int cooldownTicks,
                         int windupTicks, int chargeTicks, double engageRange, double knockback,
                         int fireSeconds, @Nullable Runnable onWindupTick, @Nullable Runnable onChargeTick) {
        this.horse = horse;
        this.speed = speed;
        this.backupSpeed = backupSpeed;
        this.cooldownTicks = cooldownTicks;
        this.windupTicks = windupTicks;
        this.chargeTicks = chargeTicks;
        this.engageRange = engageRange;
        this.knockback = knockback;
        this.fireSeconds = fireSeconds;
        this.onWindupTick = onWindupTick;
        this.onChargeTick = onChargeTick;
    }

    public int  phase()    { return phase; }
    public Vec3 dir()      { return dir; }
    public int  cooldown() { return cooldown; }

    public void tickCooldown() {
        if (cooldown > 0) cooldown--;
    }

    public void startWindup() {
        this.phase = WINDUP;
        this.windup = windupTicks;
        horse.triggerAnim("attack_controller", "rear");
        horse.playSound(SoundEvents.HORSE_ANGRY, 1.2F, 0.6F);
    }

    public void tickWindup(@Nullable LivingEntity target) {
        if (onWindupTick != null) onWindupTick.run();
        if (--windup <= 0) {
            Vec3 d = (target != null)
                    ? new Vec3(target.getX() - horse.getX(), 0.0, target.getZ() - horse.getZ())
                    : horse.getLookAngle();
            d = new Vec3(d.x, 0.0, d.z);
            this.dir = d.lengthSqr() < 1.0E-4 ? horse.getLookAngle().multiply(1, 0, 1).normalize() : d.normalize();
            this.phase = CHARGE;
            this.charge = chargeTicks;
            this.hits.clear();
            horse.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.2F);
        }
    }

    public void tickCharge() {
        if (onChargeTick != null) onChargeTick.run();
        applyHits();
        if (--charge <= 0) {
            end();
        }
    }

    public void end() {
        if (this.phase != IDLE) {
            this.cooldown = cooldownTicks;
        }
        this.phase = IDLE;
        this.windup = 0;
        this.charge = 0;
        this.hits.clear();
    }

    private void applyHits() {
        AABB box = horse.getBoundingBox().inflate(0.3);
        float dmg = (float) horse.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity victim : horse.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != horse && e.isAlive() && !horse.hasPassenger(e))) {
            if (!hits.add(victim.getId())) continue;
            victim.hurt(horse.damageSources().mobAttack(horse), dmg);
            if (fireSeconds > 0) victim.igniteForSeconds(fireSeconds);
            victim.knockback(knockback, -dir.x, -dir.z);
            victim.hurtMarked = true;
            if (victim instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
            }
        }
    }
}
