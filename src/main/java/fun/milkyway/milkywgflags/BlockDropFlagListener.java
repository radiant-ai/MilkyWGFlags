package fun.milkyway.milkywgflags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockDropFlagListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        MilkyWGFlags plugin = MilkyWGFlags.getInstance();
        if (!plugin.isEnabled()) {
            return;
        }
        RegionContainer regionContainer = MilkyWGFlags.getInstance().getWorldGuard().getPlatform().getRegionContainer();
        Location location = BukkitAdapter.adapt(event.getBlock().getLocation());
        LocalPlayer player = WorldGuardPlugin.inst().wrapPlayer(event.getPlayer());
        StateFlag blockBreakFlag = MilkyWGFlags.BLOCK_DROP_FLAG;

        if (blockBreakFlag == null) {
            return;
        }
        RegionQuery query = regionContainer.createQuery();
        StateFlag.State state = query.queryValue(location, player, blockBreakFlag);

        if (state == null || state == StateFlag.State.ALLOW) {
            return;
        }
        event.setDropItems(false);
    }
}
