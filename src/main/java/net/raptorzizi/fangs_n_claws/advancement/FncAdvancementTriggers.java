package net.raptorzizi.fangs_n_claws.advancement;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;
import net.raptorzizi.fangs_n_claws.entity.horse.HorseMob;
import net.raptorzizi.fangs_n_claws.entity.scorpion.ScorpionEntity;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;

@Mod.EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public final class FncAdvancementTriggers {

    private FncAdvancementTriggers() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;

        DamageSource source = event.getSource();
        EntityType<?> vt = victim.getType();

        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        ItemStack weapon = player.getMainHandItem();

        if (vt == EntityRegistry.WEREWOLF.get()
                && weapon.is(ItemsRegistry.SILVER_SWORD.get())) {
            FncAdvancements.grant(player, "hunt/silver_curse");
        }
        if (vt == EntityRegistry.MIMIC.get() && weapon.getItem() instanceof AxeItem) {
            FncAdvancements.grant(player, "hunt/chest_logger");
        }
        if ((vt == EntityRegistry.GOLEM.get() || vt == EntityRegistry.ICE_GOLEM.get())
                && weapon.getItem() instanceof ShovelItem) {
            FncAdvancements.grant(player, "hunt/earthmover");
        }

        if (victim.hasEffect(MobEffectsRegistry.VENOM.get())) {
            FncAdvancements.grant(player, "hunt/poisoner");
        }

        if (vt == EntityRegistry.HELL_OGRE.get()
                && player.getHealth() >= player.getMaxHealth()) {
            FncAdvancements.grant(player, "hunt/without_a_scratch");
        }

        if (source.getDirectEntity() != null
                && source.getDirectEntity().getType() == EntityRegistry.EVIL_EYE_PROJECTILE.get()
                && (vt == EntityType.PHANTOM || vt == EntityRegistry.EVIL_BAT.get())) {
            FncAdvancements.grant(player, "hunt/pest_control");
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 10 != 0) return;

        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            if (vehicle instanceof ScorpionEntity) {
                FncAdvancements.grant(player, "taming/desert_rider");
            }

            if (vehicle instanceof net.raptorzizi.fangs_n_claws.entity.nightmare_horse.NightmareHorseEntity nh
                    && player.level().dimension() == Level.NETHER
                    && player.isSprinting()
                    && nh.getStamina() > 0.0F) {
                FncAdvancements.grant(player, "taming/night_ride");
            }
            if (vehicle instanceof HorseMob bat
                    && bat.getType() == EntityRegistry.HORSE_BAT.get()
                    && bat.isTamed()
                    && isAtLeastBlocksAboveGround(player, 15)) {
                FncAdvancements.grant(player, "taming/lent_wings");
            }
            if (vehicle instanceof HorseMob horse && horse.isWearingArmor()) {
                FncAdvancements.grant(player, "taming/warm_blanket");
            }
        }

        if (player.isFallFlying()
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ItemsRegistry.SHRIKE_CHESTPLATE.get())) {
            FncAdvancements.grant(player, "forge/wind_master");
        }
    }

    private static boolean isAtLeastBlocksAboveGround(Player player, int minBlocks) {
        Level level = player.level();
        net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos();
        int x = player.getBlockX();
        int z = player.getBlockZ();
        int feetY = net.minecraft.util.Mth.floor(player.getY());
        for (int i = 1; i <= minBlocks; i++) {
            pos.set(x, feetY - i, z);
            if (level.getBlockState(pos).blocksMotion()) return false;
        }
        return true;
    }
}
