package xyz.wagyourtail.minimap.map.chunkdata.cache;

import xyz.wagyourtail.minimap.map.MapServer;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkData;
import xyz.wagyourtail.minimap.map.chunkdata.ChunkLocation;
import xyz.wagyourtail.minimap.waypoint.Waypoint;

import java.util.*;
import java.util.stream.Stream;

public class CacheManager extends AbstractCacher {
    private final Set<CacherEntry> cacherEntries = new HashSet<>();
    protected List<AbstractCacher> cachers = new ArrayList<>();

    public CacheManager() {
        super(false, false);
    }

    public synchronized void addCacherAfter(AbstractCacher cacher, Class<?> previous) {
        cacherEntries.add(new CacherEntry(cacher, null, previous));
        rebuildCacherList();
    }

    public synchronized void addCacherBefore(AbstractCacher cacher, Class<?> next) {
        cacherEntries.add(new CacherEntry(cacher, next, null));
        rebuildCacherList();
    }

    public synchronized void addCacherAfter(AbstractCacher cacher, Class<?> fallback, String... previous) {
        for (String s : previous) {
            try {
                Class<?> clazz = Class.forName(s);
                addCacherAfter(cacher, clazz);
                return;
            } catch (ClassNotFoundException ignored) {}
        }
        addCacherAfter(cacher, fallback);
    }

    public synchronized void addCacherBefore(AbstractCacher cacher, Class<?> fallback, String... next) {
        for (String s : next) {
            try {
                Class<?> clazz = Class.forName(s);
                addCacherBefore(cacher, clazz);
                return;
            } catch (ClassNotFoundException ignored) {}
        }
        addCacherBefore(cacher, fallback);
    }

    private void rebuildCacherList() {
        cachers.clear();
        Set<CacherEntry> remainingEntries = new HashSet<>(cacherEntries);
        Set<CacherEntry> removeThisLoop = new HashSet<>();
        for (CacherEntry entry : remainingEntries) {
            if (entry.before == null && entry.after == null) {
                cachers.add(entry.cache);
                removeThisLoop.add(entry);
            }
        }
        remainingEntries.removeAll(removeThisLoop);
        removeThisLoop.clear();
        while (!remainingEntries.isEmpty()) {
            for (CacherEntry entry : remainingEntries) {
                for (int i = 0; i < cachers.size(); ++i) {
                    if (cachers.get(i).getClass().equals(entry.after)) {
                        cachers.add(i + 1, entry.cache);
                        removeThisLoop.add(entry);
                        break;
                    } else if (cachers.get(i).getClass().equals(entry.before)) {
                        cachers.add(i, entry.cache);
                        removeThisLoop.add(entry);
                        break;
                    }
                }
            }
            if (removeThisLoop.isEmpty()) throw new RuntimeException("circular class before/after dependency in cachers!");
            remainingEntries.removeAll(removeThisLoop);
            removeThisLoop.clear();
        }
    }

    @Override
    public synchronized ChunkData loadChunk(ChunkLocation location) {
        ChunkData data = null;
        AbstractCacher hit = null;
        for (AbstractCacher cacher : cachers) {
            data = cacher.loadChunk(location);
            if (data != null) {
                hit = cacher;
                break;
            }
        }
        if (data == null) {
            data = new ChunkData(location);
        }
        if (hit != null && hit.countHitAsLoad) {
            for (AbstractCacher cacher : cachers) {
                if (cacher != hit && cacher.saveOnLoad) {
                    cacher.saveChunk(location, data);
                }
            }
        }
        return data;
    }

    @Override
    public synchronized void saveChunk(ChunkLocation location, ChunkData data) {
        for (AbstractCacher cacher : cachers) {
            cacher.saveChunk(location, data);
        }
    }

    @Override
    public synchronized void saveWaypoints(MapServer server, Stream<Waypoint> waypointList) {
        for (AbstractCacher cacher : cachers) {
            cacher.saveWaypoints(server, waypointList);
        }
    }

    @Override
    public synchronized List<Waypoint> loadWaypoints(MapServer server) {
        List<Waypoint> waypoints = new ArrayList<>();
        for (AbstractCacher cacher : cachers) {
            List<Waypoint> loaded = cacher.loadWaypoints(server);
            if (loaded != null) {
                waypoints.addAll(loaded);
            }
        }
        return waypoints;
    }

    @Override
    public void close() {
        for (AbstractCacher cacher : cachers) {
            cacher.close();
        }
    }

    public static record CacherEntry(AbstractCacher cache, Class<?> before, Class<?> after) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacherEntry)) return false;
            CacherEntry that = (CacherEntry) o;
            return Objects.equals(cache, that.cache);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cache);
        }

    }
}
