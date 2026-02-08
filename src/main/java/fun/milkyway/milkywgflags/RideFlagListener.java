package fun.milkyway.milkywgflags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;

import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
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
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        var vehicle = event.getMount();
        var currentPassengers = vehicle.getPassengers();
        var playerUuid = player.getUniqueId().toString();
        var owners = getVehicleOwners(vehicle);

        // Transfer ownership when becoming the primary rider (mounting an empty vehicle)
        if (!event.isCancelled()) {
            if (currentPassengers.isEmpty()) {
                setVehicleOwners(vehicle, Set.of(playerUuid));
            }
            return;
        }

        if (canRide(player, event.getEntity().getLocation())) {
            return;
        }

        // Allow joining as 2nd passenger without granting ownership
        if (!currentPassengers.isEmpty()) {
            event.setCancelled(false);
            return;
        }

        // Empty vehicle: only existing owners may remount
        if (owners.contains(playerUuid)) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player player)) {
            return;
        }

        var vehicle = event.getVehicle();
        var canRide = canRide(player, vehicle.getLocation());

        if (!event.isCancelled() && canRide) {
            return;
        }

        if (!canRide) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Вы покинули свой транспорт в привате с отключенным флагом ride. Только вы сможете сесть обратно!"));
        }
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

        if (vehicle.getPassengers().size() > 1) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Вы не можете телепортироваться с другими пассажирами в транспорте!"));
            return;
        }

        var from = event.getFrom();
        vehicle.teleportAsync(event.getTo(), PlayerTeleportEvent.TeleportCause.UNKNOWN)
            .thenAccept(success -> {
                if (success) {
                    updateBackLocation(player, from);
                }
            });
    }

    private void updateBackLocation(Player player, Location from) {
        if (Bukkit.getPluginManager().getPlugin("CMI") == null) {
            return;
        }
        setCMIBackLocation(player, from);
    }

    // Isolated to avoid class loading CMIUser when CMI is absent
    private void setCMIBackLocation(Player player, Location from) {
        var user = CMIUser.getUser(player);
        if (user == null) {
            return;
        }
        user.setLastTeleportLocation(from);
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
