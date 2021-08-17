package xyz.wagyourtail.minimap.api;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import xyz.wagyourtail.ResolveQueue;
import xyz.wagyourtail.minimap.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.chunkdata.updater.AbstractChunkUpdateStrategy;

public class MinimapEvents {
    public static final Event<ChunkUpdated> CHUNK_UPDATED = EventFactory.createLoop();

    public interface ChunkUpdated {
        void onChunkUpdated(ChunkLocation location, ResolveQueue<ChunkData> chunkData, Class<? extends AbstractChunkUpdateStrategy> strategy);
    }

}
