package cn.wanyiwige.brutalityiron.mixin;

import cn.wanyiwige.brutalityiron.NativeSpell;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeMenu;
import io.redspace.ironsspellbooks.item.InkItem;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Iron 3.16.3 otherwise writes a level-zero scroll for a missing rarity band. */
@Mixin(value = ScrollForgeMenu.class, remap = false, priority = 2000)
public abstract class ScrollForgeMenuMixin {
    @Shadow private Slot inkSlot;
    @Shadow private Slot resultSlot;
    @Shadow private AbstractSpell spellRecipeSelection;
    @Inject(method = "setupResultSlot", at = @At("HEAD"), cancellable = true, remap = false)
    private void brutalityIron$rejectUnsupportedInk(AbstractSpell spell, CallbackInfo callback) {
        if ((spell instanceof NativeSpell || spell instanceof cn.wanyiwige.brutalityiron.skills.SkillSpell) && inkSlot.getItem().getItem() instanceof InkItem ink
                && spell.getMinLevelForRarity(ink.getRarity()) == 0) {
            resultSlot.set(ItemStack.EMPTY);
            callback.cancel();
        }
    }
    // Bielgg replaces setupResultSlot using its own cancellable HEAD injection.
    // Guard the two public callers as well, after their replacement has finished.
    @Inject(method = "setRecipeSpell", at = @At("RETURN"), remap = false)
    private void brutalityIron$validateSelectedResult(AbstractSpell spell, CallbackInfo callback) {
        brutalityIron$clearUnsupportedResult(spell);
    }
    @Inject(method = "onSlotsChanged", at = @At("RETURN"), remap = false)
    private void brutalityIron$validateChangedResult(int slot, CallbackInfo callback) {
        brutalityIron$clearUnsupportedResult(spellRecipeSelection);
    }
    @Unique private void brutalityIron$clearUnsupportedResult(AbstractSpell spell) {
        if ((spell instanceof NativeSpell || spell instanceof cn.wanyiwige.brutalityiron.skills.SkillSpell) && inkSlot.getItem().getItem() instanceof InkItem ink
                && spell.getMinLevelForRarity(ink.getRarity()) == 0 && !resultSlot.getItem().isEmpty())
            resultSlot.set(ItemStack.EMPTY);
    }
}
