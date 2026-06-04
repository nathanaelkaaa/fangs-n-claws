package net.raptorzizi.fangs_n_claws;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.raptorzizi.fangs_n_claws.block.GhostBlock;
import net.raptorzizi.fangs_n_claws.entity.catching_claw.CatchingClawHookRenderer;
import net.raptorzizi.fangs_n_claws.entity.decrepit_pitchfork.DecrepitPitchforkItemRenderer;
import net.raptorzizi.fangs_n_claws.entity.decrepit_pitchfork.DecrepitPitchforkRenderer;
import net.raptorzizi.fangs_n_claws.entity.imp.ImpRenderer;
import net.raptorzizi.fangs_n_claws.entity.ogre.OgreRenderer;
import net.raptorzizi.fangs_n_claws.item.BlowgunItem;
import net.raptorzizi.fangs_n_claws.item.FangDaggerItem;
import net.raptorzizi.fangs_n_claws.registries.EntityRegistry;
import net.raptorzizi.fangs_n_claws.registries.ItemsRegistry;
import net.raptorzizi.fangs_n_claws.registries.MobEffectsRegistry;

import java.util.List;


@Mod(value = FangsClawsMod.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = FangsClawsMod.MOD_ID, value = Dist.CLIENT)
public class FangsClawsModClient {
    public FangsClawsModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.addListener(FangsClawsModClient::onRenderGui);
        NeoForge.EVENT_BUS.addListener(FangsClawsModClient::onMovementInput);
        NeoForge.EVENT_BUS.addListener(FangsClawsModClient::onMouseButton);
    }

    private static final ResourceLocation BLUR_SHADER =
            ResourceLocation.withDefaultNamespace("shaders/post/blur.json");

    private static boolean blurActive = false;

    static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        BlockPos eyePos      = BlockPos.containing(mc.player.getEyePosition());
        boolean inGhostBlock = mc.level.getBlockState(eyePos).getBlock() instanceof GhostBlock;
        boolean hasVenom     = mc.player.hasEffect(MobEffectsRegistry.VENOM);
        boolean wantsBlur    = inGhostBlock || hasVenom;

        if (wantsBlur && !blurActive) {
            mc.gameRenderer.loadEffect(BLUR_SHADER);
            blurActive = true;
        } else if (!wantsBlur && blurActive) {
            mc.gameRenderer.shutdownEffect();
            blurActive = false;
        }

        int width  = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        if (inGhostBlock) {
            event.getGuiGraphics().fill(0, 0, width, height, 0x8CFFFFFF);
        }
        if (hasVenom) {
            float pulse  = (float)(0.45 + 0.15 * Math.sin(System.currentTimeMillis() / 300.0));
            int   alpha  = (int)(pulse * 255) & 0xFF;
            int   color  = (alpha << 24) | 0x7A00C8; // violet
            event.getGuiGraphics().fill(0, 0, width, height, color);
        }
    }

    static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (!mc.player.hasEffect(MobEffectsRegistry.VENOM)) return;

        int button = event.getButton();
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;

        event.setCanceled(true);

        int swapped = (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                    ? GLFW.GLFW_MOUSE_BUTTON_RIGHT
                    : GLFW.GLFW_MOUSE_BUTTON_LEFT;

        InputConstants.Key swappedKey = InputConstants.Type.MOUSE.getOrCreate(swapped);
        boolean held = event.getAction() != GLFW.GLFW_RELEASE;

        if (held) KeyMapping.click(swappedKey);
        KeyMapping.set(swappedKey, held);
    }

    static void onMovementInput(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.hasEffect(MobEffectsRegistry.VENOM)) return;

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

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
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
                    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
                    if (data == null) return 0f;
                    return data.copyTag().contains("HookUUID") ? 1f : 0f;
                });

            ItemProperties.register(ItemsRegistry.BLOWGUN.get(),
                ResourceLocation.fromNamespaceAndPath("minecraft", "pulling"),
                (stack, level, entity, seed) ->
                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f);

            ItemProperties.register(ItemsRegistry.BLOWGUN.get(),
                ResourceLocation.fromNamespaceAndPath("minecraft", "pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null || entity.getUseItem() != stack) return 0.0f;
                    int chargeDuration = level != null
                            ? BlowgunItem.getChargeDuration(stack, level)
                            : BlowgunItem.CHARGE_TICKS;
                    int charged = stack.getUseDuration(entity) - entity.getUseItemRemainingTicks();
                    return Math.min(charged / (float) chargeDuration, 1.0f);
                });
        });
    }

    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private DecrepitPitchforkItemRenderer renderer;

            @Override
            public DecrepitPitchforkItemRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    Minecraft mc = Minecraft.getInstance();
                    this.renderer = new DecrepitPitchforkItemRenderer(
                            mc.getBlockEntityRenderDispatcher(),
                            mc.getEntityModels());
                }
                return this.renderer;
            }
        }, ItemsRegistry.DECREPIT_PITCHFORK.get());
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.OGRE.get(), OgreRenderer::new);
        event.registerEntityRenderer(EntityRegistry.IMP.get(), ImpRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DECREPIT_PITCHFORK_ENTITY.get(), DecrepitPitchforkRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CATCHING_CLAW_HOOK.get(), CatchingClawHookRenderer::new);
        event.registerEntityRenderer(EntityRegistry.EVIL_EYE_PROJECTILE.get(), ThrownItemRenderer::new);
    }
}