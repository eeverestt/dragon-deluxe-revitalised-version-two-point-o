package com.peak.init;

import com.peak.Main;
import com.peak.content.entity.DragonEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class DragonEntities {
    public static final EntityType<DragonEntity> DRAGON_ENTITY_ENTITY_TYPE = register(
            "dragon_entity",
            EntityType.Builder.create(DragonEntity::new, SpawnGroup.MONSTER)
                    .makeFireImmune().dimensions(3.5f, 2.5f).passengerAttachments(3.0F).maxTrackingRange(10)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(Registries.ENTITY_TYPE.getKey(), Identifier.of(Main.MODID, name));
        return Registry.register(Registries.ENTITY_TYPE, key, builder.build(String.valueOf(key)));
    }

    public static void registerModEntityTypes() {
        Main.LOGGER.info("Registering EntityTypes for " + Main.MODID);
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(DRAGON_ENTITY_ENTITY_TYPE, DragonEntity.createDragonAttributes());
    }

    public static void init() {
        registerAttributes();
    }
}
