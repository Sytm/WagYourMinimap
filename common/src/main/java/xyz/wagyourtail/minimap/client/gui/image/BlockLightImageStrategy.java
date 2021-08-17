package xyz.wagyourtail.minimap.client.gui.image;

import com.mojang.blaze3d.platform.NativeImage;
import xyz.wagyourtail.minimap.client.gui.ThreadsafeDynamicTexture;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;

import java.awt.*;

public class BlockLightImageStrategy extends AbstractImageStrategy {

    private static final int TICKS_PER_DAY = 24000;

    private int colorForLightLevel(byte lightLevel) {
        return Color.HSBtoRGB(50F / 360F, 1F, lightLevel / 15F);
    }

    @Override
    public boolean shouldRender() {
        assert minecraft.level != null;
        long time = minecraft.level.getDayTime() % TICKS_PER_DAY;
        return time > TICKS_PER_DAY / 2;
    }

    @Override
    public ThreadsafeDynamicTexture load(ChunkLocation location, ChunkData key) {
        NativeImage image = new NativeImage(16, 16, false);
        for (int i = 0; i < 256; ++i) {
            int x = (i >> 4) % 16;
            int z = i % 16;
            image.setPixelRGBA(x, z, 0x7FFFFFFF & colorFormatSwap(colorForLightLevel(key.blocklight[i])));
        }
        return new ThreadsafeDynamicTexture(image);
    }

}
