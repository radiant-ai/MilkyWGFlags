package fun.milkyway.milkywgflags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CaptureTheFlagFlagListener implements Listener {
    private static final List<Material> bannerMaterials = getBannerMaterials();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        var plugin = MilkyWGFlags.getInstance();

        if (!plugin.isEnabled()) {
            return;
        }

        if (event.getKeepInventory()) {
            return;
        }

        var regionContainer = MilkyWGFlags.getInstance().getWorldGuard().getPlatform().getRegionContainer();
        var location = BukkitAdapter.adapt(event.getPlayer().getLocation());

        if (MilkyWGFlags.CAPTURE_THE_FLAG == null) {
            return;
        }

        var state = regionContainer.createQuery().queryValue(location, null, MilkyWGFlags.CAPTURE_THE_FLAG);

        if (state == null || !state) {
            return;
        }

        if (MilkyWGFlags.KEEP_INVENTORY != null) {
            var keepInventory = regionContainer.createQuery().queryValue(location, null, MilkyWGFlags.KEEP_INVENTORY);
            if (Boolean.TRUE.equals(keepInventory)) {
                return;
            }
        }

        var removeIterator = event.getDrops().iterator();
        while (removeIterator.hasNext()) {
            var item = removeIterator.next();
            if (bannerMaterials.contains(item.getType())) {
                continue;
            }
            event.getItemsToKeep().add(item);
            removeIterator.remove();
        }
    }

    private static List<Material> getBannerMaterials() {
        return Collections.unmodifiableList(Arrays.stream(Material.values()).filter(value -> value.toString().endsWith("_BANNER")).toList());
    }
}
