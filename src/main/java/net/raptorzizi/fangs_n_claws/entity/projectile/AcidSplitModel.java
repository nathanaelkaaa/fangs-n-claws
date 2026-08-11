package net.raptorzizi.fangs_n_claws.entity.projectile;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.raptorzizi.fangs_n_claws.FangsClawsMod;

public class AcidSplitModel extends HierarchicalModel<AcidSplitProjectile> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(FangsClawsMod.id("acid_split"), "main");

    private final ModelPart root;

    public AcidSplitModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 5.0F)
                        .texOffs(0, 12).addBox(-2.0F, -5.0F, -1.0F, 4.0F, 4.0F, 8.0F)
                        .texOffs(0, 0).addBox(-3.0F, -6.0F, -7.0F, 6.0F, 6.0F, 6.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(AcidSplitProjectile entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
