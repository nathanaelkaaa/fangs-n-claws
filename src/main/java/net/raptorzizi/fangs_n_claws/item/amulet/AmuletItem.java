package net.raptorzizi.fangs_n_claws.item.amulet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AmuletItem extends Item {

    // Familiar storage
    public static final String FAMILIAR_TAG = "Familiar";
    public static final String LINK_TAG = "FamiliarLink";
    public static final String NAME_TAG = "FamiliarName";
    public static final String DEAD_TAG = "FamiliarDead";

    // Model states
    public static final float STATE_EMPTY = 0.0F;
    public static final float STATE_FULL = 1.0F;
    public static final float STATE_DEAD = 2.0F;

    // Feedback
    private static final int CAPTURE_PARTICLES = 24;
    private static final int REFUSE_PARTICLES = 8;
    private static final double PARTICLE_SPREAD = 0.4;
    private static final double PARTICLE_SPEED = 0.0;
    private static final float SOUND_VOLUME = 0.8F;
    private static final float CAPTURE_PITCH = 1.2F;
    private static final float RELEASE_PITCH = 0.9F;
    private static final float REFUSE_PITCH = 0.6F;
    private static final double HALF_BLOCK = 0.5;

    private final AmuletCategory category;
    private final boolean jade;

    public AmuletItem(AmuletCategory category, boolean jade) {
        super(new Item.Properties().stacksTo(1));
        this.category = category;
        this.jade = jade;
    }

    public AmuletCategory category() {
        return this.category;
    }

    public boolean isJade() {
        return this.jade;
    }

    // State

    public static CompoundTag data(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private static void setData(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean holdsFamiliar(ItemStack stack) {
        return data(stack).contains(FAMILIAR_TAG);
    }

    public static boolean isLinked(ItemStack stack) {
        return data(stack).hasUUID(LINK_TAG);
    }

    public static boolean isDead(ItemStack stack) {
        return data(stack).getBoolean(DEAD_TAG);
    }

    public static float modelState(ItemStack stack) {
        if (isDead(stack)) return STATE_DEAD;
        return holdsFamiliar(stack) ? STATE_FULL : STATE_EMPTY;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        CompoundTag tag = data(stack);
        return tag.contains(FAMILIAR_TAG) || tag.hasUUID(LINK_TAG);
    }

    // Capture

    public static boolean capture(ItemStack stack, Player player, LivingEntity target,
                                  InteractionHand hand) {
        if (!(stack.getItem() instanceof AmuletItem amulet)) return false;

        if (!canCapture(amulet, stack, player, target)) {
            refuse(player, target);
            return false;
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            CompoundTag entityTag = new CompoundTag();
            if (!target.save(entityTag)) {
                refuse(player, target);
                return false;
            }

            CompoundTag tag = data(stack);
            tag.put(FAMILIAR_TAG, entityTag);
            tag.putString(NAME_TAG, target.getDisplayName().getString());
            if (amulet.jade) {
                tag.putUUID(LINK_TAG, target.getUUID());
            }
            setData(stack, tag);

            particles(serverLevel, target.position(), target.getBbHeight());
            sound(serverLevel, player, CAPTURE_PITCH);
            target.discard();
        }

        player.swing(hand);
        return true;
    }

    private static boolean canCapture(AmuletItem amulet, ItemStack stack, Player player,
                                      LivingEntity target) {
        if (isDead(stack) || holdsFamiliar(stack)) return false;
        if (!AmuletCategory.isFamiliarOf(player, target)) return false;
        if (!amulet.category.accepts(target)) return false;

        CompoundTag tag = data(stack);
        return !tag.hasUUID(LINK_TAG) || tag.getUUID(LINK_TAG).equals(target.getUUID());
    }

    // Release

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null || !holdsFamiliar(stack)) return InteractionResult.PASS;

        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        CompoundTag tag = data(stack);
        Optional<Entity> spawned = EntityType.create(tag.getCompound(FAMILIAR_TAG), serverLevel);
        if (spawned.isEmpty()) return InteractionResult.PASS;

        Entity familiar = spawned.get();
        familiar.moveTo(pos.getX() + HALF_BLOCK, pos.getY(), pos.getZ() + HALF_BLOCK,
                familiar.getYRot(), familiar.getXRot());
        if (familiar instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        serverLevel.addFreshEntity(familiar);

        tag.remove(FAMILIAR_TAG);
        if (!tag.hasUUID(LINK_TAG)) {
            tag.remove(NAME_TAG);
        }
        setData(stack, tag);

        particles(serverLevel, familiar.position(), familiar.getBbHeight());
        sound(serverLevel, player, RELEASE_PITCH);
        return InteractionResult.CONSUME;
    }

    // Death

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level,
                              @NotNull Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !this.jade) return;
        if (!(entity instanceof Player player)) return;

        CompoundTag tag = data(stack);
        if (!tag.hasUUID(LINK_TAG) || tag.getBoolean(DEAD_TAG)) return;

        UUID familiar = tag.getUUID(LINK_TAG);
        if (!AmuletEvents.consumeDeath(player, familiar)) return;

        tag.putBoolean(DEAD_TAG, true);
        tag.remove(FAMILIAR_TAG);
        setData(stack, tag);
    }

    // Naming

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        CompoundTag tag = data(stack);
        return tag.contains(NAME_TAG)
                ? Component.literal(tag.getString(NAME_TAG))
                : super.getName(stack);
    }

    // Tooltip

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag tag = data(stack);
        if (!tag.contains(NAME_TAG)) return;

        tooltip.add(Component.translatable(this.getDescriptionId()).withStyle(ChatFormatting.GRAY));

        if (tag.getBoolean(DEAD_TAG)) {
            tooltip.add(Component.translatable("tooltip.fangs_n_claws.amulet.dead")
                    .withStyle(ChatFormatting.DARK_RED));
        } else if (tag.contains(FAMILIAR_TAG)) {
            tooltip.add(Component.translatable("tooltip.fangs_n_claws.amulet.holding")
                    .withStyle(ChatFormatting.DARK_GREEN));
        } else {
            tooltip.add(Component.translatable("tooltip.fangs_n_claws.amulet.bound")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    // Feedback

    private static void refuse(Player player, LivingEntity target) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        serverLevel.sendParticles(ParticleTypes.SMOKE,
                target.getX(), target.getY() + target.getBbHeight(), target.getZ(),
                REFUSE_PARTICLES, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD,
                PARTICLE_SPEED);
        sound(serverLevel, player, REFUSE_PITCH);
    }

    private static void particles(ServerLevel level, Vec3 pos, float height) {
        level.sendParticles(ParticleTypes.PORTAL,
                pos.x, pos.y + height * HALF_BLOCK, pos.z,
                CAPTURE_PARTICLES, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD,
                PARTICLE_SPEED);
    }

    private static void sound(ServerLevel level, Player player, float pitch) {
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, SOUND_VOLUME, pitch);
    }
}
