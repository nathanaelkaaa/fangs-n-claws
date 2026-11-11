package net.raptorzizi.fangs_n_claws.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class FluidRegistry {

    // Flow
    private static final int ACID_SLOPE_FIND_DISTANCE = 2;
    private static final int ACID_LEVEL_DECREASE = 2;
    private static final int ACID_TICK_RATE = 30;

    // Appearance
    private static final int ACID_LIGHT_LEVEL = 15;
    private static final int ACID_DENSITY = 2400;
    private static final int ACID_VISCOSITY = 4000;
    private static final int ACID_TEMPERATURE = 300;

    // Physics
    private static final double ACID_MOTION_SCALE = 0.0023333333333333335;
    private static final float ACID_EXPLOSION_RESISTANCE = 100.0F;
    private static final double ITEM_DRAG = 0.95;
    private static final double ITEM_FLOAT_THRESHOLD = 0.06;
    private static final double ITEM_FLOAT_BOOST = 5.0E-4;

    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, FangsClawsMod.MOD_ID);

    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, FangsClawsMod.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> ACID_TYPE =
            FLUID_TYPES.register("acid", () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.fangs_n_claws.acid")
                    .canSwim(false)
                    .canDrown(false)
                    .canHydrate(false)
                    .supportsBoating(false)
                    .pathType(PathType.DAMAGE_OTHER)
                    .adjacentPathType(PathType.DANGER_OTHER)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .lightLevel(ACID_LIGHT_LEVEL)
                    .density(ACID_DENSITY)
                    .viscosity(ACID_VISCOSITY)
                    .temperature(ACID_TEMPERATURE)) {

                @Override
                public double motionScale(Entity entity) {
                    return ACID_MOTION_SCALE;
                }

                @Override
                public void setItemMovement(ItemEntity entity) {
                    Vec3 motion = entity.getDeltaMovement();
                    entity.setDeltaMovement(
                            motion.x * ITEM_DRAG,
                            motion.y + (motion.y < ITEM_FLOAT_THRESHOLD ? ITEM_FLOAT_BOOST : 0.0),
                            motion.z * ITEM_DRAG);
                }
            });

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> ACID =
            FLUIDS.register("acid", () -> new BaseFlowingFluid.Source(FluidRegistry.ACID_PROPERTIES));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_ACID =
            FLUIDS.register("flowing_acid", () -> new BaseFlowingFluid.Flowing(FluidRegistry.ACID_PROPERTIES));

    private static final BaseFlowingFluid.Properties ACID_PROPERTIES =
            new BaseFlowingFluid.Properties(ACID_TYPE, ACID, FLOWING_ACID)
                    .block(BlocksRegistry.ACID)
                    .bucket(ItemsRegistry.ACID_BUCKET)
                    .slopeFindDistance(ACID_SLOPE_FIND_DISTANCE)
                    .levelDecreasePerBlock(ACID_LEVEL_DECREASE)
                    .tickRate(ACID_TICK_RATE)
                    .explosionResistance(ACID_EXPLOSION_RESISTANCE);

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }
}
