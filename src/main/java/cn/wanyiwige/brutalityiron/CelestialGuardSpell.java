package cn.wanyiwige.brutalityiron;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import java.util.List;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class CelestialGuardSpell extends AbstractSpell {
    private final DefaultConfig defaults=new DefaultConfig().setSchoolResource(BrutalityIron.id("celestia"))
            .setMaxLevel(5).setMinRarity(SpellRarity.LEGENDARY).setCooldownSeconds(45).build();
    public CelestialGuardSpell(){baseManaCost=60;manaCostPerLevel=5;baseSpellPower=1;spellPowerPerLevel=0;castTime=20;}
    @Override public ResourceLocation getSpellResource(){return BrutalityIron.id("celestial_guard");}
    @Override public DefaultConfig getDefaultConfig(){return defaults;}
    @Override public CastType getCastType(){return CastType.LONG;}
    @Override public boolean requiresLearning(){return false;}
    @Override public boolean allowLooting(){return true;}
    @Override public SpellRarity getRarity(int rank){return SpellRarity.LEGENDARY;}
    @Override public int getMinLevelForRarity(SpellRarity rarity){return rarity==SpellRarity.LEGENDARY?1:0;}
    @Override public boolean checkPreCastConditions(Level level,int rank,LivingEntity caster,MagicData data){return caster instanceof Player;}
    @Override public List<MutableComponent> getUniqueInfo(int rank,LivingEntity caster){return List.of(
        Component.translatable("spell.brutality_iron.celestial_guard.guide"),
        Component.literal(String.format(java.util.Locale.ROOT,"%d s · %.1f m · 3",CelestialGuard.duration(rank)/20,CelestialGuard.radius(rank))));}
    @Override public void onCast(Level level,int rank,LivingEntity caster,CastSource source,MagicData data){
        if(!level.isClientSide&&caster instanceof Player p)CelestialGuard.cast(p,rank);
        super.onCast(level,rank,caster,source,data);
    }
}
