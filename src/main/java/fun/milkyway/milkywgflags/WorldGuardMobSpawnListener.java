package fun.milkyway.milkywgflags;

import com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class WorldGuardMobSpawnListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPreSpawn(PreCreatureSpawnEvent event) {
        ApplicableRegionSet set =
                WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(event.getSpawnLocation()));

        if (set.testState(null, Flags.MOB_SPAWNING)) {
            return;
        }

        event.setCancelled(true);
    }

    /*@EventHandler(ignoreCancelled = true)
    public void onSpawnChunkMap(PlayerNaturallySpawnCreaturesEvent event) {
        var value = shouldCalculateSpawns(event.getPlayer(), event.getSpawnRadius() * 16 - 4);

        if (value) {
            return;
        }

        event.setCancelled(true);
    }*/

    private boolean shouldCalculateSpawns(Player player, int spawnRadius) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }
        var location = player.getLocation();
        var world = player.getWorld();
        var minY = world.getMinHeight();
        var maxY = world.getEnvironment() == World.Environment.NETHER ? 128 : world.getMaxHeight();
        var side = spawnRadius * 0.70711;
        var points = List.of(
                location.toBlockLocation(),
                new Location(world, location.getBlockX() + spawnRadius, location.getBlockY(), location.getBlockZ()),
                new Location(world, location.getBlockX() - spawnRadius, location.getBlockY(), location.getBlockZ()),
                new Location(world, location.getBlockX(), location.getBlockY(), location.getBlockZ() + spawnRadius),
                new Location(world, location.getBlockX(), location.getBlockY(), location.getBlockZ() - spawnRadius),
                new Location(world, location.getBlockX(), Math.min(maxY, location.getBlockY() + spawnRadius), location.getBlockZ()),
                new Location(world, location.getBlockX(), Math.max(minY, location.getBlockY() - spawnRadius), location.getBlockZ()),
                new Location(world, location.getBlockX() + side, location.getBlockY(), location.getBlockZ() + side),
                new Location(world, location.getBlockX() + side, location.getBlockY(), location.getBlockZ() - side),
                new Location(world, location.getBlockX() - side, location.getBlockY(), location.getBlockZ() + side),
                new Location(world, location.getBlockX() - side, location.getBlockY(), location.getBlockZ() - side)
        );
        var allows = 0;
        for (Location point : points) {
            if (WorldGuard.getInstance()
                    .getPlatform()
                    .getRegionContainer()
                    .createQuery()
                    .getApplicableRegions(BukkitAdapter.adapt(point))
                    .testState(null, Flags.MOB_SPAWNING)) {
                allows++;
            }
            if (allows > 2) {
                return true;
            }
        }
        return false;
    }
}
