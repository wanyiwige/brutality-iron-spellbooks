package cn.wanyiwige.brutalityiron;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** One persisted lease for the server's shared day clock; recasts extend it. */
@Mod.EventBusSubscriber(modid = BrutalityIron.ID)
public final class NightfallTime extends SavedData {
    private long original, start, end;
    private boolean active;
    public static NightfallTime load(CompoundTag tag) {
        var state = new NightfallTime();
        state.original = tag.getLong("original"); state.start = tag.getLong("start");
        state.end = tag.getLong("end"); state.active = tag.getBoolean("active");
        return state;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        tag.putLong("original", original); tag.putLong("start", start);
        tag.putLong("end", end); tag.putBoolean("active", active); return tag;
    }
    private static NightfallTime state(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(NightfallTime::load, NightfallTime::new, "brutality_iron_nightfall");
    }
    public static void begin(ServerLevel level, int ticks) {
        var world = level.getServer().overworld();
        var state = state(world); long now = world.getGameTime();
        if (!state.active) { state.original = world.getDayTime(); state.start = now; }
        state.end = Math.max(state.active ? state.end : now, now + ticks);
        state.active = true; state.setDirty();
        world.setDayTime(world.getDayTime() / 24000 * 24000 + 18000);
    }
    @SubscribeEvent public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)
                || level != level.getServer().overworld()) return;
        var state = state(level);
        if (state.active && level.getGameTime() >= state.end) {
            level.setDayTime(state.original + level.getGameTime() - state.start);
            state.active = false; state.setDirty();
        }
    }
}
