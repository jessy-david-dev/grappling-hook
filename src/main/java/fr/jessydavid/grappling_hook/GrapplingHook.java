package fr.jessydavid.grappling_hook;

import fr.jessydavid.grappling_hook.entity.ModEntities;
import fr.jessydavid.grappling_hook.item.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(GrapplingHook.MODID)
@EventBusSubscriber(modid = GrapplingHook.MODID)
public class GrapplingHook {

    public static final String MODID = "grappling_hook";

    public GrapplingHook(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
    }

    @SubscribeEvent
    public static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.GRAPPLING_HOOK.get());
        }
    }
}