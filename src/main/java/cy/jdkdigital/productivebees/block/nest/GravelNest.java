package cy.jdkdigital.productivebees.block.nest;

import cy.jdkdigital.productivebees.block.SolitaryNest;
import cy.jdkdigital.productivebees.init.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.Dimension;

public class GravelNest extends SolitaryNest {

    public GravelNest(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canRepopulateIn(Dimension dimension, Biome biome) {
        return (biome.getCategory().equals(Biome.Category.RIVER) || biome.getCategory().equals(Biome.Category.BEACH)) && biome.getTempCategory() != Biome.TempCategory.COLD;
    }

    @Override
    public EntityType<BeeEntity> getNestingBeeType(World world) {
        return  world.getRandom().nextBoolean() ? ModEntities.DIGGER_BEE.get() : (
                world.getRandom().nextBoolean() ? ModEntities.ASHY_MINING_BEE.get() : ModEntities.CHOCOLATE_MINING_BEE.get()
        );
    }
}
