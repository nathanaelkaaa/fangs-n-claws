package net.raptorzizi.fangs_n_claws.entity.ice_golem;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.raptorzizi.fangs_n_claws.entity.golem.GolemEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IceGolemEntity extends GolemEntity {

    private static final Set<Block> ICE_REGEN_BLOCKS = Set.of(
            Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.SNOW_BLOCK
    );
    private static final String FISH_TAG = "FrozenFish";

    private static final EntityDataAccessor<CompoundTag> FISH_DATA =
            SynchedEntityData.defineId(IceGolemEntity.class, EntityDataSerializers.COMPOUND_TAG);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private boolean isUppercutAttack = false;

    private List<ItemStack> frozenFish = new ArrayList<>();
    private transient List<ItemStack> clientFishCache = null;

    public IceGolemEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FISH_DATA, new CompoundTag());
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (FISH_DATA.equals(key)) clientFishCache = null;
    }

    public List<ItemStack> getFrozenFish() {
        if (!this.level().isClientSide()) return frozenFish;
        if (clientFishCache == null) {
            clientFishCache = new ArrayList<>();
            ListTag list = this.entityData.get(FISH_DATA).getList("l", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                ItemStack stack = ItemStack.of(list.getCompound(i));
                if (!stack.isEmpty()) clientFishCache.add(stack);
            }
        }
        return clientFishCache;
    }

    private void syncFishData() {
        ListTag list = new ListTag();
        for (ItemStack stack : frozenFish) {
            list.add(stack.save(new CompoundTag()));
        }
        CompoundTag tag = new CompoundTag();
        tag.put("l", list);
        this.entityData.set(FISH_DATA, tag);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level,
                                                  @NotNull DifficultyInstance difficulty,
                                                  @NotNull MobSpawnType spawnType,
                                                  @Nullable SpawnGroupData spawnGroupData,
                                                  @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);
        initFrozenFish();
        return data;
    }

    private void initFrozenFish() {
        frozenFish.clear();
        int count = 1 + this.random.nextInt(4);
        for (int i = 0; i < count; i++) {
            float r = this.random.nextFloat();
            if (r < 0.45f)      frozenFish.add(new ItemStack(Items.COD));
            else if (r < 0.90f) frozenFish.add(new ItemStack(Items.SALMON));
            else                frozenFish.add(new ItemStack(Items.PUFFERFISH));
        }
        syncFishData();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag list = new ListTag();
        for (ItemStack stack : frozenFish) {
            list.add(stack.save(new CompoundTag()));
        }
        tag.put(FISH_TAG, list);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        frozenFish.clear();
        if (tag.contains(FISH_TAG, Tag.TAG_LIST)) {
            ListTag list = tag.getList(FISH_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                ItemStack stack = ItemStack.of(list.getCompound(i));
                if (!stack.isEmpty()) frozenFish.add(stack);
            }
        }
        syncFishData();
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        for (ItemStack stack : frozenFish) {
            spawnAtLocation(stack);
        }
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        if (this.random.nextInt(3) == 0) {
            this.triggerAnim("events_controller", "attack_uppercut");
            this.isUppercutAttack = true;
        } else {
            this.triggerAnim("events_controller", "attack");
            this.isUppercutAttack = false;
        }
        this.pendingAttackTarget = target;
        this.attackDelayTick     = 1;
        return true;
    }

    @Override
    protected void onAttackLanded(Entity target) {
        if (!isUppercutAttack) return;
        isUppercutAttack = false;
        double dx = Mth.sin(this.getYRot() * (float) (Math.PI / 180.0));
        double dz = -Mth.cos(this.getYRot() * (float) (Math.PI / 180.0));
        if (target instanceof LivingEntity le) {
            le.knockback(0.5, dx, dz);
        }
        target.setDeltaMovement(
                target.getDeltaMovement().x,
                1.3,
                target.getDeltaMovement().z);
        target.hurtMarked = true;
    }

    @Override
    protected Set<Block> getRegenBlocks() {
        return ICE_REGEN_BLOCKS;
    }

    @Override
    protected BlockState getBodyBlockState() {
        return Blocks.PACKED_ICE.defaultBlockState();
    }

    @Override
    protected void spawnBlockPile() {
        ServerLevel serverLevel = (ServerLevel) this.level();
        BlockPos center = this.blockPosition();
        for (int dy = 0; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    float e = (dx * dx + dz * dz) / 6.25f + (dy * dy) / 2.25f;
                    if (e > 1.0f) continue;
                    if (this.random.nextBoolean()) continue;
                    if (e > 0.55f && this.random.nextFloat() > 0.55f) continue;
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (serverLevel.getBlockState(pos).canBeReplaced()) {
                        serverLevel.setBlock(pos, Blocks.SNOW_BLOCK.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
