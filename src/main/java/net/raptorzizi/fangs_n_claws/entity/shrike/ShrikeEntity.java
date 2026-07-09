package net.raptorzizi.fangs_n_claws.entity.shrike;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.projectile.FeatherProjectileEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearAttackGoal;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearEntity;
import net.raptorzizi.fangs_n_claws.entity.owlbear.OwlbearFlyingAttackGoal;

public class ShrikeEntity extends OwlbearEntity {

    public ShrikeEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeAllGoals(g -> g instanceof OwlbearFlyingAttackGoal || g instanceof OwlbearAttackGoal);
        this.goalSelector.addGoal(1, new ShrikeBlizzardGoal(this));
        this.goalSelector.addGoal(2, new ShrikeFeatherAttackGoal(this));
        this.goalSelector.addGoal(3, new OwlbearAttackGoal(this));
    }

    @Override
    protected boolean useAttack1Melee() {
        return false;

    }

    private static final double FEATHER_FAN = Math.toRadians(50.0);

    public void shootFeathers(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double originY = this.getY() + this.getBbHeight() * 0.7;
        Vec3 toTarget = new Vec3(target.getX() - this.getX(),
                                 target.getY(0.5) - originY,
                                 target.getZ() - this.getZ());
        double horiz    = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        double baseYaw  = Math.atan2(toTarget.z, toTarget.x);
        double pitch    = Math.atan2(toTarget.y, horiz);
        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);

        int count = 5 + this.random.nextInt(4);
        for (int i = 0; i < count; i++) {
            double t   = count == 1 ? 0.5 : (double) i / (count - 1);
            double yaw = baseYaw + (t - 0.5) * FEATHER_FAN;

            double dirX = cosPitch * Math.cos(yaw);
            double dirY = sinPitch;
            double dirZ = cosPitch * Math.sin(yaw);

            FeatherProjectileEntity feather = new FeatherProjectileEntity(this.level(), this);
            feather.setPos(this.getX(), originY, this.getZ());
            feather.shoot(dirX, dirY, dirZ, 2.6f, 0.5f);
            serverLevel.addFreshEntity(feather);
        }

        this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 0.8F + this.random.nextFloat() * 0.2F);
    }

    @Override
    public String textureBaseName() {
        return "shrike";
    }

    @Override
    public ResourceLocation eyesTexture() {
        return FangsClawsMod.id("textures/entity/glowing_eyes/shrike_eyes.png");
    }
}
