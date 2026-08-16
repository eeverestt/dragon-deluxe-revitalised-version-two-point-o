package com.peak.client;

import com.peak.Main;
import com.peak.client.render.DragonEntityRenderer;
import com.peak.init.DragonEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class DragonRenderers {
    public static final EntityModelLayer DRAGON = createMain("dragon");

    private static EntityModelLayer createMain(String name) {
        return new EntityModelLayer(Identifier.of(Main.MODID, name), "main");
    }

    public static void registerModelLayers() {
        EntityModelLayerRegistry.registerModelLayer(DRAGON, DragonEntityRenderer::getTexturedModelData);
    }

    public static void registerRenderers() {
        EntityRendererRegistry.register(DragonEntities.DRAGON_ENTITY_ENTITY_TYPE, DragonEntityRenderer::new);
    }
}
