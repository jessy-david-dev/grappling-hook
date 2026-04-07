package fr.jessydavid.grappling_hook.item;

import fr.jessydavid.grappling_hook.GrapplingHook;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, GrapplingHook.MODID);

    public static final DeferredHolder<Item, GrapplingHookItem> GRAPPLING_HOOK =
            ITEMS.register("grappling_hook", () ->
                    new GrapplingHookItem(new Item.Properties().stacksTo(1))
            );
}