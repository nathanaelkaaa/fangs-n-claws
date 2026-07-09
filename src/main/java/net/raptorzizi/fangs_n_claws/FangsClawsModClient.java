package net.raptorzizi.fangs_n_claws;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.ConfigScreenHandler;
import net.raptorzizi.fangs_n_claws.block.GhostBlock;
import net.raptorzizi.fangs_n_claws.client.FangsConfigScreen;
import net.raptorzizi.fangs_n_claws.client.OwlFlightState;
import net.raptorzizi.fangs_n_claws.entity.nightmare_horse.NightmareStaminaLayer;
import net.raptorzizi.fangs_n_claws.item.BlowgunItem;
import net.raptorzizi.fangs_n_claws.item.FangDaggerItem;
import net.raptorzizi.fangs_n_claws.item.armor.OwlArmorItem;
import net.raptorzizi.fangs_n_claws.item.armor.ShrikeArmorItem;
import net.raptorzizi.fangs_n_claws.network.FangsNetwork;
import net.raptorzizi.fangs_n_claws.network.OwlFlightPacket;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.List;


@Mod.EventBusSubscriber(modid = FangsClawsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FangsClawsModClient {

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(FangsClawsModClient::onRenderGui);
        MinecraftForge.EVENT_BUS.addListener(FangsClawsModClient::onMouseButton);
        MinecraftForge.EVENT_BUS.addListener(FangsClawsModClient::onMovementInput);
        MinecraftForge.EVENT_BUS.addListener(FangsClawsModClient::onClientTick);
    }

    @SubscribeEvent
    static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("nightmare_stamina", new NightmareStaminaLayer());
    }

    static void onRenderGui(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        BlockPos eyePos      = BlockPos.containing(mc.player.getEyePosition());
        boolean inGhostBlock = mc.level.getBlockState(eyePos).getBlock() instanceof GhostBlock;
        boolean hasVenom     = mc.player.hasEffect(MobEffectsRegistry.VENOM.get());

        int width  = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        if (inGhostBlock) {
            event.getGuiGraphics().fill(0, 0, width, height, 0x8CFFFFFF);
        }
        if (hasVenom) {
            float pulse = (float)(0.45 + 0.15 * Math.sin(System.currentTimeMillis() / 300.0));
            int   alpha = (int)(pulse * 255) & 0xFF;
            int   color = (alpha << 24) | 0x7A00C8;
            event.getGuiGraphics().fill(0, 0, width, height, color);
        }
    }

    static void onMouseButton(InputEvent.MouseButton event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (!mc.player.hasEffect(MobEffectsRegistry.VENOM.get())) return;
        if (event.getAction() == GLFW.GLFW_RELEASE) return;

        int button = event.getButton();
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;

        int swapped = (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                    ? GLFW.GLFW_MOUSE_BUTTON_RIGHT
                    : GLFW.GLFW_MOUSE_BUTTON_LEFT;

        InputConstants.Key swappedKey = InputConstants.Type.MOUSE.getOrCreate(swapped);
        KeyMapping.click(swappedKey);
    }

    static void onMovementInput(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.hasEffect(MobEffectsRegistry.VENOM.get())) return;

        Input input = event.getInput();

        input.forwardImpulse = -input.forwardImpulse;
        boolean tmpUp = input.up;
        input.up   = input.down;
        input.down = tmpUp;

        input.leftImpulse = -input.leftImpulse;
        boolean tmpLeft = input.left;
        input.left  = input.right;
        input.right = tmpLeft;
    }

    // Owl Armor
    private static final double OWL_FLAP_IMPULSE = 0.75;
    private static final double OWL_FLAP_IMPULSE_PER_MISSING = 0.1;
    private static final double OWL_GLIDE_FALL   = -0.2;
    private static final double OWL_GLIDE_FALL_PER_MISSING   = 0.75;
    private static final double SHRIKE_FLAP_BONUS_PER_PIECE  = 0.035;
    private static final double SHRIKE_GLIDE_BONUS_PER_PIECE = 0.02;
    private static boolean owlPrevJumpDown  = false;
    private static boolean owlPrevOnGround  = true;
    private static boolean owlFlapUsed      = false;
    private static boolean owlSentGlideLast = false;
    private static boolean shrikeFlapUsed   = false;

    static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        OwlFlightState.tickLocal();

        Player player = mc.player;
        if (player == null || mc.level == null || mc.isPaused()) {
            owlPrevJumpDown = false;
            owlPrevOnGround = true;
            OwlFlightState.setLocalGliding(false);
            return;
        }

        int owlPieces    = OwlArmorItem.countOwlPieces(player);
        int shrikePieces = ShrikeArmorItem.countShrikePieces(player);
        int flightPieces = owlPieces + shrikePieces;
        boolean canFly = (OwlArmorItem.hasOwlChestplate(player) || ShrikeArmorItem.hasShrikeChestplate(player))
                && !player.getAbilities().flying
                && !player.isFallFlying()
                && !player.isInWater() && !player.isInLava()
                && !player.onClimbable() && !player.isPassenger();

        double flapImpulse = OWL_FLAP_IMPULSE - (4 - flightPieces) * OWL_FLAP_IMPULSE_PER_MISSING
                + shrikePieces * SHRIKE_FLAP_BONUS_PER_PIECE;
        double glideFall   = OWL_GLIDE_FALL   - (4 - flightPieces) * OWL_GLIDE_FALL_PER_MISSING
                + shrikePieces * SHRIKE_GLIDE_BONUS_PER_PIECE;

        boolean jumpDown = mc.options.keyJump.isDown();
        boolean airborne = !player.onGround();
        boolean gliding  = false;
        boolean flapped  = false;

        if (!airborne) {
            owlFlapUsed = false;
            shrikeFlapUsed = false;
        }

        if (canFly && airborne) {
            boolean freshPress = jumpDown && !owlPrevJumpDown && !owlPrevOnGround && !owlFlapUsed;
            if (freshPress) {
                owlFlapUsed = true;
                Vec3 m = player.getDeltaMovement();
                player.setDeltaMovement(m.x, Math.max(m.y, flapImpulse), m.z);
                player.hasImpulse = true;
                player.resetFallDistance();
                OwlFlightState.triggerLocalFlap();
                flapped = true;
            } else if (jumpDown && player.getDeltaMovement().y < 0.0) {
                Vec3 m = player.getDeltaMovement();
                if (m.y < glideFall) player.setDeltaMovement(m.x, glideFall, m.z);
                player.resetFallDistance();
                gliding = true;
            }
        }

        if (gliding || flapped || owlSentGlideLast) {
            FangsNetwork.CHANNEL.sendToServer(new OwlFlightPacket(gliding, flapped));
        }
        owlSentGlideLast = gliding;

        if (ShrikeArmorItem.hasShrikeChestplate(player) && player.isFallFlying()) {
            boolean freshPress = jumpDown && !owlPrevJumpDown && !shrikeFlapUsed;
            if (freshPress) {
                shrikeFlapUsed = true;
                Vec3 look = player.getLookAngle();
                double hx = look.x, hz = look.z;
                double hlen = Math.sqrt(hx * hx + hz * hz);
                if (hlen > 1.0e-4) { hx /= hlen; hz /= hlen; }
                Vec3 m = player.getDeltaMovement();
                double up  = flapImpulse * 0.6;
                double fwd = flapImpulse * 0.7;
                player.setDeltaMovement(m.x + hx * fwd, m.y + up, m.z + hz * fwd);
                player.hasImpulse = true;
                OwlFlightState.triggerLocalFlap();
                FangsNetwork.CHANNEL.sendToServer(new OwlFlightPacket(false, true));
            }
        }

        OwlFlightState.setLocalGliding(gliding);
        owlPrevJumpDown = jumpDown;
        owlPrevOnGround = player.onGround();
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        FangsClawsModClient.init();

        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (mc, parent) -> new FangsConfigScreen(parent)
            )
        );

        event.enqueueWork(() -> {
            net.minecraft.client.gui.screens.MenuScreens.register(
                net.raptorzizi.fangs_n_claws.registries.MenuTypeRegistry.HORSE_ARMOR.get(),
                net.raptorzizi.fangs_n_claws.entity.horse.HorseArmorScreen::new);

            ItemProperties.register(ItemsRegistry.FANG_DAGGER.get(),
                FangsClawsMod.id("backstab"),
                (stack, level, entity, seed) -> {
                    if (!(entity instanceof Player player) || level == null) return 0f;
                    if (player.getCooldowns().isOnCooldown(stack.getItem())) return 0f;
                    AABB box = player.getBoundingBox().inflate(4.0);
                    List<LivingEntity> nearby = level.getEntitiesOfClass(
                            LivingEntity.class, box, e -> e != player && e.isAlive());
                    for (LivingEntity target : nearby) {
                        if (FangDaggerItem.isBackstab(player, target)) return 1f;
                    }
                    return 0f;
                });

            ItemProperties.register(ItemsRegistry.NETHERITE_DAGGER.get(),
                FangsClawsMod.id("backstab"),
                (stack, level, entity, seed) -> {
                    if (!(entity instanceof Player player) || level == null) return 0f;
                    if (player.getCooldowns().isOnCooldown(stack.getItem())) return 0f;
                    AABB box = player.getBoundingBox().inflate(4.0);
                    List<LivingEntity> nearby = level.getEntitiesOfClass(
                            LivingEntity.class, box, e -> e != player && e.isAlive());
                    for (LivingEntity target : nearby) {
                        if (FangDaggerItem.isBackstab(player, target)) return 1f;
                    }
                    return 0f;
                });

            ItemProperties.register(ItemsRegistry.CATCHING_CLAW.get(),
                FangsClawsMod.id("catching_claw_cast"),
                (stack, level, entity, seed) -> {
                    // 1.20.1: DataComponents don't exist — use NBT directly
                    CompoundTag tag = stack.getTag();
                    if (tag == null) return 0f;
                    return tag.contains("HookUUID") ? 1f : 0f;
                });

            ItemProperties.register(ItemsRegistry.BLOWGUN.get(),
                new ResourceLocation("minecraft", "pulling"),
                (stack, level, entity, seed) ->
                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f);

            ItemProperties.register(ItemsRegistry.BLOWGUN.get(),
                new ResourceLocation("minecraft", "pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null || entity.getUseItem() != stack) return 0.0f;
                    int charged = stack.getUseDuration() - entity.getUseItemRemainingTicks();
                    return Math.min(charged / (float) BlowgunItem.CHARGE_TICKS, 1.0f);
                });
        });
    }

}
