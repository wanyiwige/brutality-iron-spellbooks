package cn.wanyiwige.brutalityiron;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import java.util.List;
import net.goo.brutality.magic.IBrutalitySpell.SpellCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Iron owns casting, mana and cooldown. Only original effect logic is invoked. */
public final class NativeSpell extends AbstractSpell {
    public final PortedLogic logic;
    private final DefaultConfig defaults;
    private final CastType type;

    public NativeSpell(PortedLogic logic) {
        this.logic = logic;
        type = logic.getCategories().contains(SpellCategory.CONTINUOUS) ? CastType.CONTINUOUS
                : logic.getCategories().contains(SpellCategory.CHANNELLING) ? CastType.LONG : CastType.INSTANT;
        var progression = SpellProgression.get(logic.getSpellName());
        var budget = SpellBudgets.get(logic.getSpellName());
        defaults = new DefaultConfig().setSchoolResource(Content.school(logic.getSchool()))
                .setMaxLevel(progression.maxLevel()).setMinRarity(progression.minimumRarity())
                .setCooldownSeconds(budget.cooldown()).build();
        baseManaCost = budget.mana();
        manaCostPerLevel = budget.growth();
        baseSpellPower = 1;
        spellPowerPerLevel = 0;
        castTime = type == CastType.CONTINUOUS ? 200 : Math.max(1, logic.getBaseCastTime());
    }

    @Override public ResourceLocation getSpellResource() { return BrutalityIron.id(logic.getSpellName()); }
    @Override public DefaultConfig getDefaultConfig() { return defaults; }
    @Override public CastType getCastType() { return type; }
    @Override public boolean requiresLearning() { return false; }
    @Override public boolean allowLooting() { return true; }

    @Override
    public SpellRarity getRarity(int rank) {
        // Preserve Iron's configured weights except for the requested legendary tail.
        return rank >= Math.max(1, getMaxLevel() - 1) ? SpellRarity.LEGENDARY : super.getRarity(rank);
    }

    @Override
    public int getMinLevelForRarity(SpellRarity rarity) {
        // Some rarity bands can disappear in short progressions. Do not craft a
        // scroll with an absent rarity or use the old weighted boundary formula.
        for (int rank = 1; rank <= getMaxLevel(); rank++) {
            if (getRarity(rank) == rarity) return rank;
        }
        return 0;
    }

    @Override
    public int getCastTime(int rank) {
        if (type == CastType.INSTANT) return 0;
        if (type == CastType.CONTINUOUS) return castTime;
        return Math.max(1, logic.getBaseCastTime() + logic.getCastTimeLevelScaling() * rank);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int rank, LivingEntity caster, MagicData data) {
        if (!(caster instanceof Player)) return false;
        return !logic.getSpellName().equals("annihilation") || caster.onGround();
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int rank, LivingEntity caster) {
        return List.of(Component.translatable("spell.brutality_iron." + logic.getSpellName() + ".guide"));
    }

    @Override
    public void onServerPreCast(Level level, int rank, LivingEntity caster, MagicData data) {
        super.onServerPreCast(level, rank, caster, data);
        if (type != CastType.CONTINUOUS || !(caster instanceof Player player)) return;
        var cast = new ContinuousData();
        data.setAdditionalCastData(cast);
        cast.started = logic.onStartCast(player, cast.stack, rank);
    }

    @Override
    public void onServerCastTick(Level level, int rank, LivingEntity caster, MagicData data) {
        if (type != CastType.CONTINUOUS || !(caster instanceof Player player)
                || !(data.getAdditionalCastData() instanceof ContinuousData cast) || !cast.started) return;
        cast.ticks++;
        cast.stack.getOrCreateTag().putInt("port_cast_ticks", cast.ticks);
        int interval = switch (logic.getSpellName()) {
            case "meteor_shower", "star_stream", "crescent_dart" -> Math.max(2,
                    logic.getBaseCastTime() + logic.getCastTimeLevelScaling() * rank);
            default -> 1;
        };
        if (cast.ticks % interval == 0) logic.onCastTick(player, cast.stack, rank);
    }

    @Override
    public void onCast(Level level, int rank, LivingEntity caster, CastSource source, MagicData data) {
        if (!level.isClientSide && type != CastType.CONTINUOUS && caster instanceof Player player) {
            ItemStack context = new ItemStack(Items.BOOK);
            if (logic.onStartCast(player, context, rank)) logic.onEndCast(player, context, rank);
        }
        super.onCast(level, rank, caster, source, data);
    }

    @Override
    public void onServerCastComplete(Level level, int rank, LivingEntity caster, MagicData data, boolean cancelled) {
        if (type == CastType.CONTINUOUS && caster instanceof Player player
                && data.getAdditionalCastData() instanceof ContinuousData cast && cast.started) {
            logic.onEndCast(player, cast.stack, rank);
            cast.started = false;
        }
        super.onServerCastComplete(level, rank, caster, data, cancelled);
    }

    private static final class ContinuousData implements ICastData {
        final ItemStack stack = new ItemStack(Items.BOOK);
        int ticks;
        boolean started;
        @Override public void reset() { started = false; }
    }
}
