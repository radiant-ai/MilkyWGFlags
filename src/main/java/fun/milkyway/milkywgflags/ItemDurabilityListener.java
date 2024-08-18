package fun.milkyway.milkywgflags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.Arrays;

public class ItemDurabilityListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemDamage(PlayerItemDamageEvent event) {
        var player = event.getPlayer();
        var item = event.getItem();
        var location = player.getLocation();

        var regionContainer = MilkyWGFlags.getInstance().getWorldGuard().getPlatform().getRegionContainer();
        var query = regionContainer.createQuery();
        var set = query.getApplicableRegions(BukkitAdapter.adapt(location));

        var state = set.queryValue(null, MilkyWGFlags.NO_DURABILITY);

        if (state == null) {
            return;
        }

        if (Arrays.stream(state).anyMatch(m -> item.getType().equals(m))) {
            event.setCancelled(true);
        }
    }
}
