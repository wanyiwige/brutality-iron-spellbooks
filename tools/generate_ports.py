"""Mechanically port the inspected 0.8.5 logic to official 1.20.1 names.

Original mechanics remain attributed to Brutality. Keep hand-maintained native
registration/lifecycle code outside the generated logic and entity directories.
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT.parent / 'work/brutality-iron-reference/source/net/goo/brutality'
JAVA = ROOT / 'src/main/java/cn/wanyiwige/brutalityiron'
MAPPING = Path('C:/Users/WANYIWIGE/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg')
names = {}
for line in MAPPING.read_text().splitlines():
    parts = line.strip().split()
    if parts and re.fullmatch(r'[mf]_\d+_', parts[0]):
        names[parts[0]] = parts[-1]

def mapped(text):
    return re.sub(r'\b[mf]_\d+_\b', lambda m: names.get(m[0], m[0]), text)

entity_source = (SOURCE / 'registry/BrutalityModEntities.java').read_text()
entity_defs = []
for line in entity_source.splitlines():
    m = re.search(r'RegistryObject<EntityType<(\w+)>> (\w+) = ENTITY_TYPES.register\("([^"]+)"', line)
    if m and list((SOURCE/'entity/spells').rglob(m[1]+'.java')):
        entity_defs.append((m[1], m[2], m[3], mapped(line.strip())))
entity_constants = {entry[1] for entry in entity_defs}

def route_damage(text, entity):
    """Retain the hit amount but use the native spell's damage type/attacker."""
    pattern = re.compile(r'\b(\w+)\.hurt\(')
    cursor = 0
    while (match := pattern.search(text, cursor)):
        start = match.end()
        depth = 1
        end = start
        while depth:
            if text[end] == '(':
                depth += 1
            elif text[end] == ')':
                depth -= 1
            end += 1
        args = text[start:end-1]
        nesting = 0
        split = None
        for i, char in enumerate(args):
            if char in '([{': nesting += 1
            elif char in ')]}': nesting -= 1
            elif char == ',' and nesting == 0:
                split = i
                break
        if split is None:
            raise ValueError(args)
        # owner and spell are available in every spell-projectile damage handler.
        logic = 'this.getSpell()' if entity else 'this'
        owner = 'this.getOwner()' if entity else 'player'
        replacement = f'NativeDamage.hurt({match[1]}, {args[split+1:].strip()}, {logic}, {owner}, '+('this' if entity else 'player')+')'
        text = text[:match.start()] + replacement + text[end:]
        cursor = match.start() + len(replacement)
    return text

spell_classes = []
for source, kind in [(SOURCE/'magic/spells', 'logic'), (SOURCE/'entity/spells', 'entity')]:
    for path in sorted(source.rglob('*.java')):
        text = path.read_text()
        text = text[text.index('package '):]
        text = mapped(text)
        text = text.replace('net.goo.brutality.magic.spells.', 'cn.wanyiwige.brutalityiron.logic.')
        text = text.replace('net.goo.brutality.entity.spells.', 'cn.wanyiwige.brutalityiron.entity.')
        text = text.replace('import net.goo.brutality.registry.BrutalityModEntities;', 'import cn.wanyiwige.brutalityiron.PortEntities;\nimport net.goo.brutality.registry.BrutalityModEntities;')
        for constant in entity_constants:
            text = text.replace('BrutalityModEntities.'+constant+'.', 'PortEntities.'+constant+'.')
        text = text.replace('import net.goo.brutality.magic.IBrutalitySpellEntity;', 'import cn.wanyiwige.brutalityiron.NativeSpellEntity;')
        text = re.sub(r'\bIBrutalitySpellEntity\b', 'NativeSpellEntity', text)
        if kind == 'logic':
            text = text.replace('extends BrutalitySpell', 'extends PortedLogic')
            text = text.replace('import net.goo.brutality.magic.BrutalitySpell;', 'import cn.wanyiwige.brutalityiron.PortedLogic;')
            spell_classes.append((path.parent.name, path.stem))
        text = text.replace('import java.util.List;', 'import java.util.List;\nimport cn.wanyiwige.brutalityiron.NativeDamage;')
        if 'import cn.wanyiwige.brutalityiron.NativeDamage;' not in text:
            text = text.replace('\n\n', '\n\nimport cn.wanyiwige.brutalityiron.NativeDamage;\n', 1)
        if kind == 'logic' or 'getSpell()' in text:
            text = route_damage(text, kind == 'entity')
        text = text.replace('(Object)', '')
        text = text.replace('List nearbyEntities =', 'List<Entity> nearbyEntities =')
        text = text.replace('List nearbyLivingEntities =', 'List<LivingEntity> nearbyLivingEntities =')
        if path.stem == 'CosmicCataclysmEntity':
            text = text.replace('level = this.getOwner();', 'Entity ownerEntity = this.getOwner();')
            text = text.replace('if (level instanceof LivingEntity)', 'if (ownerEntity instanceof LivingEntity)')
            text = text.replace('(LivingEntity)level;', '(LivingEntity)ownerEntity;')
        if path.stem == 'IntangibleSpell':
            text = text.replace('boolean onCastTick(', 'boolean onStartCast(')
        if path.stem == 'DestructionSpell':
            text = text.replace('player.level().addFreshEntity((Entity)destructionEntity);', '''player.level().addFreshEntity((Entity)destructionEntity);
        for (var target : player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                player.getBoundingBox().inflate(3), e -> e != player && !player.isAlliedTo(e))) {
            if (target.distanceToSqr(player) <= 9) NativeDamage.hurt(target, getFinalDamage(player, spellLevel), this, player, destructionEntity);
        }''')
        if path.stem == 'NightfallSpell':
            text = text.replace('serverLevel.setDayTime(18000L);\n            DelayedTaskScheduler.queueServerWork((Level)serverLevel, duration, () -> serverLevel.setDayTime(originalTime));', 'cn.wanyiwige.brutalityiron.NightfallTime.begin(serverLevel, duration);')
        if path.stem == 'MeteorShowerEntity':
            text = text.replace('return 7.0f;', 'return spell.getFinalDamage(owner, spellLevel);')
        if path.stem == 'ExtinctionEntity':
            text = text.replace('ClientAccess.playExtinctionSpellSound(this);', 'cn.wanyiwige.brutalityiron.client.ClientSounds.play(this);')
            text = text.replace('ClientAccess.stopExtinctionSpellSound();', '// Each sound instance stops when its own entity is removed.')
        if path.stem == 'DestructionEntity':
            text = text.replace('logic.brimwielder.AnnihilationSpell', 'logic.brimwielder.DestructionSpell').replace('new AnnihilationSpell()', 'new DestructionSpell()')
        if path.stem == 'DivineRetributionSpell':
            text = text.replace('net.mcreator.terramity.entity.HolyBeamEntity', 'cn.wanyiwige.brutalityiron.entity.celestia.HolyBeamEntity')
            text = text.replace('TerramityModEntities.HOLY_BEAM', 'cn.wanyiwige.brutalityiron.PortEntities.HOLY_BEAM')
            text = text.replace('player.level().addFreshEntity((Entity)holyBeam);', 'holyBeam.getPersistentData().putUUID("port_owner", player.getUUID());\n        holyBeam.getPersistentData().putInt("port_rank", spellLevel);\n        player.level().addFreshEntity((Entity)holyBeam);')
        # Native casting tracks its own elapsed ticks. It does not use the player's held-item timer.
        if path.stem == 'BrimspikeSpell':
            text = text.replace('(float)(stack.getUseDuration() - player.getUseItemRemainingTicks())', '(float)stack.getOrCreateTag().getInt("port_cast_ticks")')
        if path.stem == 'DematerializeSpell':
            text = text.replace('targetPlayer2.setHealth(0.0f);', '')
            text = text.replace('2.14748365E9f', 'getFinalDamage(player, spellLevel)')
            text = text.replace('livingTarget.discard();', 'NativeDamage.hurt(livingTarget, getFinalDamage(player, spellLevel), this, player, player);')
            text = text.replace('LivingEntity livingTarget = (LivingEntity)target;', 'LivingEntity livingTarget = (LivingEntity)target;\n        if (target == player || player.isAlliedTo(target)) return false;')
        if path.stem in ('AnnihilationEntity', 'ChthonicCapsuleEntity'):
            text = text.replace('this.getOwner(), null, null,', 'this.getOwner(), cn.wanyiwige.brutalityiron.Content.spell(getSpell().getSpellName()).getDamageSource(this, getOwner()), null,')
        # Distinct spell entities are created for every native cast, so lookup is scoped to this addon.
        target = JAVA/kind/path.relative_to(source)
        target.parent.mkdir(parents=True, exist_ok=True)
        if 'class PiercingMoonlight\n' in text:
            text=text.replace('import net.goo.brutality.network.ClientboundPreciseAddEntityPacket;','')
            text=text.replace('implements NativeSpellEntity {','implements NativeSpellEntity, net.minecraftforge.entity.IEntityAdditionalSpawnData {')
            text=text.replace('return new ClientboundPreciseAddEntityPacket((Entity)this, entity == null ? 0 : entity.getId());','return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);')
            text=text.replace('    public void setYRot(float pYRot) {','    @Override public void writeSpawnData(net.minecraft.network.FriendlyByteBuf data) { data.writeVarInt(getOwner()==null?0:getOwner().getId()); }\n    @Override public void readSpawnData(net.minecraft.network.FriendlyByteBuf data) { setOwner(level().getEntity(data.readVarInt())); }\n    public void setYRot(float pYRot) {')
        target.write_text('// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.\n'+text, encoding='utf-8')

client = JAVA/'client'
client.mkdir(exist_ok=True)
sound = (SOURCE/'sounds/ExtinctionSpellSoundInstance.java').read_text()
sound = mapped(sound[sound.index('package '):]).replace('package net.goo.brutality.sounds;', 'package cn.wanyiwige.brutalityiron.client;')
sound = sound.replace('net.goo.brutality.entity.spells.', 'cn.wanyiwige.brutalityiron.entity.')
(client/'ExtinctionSpellSoundInstance.java').write_text(sound, encoding='utf-8')

# Holy Beam had hardcoded Terramity damage without an owner. Use its visual entity
# with a separately implemented native damage/lifetime procedure.
terra = SOURCE.parents[3]/'terra/net/mcreator/terramity'
for rel, dest in [('entity/HolyBeamEntity.java', 'entity/celestia'), ('entity/model/HolyBeamModel.java', 'client'),
                  ('entity/layer/HolyBeamLayer.java', 'client'), ('client/renderer/HolyBeamRenderer.java', 'client')]:
    text = (terra/rel).read_text()
    text = mapped(text[text.index('package '):]).replace('(Object)', '')
    text = re.sub(r'package [^;]+;', 'package cn.wanyiwige.brutalityiron.'+dest.replace('/', '.')+';', text, count=1)
    for old, new in [('net.mcreator.terramity.entity.HolyBeamEntity', 'cn.wanyiwige.brutalityiron.entity.celestia.HolyBeamEntity'),
                     ('net.mcreator.terramity.entity.model.HolyBeamModel', 'cn.wanyiwige.brutalityiron.client.HolyBeamModel'),
                     ('net.mcreator.terramity.entity.layer.HolyBeamLayer', 'cn.wanyiwige.brutalityiron.client.HolyBeamLayer'),
                     ('net.mcreator.terramity.procedures.HolyBeamOnEntityTickUpdateProcedure', 'cn.wanyiwige.brutalityiron.BeamProcedure')]:
        text = text.replace(old, new)
    text = text.replace('HolyBeamOnEntityTickUpdateProcedure.execute', 'BeamProcedure.execute')
    text = text.replace('TerramityModEntities.HOLY_BEAM', 'cn.wanyiwige.brutalityiron.PortEntities.HOLY_BEAM')
    text = text.replace('super.preRender(poseStack, (Entity)entity,', 'super.preRender(poseStack, entity,')
    text = text.replace('this.getTextureLocation((Entity)animatable)', 'this.getTextureLocation(animatable)')
    text = text.replace('this.getDefaultBakedModel((GeoAnimatable)animatable)', 'this.getDefaultBakedModel(animatable)')
    text = text.replace('bufferSource, (GeoAnimatable)animatable,', 'bufferSource, animatable,')
    (JAVA/dest/Path(rel).name).write_text(text, encoding='utf-8')

imports = '\n'.join(f'import cn.wanyiwige.brutalityiron.entity.{p.parent.name}.{p.stem};' for p in sorted((SOURCE/'entity/spells').rglob('*.java')))
imports += '\nimport cn.wanyiwige.brutalityiron.entity.celestia.HolyBeamEntity;'
definitions = []
for cls, constant, identifier, line in entity_defs:
    expression = line[line.index('=')+1:].strip().replace('ENTITY_TYPES.register', 'REGISTRY.register')
    expression = expression.replace('.setUpdateInterval(1)', '')
    expression = expression.replace('EntityType.Builder.of(', f'EntityType.Builder.<{cls}>of(')
    expression = expression.replace(f'.build("{identifier}")', f'.clientTrackingRange(96).updateInterval(1).build("brutality_iron:{identifier}")')
    definitions.append(f'    public static final RegistryObject<EntityType<{cls}>> {constant} = {expression}')
(JAVA/'PortEntities.java').write_text('''package cn.wanyiwige.brutalityiron;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;
'''+imports+'''
public final class PortEntities {
    private static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BrutalityIron.ID);
'''+ '\n'.join(definitions)+'''
    public static final RegistryObject<EntityType<HolyBeamEntity>> HOLY_BEAM = REGISTRY.register("holy_beam", () -> EntityType.Builder.<HolyBeamEntity>of(HolyBeamEntity::new, MobCategory.MISC).sized(1, 1).clientTrackingRange(96).updateInterval(1).build("brutality_iron:holy_beam"));
    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
        bus.addListener((net.minecraftforge.event.entity.EntityAttributeCreationEvent event) -> event.put(HOLY_BEAM.get(), HolyBeamEntity.createAttributes().build()));
    }
    private PortEntities() {}
}
''', encoding='utf-8')

imports = '\n'.join(f'import cn.wanyiwige.brutalityiron.logic.{school}.{name};' for school,name in spell_classes)
ctors = ',\n            '.join(f'new {name}()' for _,name in spell_classes)
(JAVA/'SpellCatalog.java').write_text('''package cn.wanyiwige.brutalityiron;
import java.util.List;
'''+imports+'''
public final class SpellCatalog {
    public static List<PortedLogic> create() {
        return List.of(
            '''+ctors+''');
    }
    private SpellCatalog() {}
}
''', encoding='utf-8')
print(f'Ported {len(spell_classes)} spell logic classes and {len(entity_defs)} entity types')
