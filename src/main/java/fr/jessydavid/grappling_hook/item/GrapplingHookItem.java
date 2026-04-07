package fr.jessydavid.grappling_hook.item;

import fr.jessydavid.grappling_hook.entity.GrapplingHookEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class GrapplingHookItem extends Item {

    private static final float LAUNCH_POWER = 1.5F;

    public GrapplingHookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        List<GrapplingHookEntity> existingHooks = level.getEntitiesOfClass(
                GrapplingHookEntity.class,
                player.getBoundingBox().inflate(64),
                e -> e.getOwner() == player
        );

        if (!existingHooks.isEmpty()) {
            if (!level.isClientSide()) {
                existingHooks.forEach(hook -> {
                    if (hook.isStuck()) {
                        hook.pull();
                    } else {
                        hook.discard();
                    }
                });
            }
            return InteractionResultHolder.success(stack);
        }

        if (!level.isClientSide()) {
            GrapplingHookEntity hook = new GrapplingHookEntity(player, stack, level);
            hook.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, LAUNCH_POWER, 1.0F);
            level.addFreshEntity(hook);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS,
                    0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}