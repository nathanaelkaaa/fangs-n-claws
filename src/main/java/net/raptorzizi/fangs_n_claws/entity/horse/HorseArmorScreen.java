package net.raptorzizi.fangs_n_claws.entity.horse;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class HorseArmorScreen extends AbstractContainerScreen<HorseArmorMenu> {

    private static final ResourceLocation BG = new ResourceLocation("textures/gui/container/horse.png");

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
        // Slot selle et armure (UV depuis horse.png, comme le HorseInventoryScreen vanilla)
        guiGraphics.blit(BG, i + 7, j + 35 - 18, 18, this.imageHeight + 54, 18, 18);
        guiGraphics.blit(BG, i + 7, j + 35, 0, this.imageHeight + 54, 18, 18);
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 51, j + 60, 17,
                (float) (i + 51) - this.xMouse, (float) (j + 75 - 50) - this.yMouse, this.horse);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
