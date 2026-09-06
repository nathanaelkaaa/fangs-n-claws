package net.raptorzizi.fangs_n_claws.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.raptorzizi.fangs_n_claws.entity.carnivorous_plant.CarnivorousPlantSproutEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.SoundsRegistry;
import org.jetbrains.annotations.NotNull;

public class CarnivorousPlantSproutItem extends Item {

    public CarnivorousPlantSproutItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (context.getClickedFace() != Direction.UP) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos().above();
        if (!level.getBlockState(pos).canBeReplaced()) return InteractionResult.PASS;

        if (level instanceof ServerLevel server) {
            CarnivorousPlantSproutEntity plant = EntityRegistry.CARNIVOROUS_PLANT_SPROUT.get().create(server);
            if (plant == null) return InteractionResult.PASS;

            Player player = context.getPlayer();
            plant.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    player == null ? 0.0F : player.getYRot(), 0.0F);
            plant.lockYaw(player == null ? 0.0F : player.getYRot());
            plant.finalizeSpawn(server, server.getCurrentDifficultyAt(pos),
                    MobSpawnType.MOB_SUMMONED, null);
            plant.setPersistenceRequired();
            server.addFreshEntity(plant);

            level.playSound(null, pos, SoundsRegistry.CARNIVOROUS_PLANT_OPEN.get(),
                    SoundSource.BLOCKS, 1.0F, 1.0F);

            ItemStack stack = context.getItemInHand();
            if (player == null || !player.getAbilities().instabuild) stack.shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
