package fr.jessydavid.grappling_hook.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.jessydavid.grappling_hook.GrapplingHook;
import fr.jessydavid.grappling_hook.entity.GrapplingHookEntity;
import fr.jessydavid.grappling_hook.entity.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(modid = GrapplingHook.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GRAPPLING_HOOK.get(), GrapplingHookRenderer::new);
    }

    public static class GrapplingHookRenderer extends EntityRenderer<GrapplingHookEntity> {

        private static final ResourceLocation HOOK_TEXTURE =
                ResourceLocation.fromNamespaceAndPath(GrapplingHook.MODID, "textures/item/hook.png");

        private static final ResourceLocation CHAIN_TEXTURE =
                ResourceLocation.withDefaultNamespace("textures/block/bamboo_stalk.png");

        private static final float CHAIN_WIDTH = 0.025F;

        public GrapplingHookRenderer(EntityRendererProvider.Context ctx) {
            super(ctx);
        }

        @Override
        public void render(GrapplingHookEntity entity, float entityYaw, float partialTicks,
                           PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

            Player player = (Player) entity.getOwner();
            if (player == null || !entity.isFocused()) return;

            poseStack.pushPose();

            poseStack.pushPose();
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(0.5F, 0.5F, 0.5F);

            VertexConsumer hookConsumer = buffer.getBuffer(RenderType.entityTranslucentCull(HOOK_TEXTURE));
            PoseStack.Pose hookPose = poseStack.last();

            hookConsumer.addVertex(hookPose.pose(), -0.5F, -0.5F, 0.0F)
                    .setColor(255, 255, 255, 255).setUv(0.0F, 1.0F)
                    .setOverlay(0).setLight(packedLight).setNormal(hookPose, 0, 1, 0);
            hookConsumer.addVertex(hookPose.pose(), 0.5F, -0.5F, 0.0F)
                    .setColor(255, 255, 255, 255).setUv(1.0F, 1.0F)
                    .setOverlay(0).setLight(packedLight).setNormal(hookPose, 0, 1, 0);
            hookConsumer.addVertex(hookPose.pose(), 0.5F, 0.5F, 0.0F)
                    .setColor(255, 255, 255, 255).setUv(1.0F, 0.0F)
                    .setOverlay(0).setLight(packedLight).setNormal(hookPose, 0, 1, 0);
            hookConsumer.addVertex(hookPose.pose(), -0.5F, 0.5F, 0.0F)
                    .setColor(255, 255, 255, 255).setUv(0.0F, 0.0F)
                    .setOverlay(0).setLight(packedLight).setNormal(hookPose, 0, 1, 0);

            poseStack.popPose();

            int handOffset = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            if (!player.getMainHandItem().is(entity.getItem().getItem())) {
                handOffset = -handOffset;
            }

            float attackAnim = player.getAttackAnim(partialTicks);
            float swingProgress = Mth.sin(Mth.sqrt(attackAnim) * (float) Math.PI);
            float bodyYaw = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180F);
            double sinYaw = Mth.sin(bodyYaw);
            double cosYaw = Mth.cos(bodyYaw);
            double handSide = handOffset * 0.35;

            double handX, handY, handZ;
            float crouchOffset;

            if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson()
                    && player == Minecraft.getInstance().player) {
                double fovScale = 960.0D / this.entityRenderDispatcher.options.fov().get().intValue();
                net.minecraft.world.phys.Vec3 nearPlane = this.entityRenderDispatcher.camera.getNearPlane()
                        .getPointOnPlane((float) handOffset * 0.825F, -0.08F);
                nearPlane = nearPlane.scale(fovScale);
                nearPlane = nearPlane.yRot(swingProgress * 0.5F);
                nearPlane = nearPlane.xRot(-swingProgress * 0.7F);
                handX = Mth.lerp(partialTicks, player.xo, player.getX()) + nearPlane.x;
                handY = Mth.lerp(partialTicks, player.yo, player.getY()) + nearPlane.y;
                handZ = Mth.lerp(partialTicks, player.zo, player.getZ()) + nearPlane.z;
                crouchOffset = player.getEyeHeight();
            } else {
                handX = Mth.lerp(partialTicks, player.xo, player.getX()) - cosYaw * handSide - sinYaw * 0.8D;
                handY = player.yo + player.getEyeHeight() + (player.getY() - player.yo) * partialTicks - 0.45D;
                handZ = Mth.lerp(partialTicks, player.zo, player.getZ()) - sinYaw * handSide + cosYaw * 0.8D;
                crouchOffset = player.isCrouching() ? -0.1875F : 0.0F;
            }

            double hookX = Mth.lerp(partialTicks, entity.xo, entity.getX());
            double hookY = Mth.lerp(partialTicks, entity.yo, entity.getY()) + 0.25D;
            double hookZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());

            float dx = (float) (handX - hookX);
            float dy = (float) (handY - hookY) + crouchOffset;
            float dz = (float) (handZ - hookZ);
            float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);

            if (length < 0.01F) {
                poseStack.popPose();
                return;
            }

            VertexConsumer chainConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(CHAIN_TEXTURE));
            PoseStack.Pose chainPose = poseStack.last();
            Matrix4f mat = chainPose.pose();

            float nx = dx / length;
            float ny = dy / length;
            float nz = dz / length;

            Vector3f up = new Vector3f(0, 1, 0);
            Vector3f dir = new Vector3f(nx, ny, nz);
            Vector3f perp = new Vector3f();
            dir.cross(up, perp);
            if (perp.lengthSquared() < 0.001F) {
                perp.set(1, 0, 0);
            }
            perp.normalize().mul(CHAIN_WIDTH);

            int segments = Math.max(1, (int) (length / 0.5F));
            float uvStep = length / segments;

            for (int i = 0; i < segments; i++) {
                float t0 = (float) i / segments;
                float t1 = (float) (i + 1) / segments;

                float x0 = dx * t0;
                float y0 = dy * t0;
                float z0 = dz * t0;
                float x1 = dx * t1;
                float y1 = dy * t1;
                float z1 = dz * t1;

                float u0 = i * uvStep;
                float u1 = (i + 1) * uvStep;

                // Side 1 (XY plane of the chain)
                chainConsumer.addVertex(mat, x0 - perp.x, y0 - perp.y, z0 - perp.z)
                        .setColor(255, 255, 255, 255).setUv(u0, 0.0F)
                        .setOverlay(0).setLight(packedLight).setNormal(chainPose, nx, ny, nz);
                chainConsumer.addVertex(mat, x1 - perp.x, y1 - perp.y, z1 - perp.z)
                        .setColor(255, 255, 255, 255).setUv(u1, 0.0F)
                        .setOverlay(0).setLight(packedLight).setNormal(chainPose, nx, ny, nz);
                chainConsumer.addVertex(mat, x1 + perp.x, y1 + perp.y, z1 + perp.z)
                        .setColor(255, 255, 255, 255).setUv(u1, 1.0F)
                        .setOverlay(0).setLight(packedLight).setNormal(chainPose, nx, ny, nz);
                chainConsumer.addVertex(mat, x0 + perp.x, y0 + perp.y, z0 + perp.z)
                        .setColor(255, 255, 255, 255).setUv(u0, 1.0F)
                        .setOverlay(0).setLight(packedLight).setNormal(chainPose, nx, ny, nz);

                // Side 2 (perpendicular plane for 3D effect)
                Vector3f perp2 = new Vector3f();
                dir.cross(perp.normalize(new Vector3f()), perp2);
                perp2.normalize().mul(CHAIN_WIDTH);

                chainConsumer.addVertex(mat, x0 - perp2.x, y0 - perp2.y, z0 - perp2.z)
                        .setColor(255, 255, 255, 255).setUv(u0, 0.0F)
                        .setOverlay(0).setLight(packedLight).setNormal(chainPose, nx, ny, nz);
                chainConsumer.addVertex(mat, x1 - perp2.x, y1 - perp2.y, z1 - perp2.z)
                        .setColor(255, 255, 255, 255).setUv(u1, 0.0F)
                        .setOverlay(0).setLight(packedLight).setNormal(chainPose, nx, ny, nz);
                chainConsumer.addVertex(mat, x1 + perp2.x, y1 + perp2.y, z1 + perp2.z)
                        .setColor(255, 255, 255, 255).setUv(u1, 1.0F)
                        .setOverlay(0).setLight(packedLight).setNormal(chainPose, nx, ny, nz);
                chainConsumer.addVertex(mat, x0 + perp2.x, y0 + perp2.y, z0 + perp2.z)
                        .setColor(255, 255, 255, 255).setUv(u0, 1.0F)
                        .setOverlay(0).setLight(packedLight).setNormal(chainPose, nx, ny, nz);
            }

            poseStack.popPose();

            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }

        @Override
        public ResourceLocation getTextureLocation(GrapplingHookEntity entity) {
            return HOOK_TEXTURE;
        }
    }
}