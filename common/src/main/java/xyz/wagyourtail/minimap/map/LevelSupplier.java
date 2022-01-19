package xyz.wagyourtail.minimap.map;

import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.function.Supplier;

public abstract class LevelSupplier {
    public abstract Set<String> getAvailableLevels();

    public abstract String getLevelName(Level level);

}
