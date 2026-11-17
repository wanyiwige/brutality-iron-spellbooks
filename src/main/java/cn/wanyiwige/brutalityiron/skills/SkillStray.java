package cn.wanyiwige.brutalityiron.skills;

import java.util.UUID;
import net.goo.brutality.entity.mobs.SummonedStray;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SkillStray extends SummonedStray implements OwnableEntity {
    public int remaining=300;public float spellDamage=4;
    public SkillStray(EntityType<? extends net.minecraft.world.entity.monster.Stray> type,Level level){super(type,level);}
    @Override public UUID getOwnerUUID(){return summonerUUID;}
    @Override public LivingEntity getOwner(){return getSummoner();}
    @Override public boolean canAttack(LivingEntity target){return getSummoner() instanceof Player p&&SkillRuntime.enemy(p,target);}
    @Override protected AbstractArrow getArrow(ItemStack stack,float distance){var arrow=super.getArrow(stack,distance);arrow.setBaseDamage(Math.min(8,spellDamage*.6));return arrow;}
    @Override public void tick(){if(!level().isClientSide&&!SkillContent.SKILLS.containsKey("stray_legion")){discard();return;}super.tick();if(level().isClientSide)return;if(--remaining<=0||getSummoner()==null||!getSummoner().isAlive()){discard();return;}if(tickCount%20==0&&getSummoner() instanceof Player p){var enemies=SkillRuntime.targets(p,position(),18);if(!enemies.isEmpty())setTarget(enemies.get(0));}}
    @Override protected void dropAllDeathLoot(net.minecraft.world.damagesource.DamageSource source){}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putInt("SkillRemaining",remaining);n.putFloat("SpellDamage",spellDamage);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);remaining=Math.min(n.getInt("SkillRemaining"),40);spellDamage=n.getFloat("SpellDamage");}
}
