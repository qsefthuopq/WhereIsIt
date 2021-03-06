package red.jackf.whereisit.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import red.jackf.whereisit.FoundType;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.WhereIsItClient;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import static red.jackf.whereisit.WhereIsItClient.optimizedDrawShapeOutline;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    private final List<WhereIsItClient.FoundItemPos> wii_outlinesToRemove = new ArrayList<>();

    private static final RenderPhase.Transparency WII_Transparency = new RenderPhase.Transparency("wii_translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);

    private static final RenderLayer WII_RenderLayer = RenderLayer.of("wii_blockoutline",
            VertexFormats.POSITION_COLOR,
            1, 256,
            RenderLayer.MultiPhaseParameters.builder()
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.empty()))
                    .depthTest(new RenderPhase.DepthTest("pass", 519))
                    .transparency(WII_Transparency)
                    .build(false)
            );

    //@Shadow
    //private static void drawShapeOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {}


    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Shadow private ClientWorld world;

    @Inject(method= "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",
            at=@At(value = "TAIL"))
    public void renderFoundItemOverlay(MatrixStack matrices,
                                       float tickDelta,
                                       long limitTime,
                                       boolean renderBlockOutline,
                                       Camera camera,
                                       GameRenderer gameRenderer,
                                       LightmapTextureManager lightmapTextureManager,
                                       Matrix4f matrix4f,
                                       CallbackInfo ci) {
        this.world.getProfiler().swap("wii_founditems");
        Vec3d cameraPos = camera.getPos();
        VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();
        RenderSystem.disableDepthTest();
        matrices.push();

        float r = ((WhereIsIt.CONFIG.colour >> 16) & 0xff) / 255f;
        float g = ((WhereIsIt.CONFIG.colour >> 8) & 0xff) / 255f;
        float b = ((WhereIsIt.CONFIG.colour) & 0xff) / 255f;

        float rAlt = ((WhereIsIt.CONFIG.alternateColour >> 16) & 0xff) / 255f;
        float gAlt = ((WhereIsIt.CONFIG.alternateColour >> 8) & 0xff) / 255f;
        float bAlt = ((WhereIsIt.CONFIG.alternateColour) & 0xff) / 255f;

        for (WhereIsItClient.FoundItemPos foundPos : WhereIsItClient.FOUND_ITEM_POSITIONS) {
            long timeDiff = this.world.getTime() - foundPos.time;
            if (foundPos.type == FoundType.FOUND_DEEP) {
                optimizedDrawShapeOutline(matrices,
                    immediate.getBuffer(WII_RenderLayer),
                    foundPos.shape,
                    foundPos.pos.getX() - cameraPos.x,
                    foundPos.pos.getY() - cameraPos.y,
                    foundPos.pos.getZ() - cameraPos.z,
                    rAlt,
                    gAlt,
                    bAlt,
                    (WhereIsIt.CONFIG.fadeOutTime - timeDiff) / (float) WhereIsIt.CONFIG.fadeOutTime
                );
            } else {
                optimizedDrawShapeOutline(matrices,
                    immediate.getBuffer(WII_RenderLayer),
                    foundPos.shape,
                    foundPos.pos.getX() - cameraPos.x,
                    foundPos.pos.getY() - cameraPos.y,
                    foundPos.pos.getZ() - cameraPos.z,
                    r,
                    g,
                    b,
                    (WhereIsIt.CONFIG.fadeOutTime - timeDiff) / (float) WhereIsIt.CONFIG.fadeOutTime
                );
            }
            if (timeDiff >= WhereIsIt.CONFIG.fadeOutTime) {
                wii_outlinesToRemove.add(foundPos);
            }
        }

        immediate.draw(WII_RenderLayer);
        matrices.pop();
        RenderSystem.enableDepthTest();

        for (WhereIsItClient.FoundItemPos pos : wii_outlinesToRemove)
            WhereIsItClient.FOUND_ITEM_POSITIONS.remove(pos);

        wii_outlinesToRemove.clear();
    }
}
