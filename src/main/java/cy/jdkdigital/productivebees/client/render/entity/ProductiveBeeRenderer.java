package cy.jdkdigital.productivebees.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import cy.jdkdigital.productivebees.ProductiveBees;
import cy.jdkdigital.productivebees.client.render.entity.layers.BeeBodyLayer;
import cy.jdkdigital.productivebees.client.render.entity.model.ProductiveBeeModel;
import cy.jdkdigital.productivebees.common.entity.bee.ConfigurableBeeEntity;
import cy.jdkdigital.productivebees.common.entity.bee.ProductiveBeeEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Calendar;

public class ProductiveBeeRenderer extends MobRenderer<ProductiveBeeEntity, ProductiveBeeModel<ProductiveBeeEntity>>
{
    private boolean isChristmas;

    public ProductiveBeeRenderer(EntityRendererManager renderManagerIn, ProductiveBeeModel<ProductiveBeeEntity> model) {
        super(renderManagerIn, model, 0.7F);

        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 21 && calendar.get(Calendar.DATE) <= 26) {
            this.isChristmas = true;
        }
    }

    public ProductiveBeeRenderer(EntityRendererManager renderManagerIn) {
        this(renderManagerIn, new ProductiveBeeModel<>());

        addLayer(new BeeBodyLayer(this, "thicc", isChristmas));
        addLayer(new BeeBodyLayer(this, "default", isChristmas));
        addLayer(new BeeBodyLayer(this, "default_crystal", isChristmas));
        addLayer(new BeeBodyLayer(this, "default_shell", isChristmas));
        addLayer(new BeeBodyLayer(this, "default_foliage", isChristmas));
        addLayer(new BeeBodyLayer(this, "elvis", isChristmas));
        addLayer(new BeeBodyLayer(this, "small", isChristmas));
        addLayer(new BeeBodyLayer(this, "slim", isChristmas));
        addLayer(new BeeBodyLayer(this, "tiny", isChristmas));
        addLayer(new BeeBodyLayer(this, "translucent_with_center", isChristmas));
    }

    @Override
    protected void setupRotations(ProductiveBeeEntity entity, MatrixStack matrixStack, float f1, float f2, float f3) {
        super.setupRotations(entity, matrixStack, f1, f2, f3);

        if (entity instanceof ConfigurableBeeEntity && ((ConfigurableBeeEntity) entity).getRenderTransform().equals("flipped")) {
            matrixStack.translate(0.0D, (double) (entity.getBbHeight() + 0.1F), 0.0D);
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        }
    }

    @Nullable
    @Override
    protected RenderType getRenderType(ProductiveBeeEntity bee, boolean b1, boolean b2, boolean b3) {
        if (bee instanceof ConfigurableBeeEntity && ((ConfigurableBeeEntity) bee).isTranslucent()) {
            return RenderType.entityTranslucent(this.getTextureLocation(bee));
        }
        return super.getRenderType(bee, b1, b2, b3);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(ProductiveBeeEntity bee) {
        String textureLocation = ProductiveBees.MODID + ":textures/entity/bee/" + bee.getBeeName() + "/bee";

        // Colored bees use tinted base texture
        if (bee.getColor(0) != null) {
            String modelType = bee.getRenderer();
            textureLocation = ProductiveBees.MODID + ":textures/entity/bee/base/" + modelType + "/bee";
        }

        if (bee instanceof ConfigurableBeeEntity) {
            if (((ConfigurableBeeEntity) bee).hasBeeTexture()) {
                textureLocation = ((ConfigurableBeeEntity) bee).getBeeTexture();
            }
        }

        if (bee.isAngry()) {
            textureLocation = textureLocation + "_angry";
        }

        if (bee.hasNectar()) {
            textureLocation = textureLocation + "_nectar";
        }

        return new ResourceLocation(textureLocation + ".png");
    }
}
