package fun.milkyway.milkywgflags;

import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;

public class ItemFrameDestroyFixListener implements Listener {
    @EventHandler
    public void onItemFrameDestroy(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof ItemFrame itemFrame)) {
            return;
        }
        if (event.getCause() != HangingBreakEvent.RemoveCause.PHYSICS) {
            return;
        }
        var block = itemFrame.getLocation().getBlock().getRelative(itemFrame.getAttachedFace());
        if (!block.getType().isSolid()) {
            return;
        }
        event.setCancelled(true);
    }
}
