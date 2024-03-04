package fun.milkyway.milkywgflags;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import me.neznamy.tab.api.TabAPI;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class ShowNamesHandler extends FlagValueChangeHandler<StateFlag.State> {
    private BukkitTask delayedTask;
    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<ShowNamesHandler> implements Listener {
        @Override
        public ShowNamesHandler create(Session session) {
            return new ShowNamesHandler(session, MilkyWGFlags.SHOW_NAMES_FLAG);
        }
    }

    protected ShowNamesHandler(Session session, Flag<StateFlag.State> flag) {
        super(session, flag);
    }

    @Override
    protected void onInitialValue(LocalPlayer localPlayer, ApplicableRegionSet applicableRegionSet, StateFlag.State state) {
        updateNameTagRetry(localPlayer.getUniqueId(), state);
    }

    @Override
    protected boolean onSetValue(LocalPlayer localPlayer, Location location, Location location1, ApplicableRegionSet applicableRegionSet, StateFlag.State state, StateFlag.State t1, MoveType moveType) {
        updateNameTagRetry(localPlayer.getUniqueId(), state);
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer localPlayer, Location location, Location location1, ApplicableRegionSet applicableRegionSet, StateFlag.State state, MoveType moveType) {
        updateNameTagRetry(localPlayer.getUniqueId(), state);
        return true;
    }

    private void updateNameTagRetry(UUID uuid, StateFlag.State state) {
        if (delayedTask != null) {
            delayedTask.cancel();
            delayedTask = null;
        }
        try {
            updateNameTag(uuid, state);
        } catch (Exception e) {
            delayedTask = new BukkitRunnable() {
                private int countLeft = 6;
                @Override
                public void run() {
                    if (countLeft-- <= 0) {
                        delayedTask = null;
                        cancel();
                        return;
                    }
                    try {
                        updateNameTag(uuid, state);
                        delayedTask = null;
                        cancel();
                    } catch (Exception e) {
                        updateNameTagRetry(uuid, state);
                    }
                }
            }.runTaskTimer(MilkyWGFlags.getInstance(), 10L,10L);
        }
    }

    private static void updateNameTag(UUID uuid, StateFlag.State state) {
        var nameTagManager = TabAPI.getInstance().getNameTagManager();
        if (nameTagManager == null) {
            return;
        }
        var tabPlayer = TabAPI.getInstance().getPlayer(uuid);
        if (state == StateFlag.State.DENY) {
            nameTagManager.hideNameTag(tabPlayer);
        } else {
            nameTagManager.showNameTag(tabPlayer);
        }
    }
}
