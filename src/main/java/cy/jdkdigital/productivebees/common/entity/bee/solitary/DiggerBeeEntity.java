package cy.jdkdigital.productivebees.common.entity.bee.solitary;

import cy.jdkdigital.productivebees.common.entity.bee.SolitaryBeeEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.world.World;

public class DiggerBeeEntity extends SolitaryBeeEntity
{
    public DiggerBeeEntity(EntityType<? extends BeeEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public String getRenderer() {
        return "small";
    }
}
