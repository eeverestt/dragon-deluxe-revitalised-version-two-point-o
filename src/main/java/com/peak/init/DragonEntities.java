package com.peak.init;

import com.peak.Main;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class DragonEntities {
    public static final EntityType<MiniGolemEntity> MINI_GOLEM = register(
            "mini_golem",
            EntityType.Builder.<MiniGolemEntity>of(MiniGolemEntity::new, MobCategory.MISC)
                    .sized(0.75f, 1.75f)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(Registries.ENTITY_TYPE.getKey(), Identifier.of(Main.MODID, name));
        return Registry.register(Registries.ENTITY_TYPE, key, builder.build(String.valueOf(key)));
    }

    public static void registerModEntityTypes() {
        Main.LOGGER.info("Registering EntityTypes for " + Main.MODID);
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(MINI_GOLEM, MiniGolemEntity.createCubeAttributes());
    }

    public static void init() {}
}
