package fr.jessydavid.grappling_hook.entity;

import fr.jessydavid.grappling_hook.GrapplingHook;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, GrapplingHook.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<GrapplingHookEntity>> GRAPPLING_HOOK =
            ENTITY_TYPES.register("grappling_hook", () ->
                    EntityType.Builder.<GrapplingHookEntity>of(GrapplingHookEntity::new, MobCategory.MISC)
                            .sized(0.2F, 0.2F)
                            .noSave()
                            .noSummon()
                            .clientTrackingRange(4)
                            .updateInterval(2)
                            .build("grappling_hook")
            );
}