package fr.jessydavid.grappling_hook.entity;

import fr.jessydavid.grappling_hook.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GrapplingHookEntity extends ThrowableItemProjectile {

    private static final double MAX_DISTANCE = 32.0D;
    private static final double MAX_SPEED = 4.0D;

    protected boolean isStick;
    protected double stickLength;
    private boolean addedToWorld;

    public GrapplingHookEntity(EntityType<? extends GrapplingHookEntity> entityType, Level level) {
        super(entityType, level);
    }

    public GrapplingHookEntity(Player owner, ItemStack ownerStack, Level level) {
        super(ModEntities.GRAPPLING_HOOK.get(), level);
        this.moveTo(owner.getX(), owner.getEyeY(), owner.getZ(), owner.getYHeadRot(), owner.getXRot());
        this.setOwner(owner);
        this.setItem(ownerStack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.GRAPPLING_HOOK.get();
    }

    @Override
    public void tick() {
        super.tick();

        if (!addedToWorld) {
            addedToWorld = true;
        }

        double offsetLengthSqr = distanceToSqr(getOwner());
        double maxDistanceSqr = MAX_DISTANCE * MAX_DISTANCE;

        if (!level().isClientSide() && (!isFocused() || offsetLengthSqr > maxDistanceSqr)) {
            discard();
            return;
        }

        // Collision detection with blocks
        var collidableBox = getBoundingBox().inflate(0.25D);
        var collisions = level().getBlockCollisions(this, collidableBox);

        boolean willStick = false;
        for (VoxelShape shape : collisions) {
            if (!shape.isEmpty() && shape.bounds().intersects(collidableBox)) {
                willStick = true;
                break;
            }
        }

        if (willStick && !isStick) {
            stickLength = offsetLengthSqr;
            playSound(SoundEvents.CHAIN_HIT);
        }

        isStick = willStick;

        if (isStick && getOwner() != null) {
            if (offsetLengthSqr > stickLength) {
                var direction = position().subtract(getOwner().position()).normalize();
                var scale = Math.min(MAX_SPEED, 0.01D * Math.sqrt(offsetLengthSqr));
                if (scale >= 0) {
                    getOwner().setDeltaMovement(getOwner().getDeltaMovement().add(direction.scale(scale)));
                    getOwner().hurtMarked = true;
                }
            }
            setDeltaMovement(0.0D, 0.0D, 0.0D);
        } else {
            setDeltaMovement(getDeltaMovement().scale(0.98D));
            setDeltaMovement(getDeltaMovement().add(0.0D, -0.03D, 0.0D));
        }

        move(MoverType.SELF, getDeltaMovement());
    }

    // Pull
    public void pull() {
        if (getOwner() instanceof Player player && isStick) {
            Vec3 direction = position().subtract(player.position()).normalize();
            double distance = distanceTo(player);

            double speed = Math.min(MAX_SPEED, Math.max(1.5D, distance * 0.5D));

            player.setDeltaMovement(direction.scale(speed));
            player.hurtMarked = true;
            player.resetFallDistance();
        }
        discard();
    }

    public boolean isFocused() {
        if (getOwner() instanceof Player player) {
            return ItemStack.isSameItemSameComponents(player.getMainHandItem(), getItem())
                    || ItemStack.isSameItemSameComponents(player.getOffhandItem(), getItem());
        }
        return false;
    }

    public boolean isStuck() {
        return isStick;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }
}