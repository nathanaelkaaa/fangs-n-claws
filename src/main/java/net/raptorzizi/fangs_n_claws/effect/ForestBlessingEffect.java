package net.raptorzizi.fangs_n_claws.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Accordee par un ent qui recoit une pousse d'arbre : tant qu'elle dure, les ents ne s'en prennent
 * pas a celui qui abat des arbres. L'effet ne fait rien par lui-meme, EntEvents le consulte.
 */
public class ForestBlessingEffect extends MobEffect {

    private static final int COLOR = 0x4F8A32;

    public ForestBlessingEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
