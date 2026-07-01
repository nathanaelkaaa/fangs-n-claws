package net.raptorzizi.fangs_n_claws.entity.horse;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class HorseArmorScreen extends AbstractContainerScreen<HorseArmorMenu> {

    private static final ResourceLocation BG          = ResourceLocation.withDefaultNamespace("textures/gui/container/horse.png");
    private static final ResourceLocation SADDLE_SLOT = ResourceLocation.withDefaultNamespace("container/horse/saddle_slot");
    private static final ResourceLocation ARMOR_SLOT  = ResourceLocation.withDefaultNamespace("container/horse/armor_slot");

    private final HorseMob horse;
    private float xMouse;
    private float yMouse;

    public HorseArmorScreen(HorseArmorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.horse = menu.getHorse();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BG, i, j, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blitSprite(SADDLE_SLOT, i + 7, j + 35 - 18, 18, 18);
        guiGraphics.blitSprite(ARMOR_SLOT, i + 7, j + 35, 18, 18);
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 18, i + 78, j + 70, 17, 0.25F,
                this.xMouse, this.yMouse, this.horse);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
