package fun.milkyway.milkywgflags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class AmbientEffectHandler extends FlagValueChangeHandler<StateFlag.State> {
    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<AmbientEffectHandler> {
        @Override
        public AmbientEffectHandler create(Session session) {
            return new AmbientEffectHandler(session, MilkyWGFlags.AMBIENT_EFFECT);
        }
    }

    protected AmbientEffectHandler(Session session, Flag<StateFlag.State> flag) {
        super(session, flag);
    }

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, StateFlag.State value) {
        handleAmbientEffects(player, value);
    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet set, StateFlag.State currentValue, StateFlag.State lastValue, MoveType moveType) {
        handleAmbientEffects(player, currentValue);
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet set, StateFlag.State lastValue, MoveType moveType) {
        handleAmbientEffects(player, StateFlag.State.ALLOW);
        return true;
    }

    private void handleAmbientEffects(LocalPlayer localPlayer, StateFlag.State state) {
        Player player = BukkitAdapter.adapt(localPlayer);
        if (player == null) return;

        if (state == StateFlag.State.DENY) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.isAmbient()) {
                    player.removePotionEffect(effect.getType());
                }
            }
        }
    }
}
