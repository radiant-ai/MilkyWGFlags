package fun.milkyway.milkywgflags;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class MilkyWGFlags extends JavaPlugin {
    private static MilkyWGFlags instance;

    private WorldGuard worldGuard;

    public static StateFlag BLOCK_DROP_FLAG = new StateFlag("block-drop", true);
    public static BooleanFlag CAPTURE_THE_FLAG = new BooleanFlag("capture-the-flag");

    @Override
    public void onEnable() {
        if (worldGuard == null) {
            setEnabled(false);
        }

        getServer().getPluginManager().registerEvents(new BlockDropFlagListener(), this);
        getServer().getPluginManager().registerEvents(new CaptureTheFlagFlagListener(), this);
        getServer().getPluginManager().registerEvents(new ItemFrameDestroyFixListener(), this);
        getServer().getPluginManager().registerEvents(new WorldGuardMobSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new RideFlagListener(), this);
    }

    @Override
    public void onLoad() {
        instance = this;
        worldGuard = WorldGuard.getInstance();
        registerFlags();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void registerFlags() {
        FlagRegistry flagRegistry = worldGuard.getFlagRegistry();
        try {
            flagRegistry.register(BLOCK_DROP_FLAG);
        } catch (FlagConflictException e) {
            if (!(flagRegistry.get("block-drop") instanceof StateFlag)) {
                getLogger().severe("Flag block-drop already existed while tried to register and it had a wrong type!");
            }
        }
        try {
            flagRegistry.register(CAPTURE_THE_FLAG);
        } catch (FlagConflictException e) {
            if (!(flagRegistry.get("capture-the-flag") instanceof StateFlag)) {
                getLogger().severe("Flag capture-the-flag already existed while tried to register and it had a wrong type!");
            }
        }
    }

    public static @NotNull MilkyWGFlags getInstance() {
        return instance;
    }

    public @NotNull WorldGuard getWorldGuard() {
        return worldGuard;
    }
}
