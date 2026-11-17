// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.cosmic;

import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.entity.cosmic.CosmicCataclysmEntity;
import net.goo.brutality.magic.BrutalitySpell;
import net.goo.brutality.magic.IBrutalitySpell;
import cn.wanyiwige.brutalityiron.logic.cosmic.MeteorShowerSpell;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;

public class MeteorShowerEntity
extends CosmicCataclysmEntity {
    public MeteorShowerEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.shouldApplyWaveEffect = false;
    }

    @Override
    public String texture() {
        return "cosmic_cataclysm";
    }

    @Override
    public String model() {
        return "cosmic_cataclysm";
    }

    @Override
    public String animation() {
        return "cosmic_cataclysm";
    }

    @Override
    public float getSizeScaling() {
        return 0.0f;
    }

    @Override
    public BrutalitySpell getSpell() {
        return new MeteorShowerSpell();
    }

    @Override
    public float getFinalDamage(IBrutalitySpell spell, Entity owner, int spellLevel) {
        return spell.getFinalDamage(owner, spellLevel);
    }
}

