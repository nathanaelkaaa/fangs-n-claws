package net.raptorzizi.fangs_n_claws.item.amulet;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

@EventBusSubscriber(modid = FangsClawsMod.MOD_ID)
public final class AmuletEvents {

    private static final String DEAD_FAMILIARS_TAG = "FangsDeadFamiliars";

    private AmuletEvents() { }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof AmuletItem)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;

        AmuletItem.capture(stack, event.getEntity(), target, event.getHand());

        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFamiliarDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof OwnableEntity ownable)) return;

        UUID ownerId = ownable.getOwnerUUID();
        if (ownerId == null) return;

        Player owner = event.getEntity().level().getPlayerByUUID(ownerId);
        if (owner == null) return;

        noteDeath(owner, event.getEntity().getUUID());
    }

    private static void noteDeath(Player owner, UUID familiar) {
        CompoundTag persisted = persistentData(owner);
        ListTag deaths = persisted.getList(DEAD_FAMILIARS_TAG, Tag.TAG_INT_ARRAY);
        deaths.add(NbtUtils.createUUID(familiar));
        persisted.put(DEAD_FAMILIARS_TAG, deaths);
    }

    static boolean consumeDeath(Player owner, UUID familiar) {
        CompoundTag persisted = persistentData(owner);
        if (!persisted.contains(DEAD_FAMILIARS_TAG)) return false;

        ListTag deaths = persisted.getList(DEAD_FAMILIARS_TAG, Tag.TAG_INT_ARRAY);
        for (int i = 0; i < deaths.size(); i++) {
            if (!NbtUtils.loadUUID(deaths.get(i)).equals(familiar)) continue;

            deaths.remove(i);
            if (deaths.isEmpty()) {
                persisted.remove(DEAD_FAMILIARS_TAG);
            }
            return true;
        }
        return false;
    }

    private static CompoundTag persistentData(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) {
            root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }
}
