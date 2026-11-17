package cn.wanyiwige.brutalityiron;

import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class SchoolBook extends SpellBook implements GeoItem {
    public final BookContent.Definition definition;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public SchoolBook(BookContent.Definition definition) {
        super(definition.slots(), new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
        this.definition = definition;
        withSpellbookAttributes(
            new AttributeContainer(attribute("irons_spellbooks:max_mana"), definition.mana(), AttributeModifier.Operation.ADDITION),
            new AttributeContainer(attribute(definition.name().equals("cosmic_tome") && SchoolFallback.usingFallback(new ResourceLocation(definition.school())) ? "irons_spellbooks:ender_spell_power" : definition.powerAttribute()), definition.power(), AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(attribute("irons_spellbooks:" + definition.extraAttribute()), definition.extraValue(), AttributeModifier.Operation.MULTIPLY_BASE));
    }
    private static Supplier<Attribute> attribute(String id) {
        return () -> Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(id)), "Missing book attribute " + id);
    }
    @Override public ItemStack getDefaultInstance() {
        var stack = super.getDefaultInstance(); initializeSpellContainer(stack); return stack;
    }
    @Override public void onCraftedBy(ItemStack stack, Level level, net.minecraft.world.entity.player.Player player) {
        super.onCraftedBy(stack, level, player);
        initializeSpellContainer(stack);
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        initializeSpellContainer(stack);
    }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        if (definition.schoolType() != null) lines.add(Component.translatable("tooltip.brutality_iron.school_book",
                definition.schoolType().getDisplayName()).withStyle(ChatFormatting.GRAY));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private cn.wanyiwige.brutalityiron.client.SchoolBookRenderer renderer;
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new cn.wanyiwige.brutalityiron.client.SchoolBookRenderer(SchoolBook.this);
                return renderer;
            }
        });
    }
}
