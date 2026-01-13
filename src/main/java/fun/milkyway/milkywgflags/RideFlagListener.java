package fun.milkyway.milkywgflags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;

import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RideFlagListener implements Listener {
    private static final NamespacedKey vehicleOwnersKey = new NamespacedKey(MilkyWGFlags.getInstance(), "vehicleOwners");

    @EventHandler(priority = EventPriority.HIGH)
    public void onMount(EntityMountEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (canRide(player, event.getEntity().getLocation())) {
            return;
        }
        
        var vehicle = event.getMount();
        var currentPassengers = vehicle.getPassengers();
        var owners = getVehicleOwners(vehicle);
        var playerUuid = player.getUniqueId().toString();
        
        if (currentPassengers.isEmpty()) {
            if (!owners.isEmpty() && !owners.contains(playerUuid)) {
                setVehicleOwners(vehicle, Set.of(playerUuid));
                event.setCancelled(false);
                return;
            }
            if (owners.contains(playerUuid)) {
                event.setCancelled(false);
                return;
            }
            return;
        }
        
        var updatedOwners = new HashSet<>(owners);
        updatedOwners.add(playerUuid);
        currentPassengers.stream()
            .filter(p -> p instanceof Player)
            .map(p -> ((Player) p).getUniqueId().toString())
            .forEach(updatedOwners::add);
        setVehicleOwners(vehicle, updatedOwners);
        
        if (updatedOwners.contains(playerUuid)) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player player)) {
            return;
        }
        
        var vehicle = event.getVehicle();
        var remainingPassengers = vehicle.getPassengers().stream()
            .filter(p -> p != player && p instanceof Player)
            .map(p -> ((Player) p).getUniqueId().toString())
            .collect(Collectors.toSet());
        remainingPassengers.add(player.getUniqueId().toString());
        
        setVehicleOwners(vehicle, remainingPassengers);
        
        if (!event.isCancelled() && canRide(player, vehicle.getLocation())) {
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

        event.setCancelled(true);

        vehicle.teleportAsync(event.getTo(), PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.MOUNT) {
            return;
        }
        if (!(event.getEntity() instanceof HappyGhast)) {
            return;
        }
        event.setCancelled(false);
        MilkyWGFlags.getInstance().getLogger().info("Uncancelled HappyGhast spawn");
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
    
    private Set<String> getVehicleOwners(org.bukkit.entity.Entity vehicle) {
        var ownersString = vehicle.getPersistentDataContainer().getOrDefault(vehicleOwnersKey, PersistentDataType.STRING, "");
        if (ownersString.isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(ownersString.split(","))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }
    
    private void setVehicleOwners(org.bukkit.entity.Entity vehicle, Set<String> owners) {
        var ownersString = String.join(",", owners);
        vehicle.getPersistentDataContainer().set(vehicleOwnersKey, PersistentDataType.STRING, ownersString);
    }
}
