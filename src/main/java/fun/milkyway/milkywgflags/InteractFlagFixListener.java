package fun.milkyway.milkywgflags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;

public class InteractFlagFixListener implements Listener {
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (canInteract(event.getPlayer(), event.getEntity().getLocation())) {
            return;
        }

        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
        if (canInteract(event.getPlayer(), event.getEntity().getLocation())) {
            return;
        }

        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        var player = event.getPlayer();
        var item = player.getInventory().getItem(event.getHand());
        if (item != null && item.getType() == org.bukkit.Material.SHEARS) {
            return;
        }

        if (canInteract(event.getPlayer(), event.getEntity().getLocation())) {
            return;
        }

        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEntityWithShears(PlayerInteractEntityEvent event) {
        var player = event.getPlayer();
        var item = player.getInventory().getItem(event.getHand());
        if (item == null || item.getType() != org.bukkit.Material.SHEARS) {
            return;
        }
        
        if (canInteract(player, event.getRightClicked().getLocation())) {
            return;
        }

        event.setCancelled(true);
    }
    
    private boolean canInteract(Player player, Location location) {
        var wrappedPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        var wrappedWorld = BukkitAdapter.adapt(player.getWorld());
        
        if (MilkyWGFlags.getInstance().getWorldGuard().getPlatform().getSessionManager().hasBypass(wrappedPlayer, wrappedWorld)) {
            return true;
        }
        
        var regionContainer = MilkyWGFlags.getInstance().getWorldGuard().getPlatform().getRegionContainer();
        var adaptedLocation = BukkitAdapter.adapt(location);
        
        return regionContainer.createQuery().testBuild(adaptedLocation, wrappedPlayer, Flags.INTERACT);
    }
}
