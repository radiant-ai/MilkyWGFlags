package fun.milkyway.milkywgflags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.persistence.PersistentDataType;

public class RideFlagListener implements Listener {
    private static final NamespacedKey vehicleOwnerKey = new NamespacedKey(MilkyWGFlags.getInstance(), "vehicleOwner");

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClickHorse(PlayerInteractEntityEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        if (!isRidable(event.getRightClicked())) {
            return;
        }
        if (canRide(event.getPlayer(), event.getRightClicked().getLocation())) {
            return;
        }
        if (!event.getRightClicked().getPersistentDataContainer().getOrDefault(vehicleOwnerKey, PersistentDataType.STRING, "")
                .equals(event.getPlayer().getUniqueId().toString())) {
            return;
        }
        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMount(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) {
            return;
        }
        if (!isRidable(event.getVehicle())) {
            return;
        }
        if (canRide(player, event.getVehicle().getLocation())) {
            return;
        }
        if (event.getVehicle().getPersistentDataContainer().getOrDefault(vehicleOwnerKey, PersistentDataType.STRING, "")
                .equals(player.getUniqueId().toString())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player player)) {
            return;
        }
        if (event.getVehicle().getPassengers().size() > 1) {
            return;
        }
        event.getVehicle().getPersistentDataContainer().set(vehicleOwnerKey, PersistentDataType.STRING, player.getUniqueId().toString());
        if (!event.isCancelled() && canRide(player, event.getVehicle().getLocation())) {
            return;
        }
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Вы покинули свой транспорт в привате с отключенным флагом ride. Только вы сможете сесть обратно!"));
        event.setCancelled(false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        var player = event.getPlayer();

        if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN &&
                event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND) {
            return;
        }

        var vehicle = player.getVehicle();
        if (vehicle == null) {
            return;
        }

        if (!canRide(player, event.getTo())) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Вы не можете телепортироваться со своим транспортом: в точке прибытия отключен флаг ride."));
            return;
        }

        MilkyWGFlags.getInstance().getServer().getScheduler().runTaskLater(MilkyWGFlags.getInstance(), () -> {
            vehicle.teleportAsync(event.getTo()).thenRun(() -> {
                vehicle.addPassenger(player);
            });
        }, 1);
    }

    private boolean canRide(Player player, Location location) {
        var wrappedPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        var wrappedWorld = BukkitAdapter.adapt(location.getWorld());

        if (MilkyWGFlags.getInstance().getWorldGuard().getPlatform().getSessionManager().hasBypass(wrappedPlayer, wrappedWorld)) {
            return true;
        }

        var regionContainer = MilkyWGFlags.getInstance().getWorldGuard().getPlatform().getRegionContainer();

        var aLocation = BukkitAdapter.adapt(location);
        return regionContainer.createQuery().testBuild(aLocation, WorldGuardPlugin.inst().wrapPlayer(player), Flags.RIDE);
    }

    private boolean isRidable(Entity entity) {
        return entity instanceof AbstractHorse || entity instanceof RideableMinecart || entity instanceof Boat;
    }
}
