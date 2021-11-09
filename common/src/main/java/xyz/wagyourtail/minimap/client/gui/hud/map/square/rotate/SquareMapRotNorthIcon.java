package xyz.wagyourtail.minimap.client.gui.hud.map.square.rotate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.minimap.WagYourMinimap;
import xyz.wagyourtail.minimap.api.client.MinimapClientApi;
import xyz.wagyourtail.minimap.api.client.config.MinimapClientConfig;
import xyz.wagyourtail.minimap.client.gui.AbstractMapRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMapOverlayRenderer;
import xyz.wagyourtail.minimap.client.gui.hud.map.AbstractMinimapRenderer;

public class SquareMapRotNorthIcon extends AbstractMapOverlayRenderer {
    private static final ResourceLocation north_tex = new ResourceLocation(WagYourMinimap.MOD_ID, "textures/north_icon.png");

    public SquareMapRotNorthIcon(AbstractMinimapRenderer parent) {
        super(parent);
    }

    @Override
    public void renderOverlay(PoseStack stack, @NotNull Vec3 center, float maxLength, @NotNull Vec3 player_pos, float player_rot) {
        int chunkRadius = MinimapClientApi.getInstance().getConfig().get(MinimapClientConfig.class).chunkRadius;

        int chunkDiam = chunkRadius * 2 - 1;
        float chunkScale = maxLength / ((float) chunkDiam - 1);

        Vec3 pointVec = new Vec3(0,1,1).yRot((float) Math.toRadians(player_rot));
        float scale = ((chunkRadius - 1) * 16f) / (float) Math.max(Math.abs(pointVec.x), Math.abs(pointVec.z));
        pointVec = pointVec.multiply(scale, scale, scale);
        stack.translate(maxLength / 2 + pointVec.x * chunkScale / 16f, maxLength / 2 + pointVec.z * chunkScale / 16f, 0);
        stack.scale(.005f * maxLength, .005f * maxLength, 1);
        RenderSystem.setShaderTexture(0, north_tex);
        AbstractMapRenderer.drawTex(stack, -10, -10, 20, 20, 1, 1, 0, 0);
    }

}
