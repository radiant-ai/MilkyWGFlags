package fun.milkyway.milkywgflags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.papermc.paper.event.entity.ItemTransportingEntityValidateTargetEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Prevents Copper Golems from transporting items across region boundaries.
 * This protects against griefing where a golem enters a region to steal items
 * or exits a region to deposit items into a griefer's chest.
 */
public class CopperGolemProtectionListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onGolemValidateTarget(ItemTransportingEntityValidateTargetEvent event) {
        Location entityLocation = event.getEntity().getLocation();
        Location targetLocation = event.getBlock().getLocation();

        if (entityLocation.getWorld() != targetLocation.getWorld()) {
            event.setAllowed(false);
            return;
        }

        Set<ProtectedRegion> entityRegions = getRegions(entityLocation);
        Set<ProtectedRegion> targetRegions = getRegions(targetLocation);

        if (!entityRegions.equals(targetRegions)) {
            event.setAllowed(false);
        }
    }

    private Set<ProtectedRegion> getRegions(Location location) {
        RegionContainer container = MilkyWGFlags.getInstance().getWorldGuard().getPlatform().getRegionContainer();
        ApplicableRegionSet regions = container.createQuery().getApplicableRegions(BukkitAdapter.adapt(location));
        return regions.getRegions().stream()
                .filter(r -> !r.getId().equals(ProtectedRegion.GLOBAL_REGION))
                .collect(Collectors.toSet());
    }
}
