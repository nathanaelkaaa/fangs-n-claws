package net.raptorzizi.fangs_n_claws.entity.hyena;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.wild_wolf.WildWolfEntity;

public class HyenaEntity extends WildWolfEntity {

    public HyenaEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Rabbit.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Camel.class, false));
    }
}
