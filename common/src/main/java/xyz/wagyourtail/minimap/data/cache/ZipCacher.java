package xyz.wagyourtail.minimap.data.cache;

import net.minecraft.resources.ResourceLocation;
import xyz.wagyourtail.minimap.api.MinimapApi;
import xyz.wagyourtail.minimap.data.ChunkData;
import xyz.wagyourtail.minimap.data.ChunkLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ZipCacher extends AbstractCacher {
    private Path locationToPath(ChunkLocation location) {
        return MinimapApi.getInstance().configFolder.resolve(location.level().server_slug).resolve(location.level().level_slug).resolve(location.region().getString() + ".zip");
    }

    @Override
    public synchronized ChunkData load(ChunkLocation location) {
        Path zip = locationToPath(location);
        if (Files.notExists(zip)) return null;
        try (FileSystem zipfs = FileSystems.newFileSystem(zip)) {
            Path dataPath = zipfs.getPath(location.index() + ".data");
            Path resourcesPath = zipfs.getPath(location.index() + ".resources");
            if (Files.exists(dataPath) && Files.exists(resourcesPath)) {
                return loadFromDisk(location, dataPath, resourcesPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public synchronized void save(ChunkLocation location, ChunkData data) {
        if (!data.changed) return;
        Path zip = locationToPath(location);
        try {
            if (Files.notExists(zip.getParent())) Files.createDirectories(zip.getParent());
            try (FileSystem zipfs = FileSystems.newFileSystem(zip, Map.of("create", true))) {
                Path dataPath = zipfs.getPath(location.index() + ".data");
                Path resourcesPath = zipfs.getPath(location.index() + ".resources");
                writeToZip(dataPath, resourcesPath, data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ChunkData loadFromDisk(ChunkLocation location, Path dataPath, Path resourcesPath) {
        ChunkData chunk = new ChunkData(location);
        try (InputStream stream = Files.newInputStream(dataPath)) {
            ByteBuffer data = ByteBuffer.wrap(stream.readAllBytes());
            data.rewind();
            chunk.updateTime = data.getLong();
            for (int i = 0; i < 256; ++i) {
                chunk.heightmap[i] = data.getInt();
            }
            for (int i = 0; i < 256; ++i) {
                chunk.blocklight[i] = data.get();
            }
            for (int i = 0; i < 256; ++i) {
                chunk.blockid[i] = data.getInt();
            }
            for (int i = 0; i < 256; ++i) {
                chunk.biomeid[i] = data.getInt();
            }
            for (int i = 0; i < 256; ++i) {
                chunk.oceanFloorHeightmap[i] = data.getInt();
            }
            for (int i = 0; i < 256; ++i) {
                chunk.oceanFloorBlockid[i] = data.getInt();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try (InputStream stream = Files.newInputStream(resourcesPath)) {
            for (String resource : new String(stream.readAllBytes()).split("\n")) {
                chunk.getOrRegisterResourceLocation(new ResourceLocation(resource));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return chunk;
    }

    private synchronized void writeToZip(Path dataPath, Path resourcesPath, ChunkData chunk) {
        try {
            String resources = chunk.getResources().stream().map(ResourceLocation::toString).reduce("", (a, b) -> a + b + "\n");
            Files.writeString(resourcesPath, resources);
            ByteBuffer data = ByteBuffer.allocate(Long.BYTES + Integer.BYTES * 256 * 6 + Byte.BYTES * 256);
            data.putLong(chunk.updateTime);
            for (int i = 0; i < 256; ++i) {
                data.putInt(chunk.heightmap[i]);
            }
            for (int i = 0; i < 256; ++i) {
                data.put(chunk.blocklight[i]);
            }
            for (int i = 0; i < 256; ++i) {
                data.putInt(chunk.blockid[i]);
            }
            for (int i = 0; i < 256; ++i) {
                data.putInt(chunk.biomeid[i]);
            }
            for (int i = 0; i < 256; ++i) {
                data.putInt(chunk.oceanFloorHeightmap[i]);
            }
            for (int i = 0; i < 256; ++i) {
                data.putInt(chunk.oceanFloorBlockid[i]);
            }
            Files.write(dataPath, data.array());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
