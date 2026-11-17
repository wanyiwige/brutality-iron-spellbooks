"""Prepare native scrolls, focus tags, translations, and spell-icon resources."""
from pathlib import Path
import io
import json
import re
import shutil
import zipfile
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT/'src/main/resources'
ASSET = RES/'assets/brutality_iron'
DATA = RES/'data/brutality_iron'

def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')

fallback = {
    'destruction': ('毁灭', 'Destruction', '在身前释放毁灭冲击，伤害周围敌人。'),
    'duo_lunaris': ('双月', 'Duo Lunaris', '依据时刻恢复生命，将溢出治疗转化为伤害吸收。'),
    'crescent_scythe': ('新月之镰', 'Crescent Scythe', '掷出旋转的新月镰刀，夜间伤害提升。'),
    'piercing_moonlight': ('贯穿月光', 'Piercing Moonlight', '释放贯穿月光，夜间强化，对特定距离的目标造成额外伤害。'),
    'crescent_dart': ('新月飞镖', 'Crescent Dart', '持续发射能够穿透目标的新月飞镖。'),
}
zh = {'itemGroup.brutality_iron.spells': 'Brutality：铁魔法法术'}
en = {'itemGroup.brutality_iron.spells': 'Brutality · Iron Spellbooks'}
inventory = []
progression = json.loads((ASSET/'spell_progression.json').read_text(encoding='utf-8'))
with zipfile.ZipFile(ROOT/'libs/brutality-0.8.5.jar') as jar:
    original_zh = json.loads(jar.read('assets/brutality/lang/zh_cn.json'))
    original_en = json.loads(jar.read('assets/brutality/lang/en_us.json'))
    for path in sorted((ROOT/'src/main/java/cn/wanyiwige/brutalityiron/logic').rglob('*.java')):
        text = path.read_text(encoding='utf-8')
        name = re.search(r'"([a-z_]+)", \d+,', text)[1]
        old_school = path.parent.name
        key = 'spell.brutality.'+name
        chinese = original_zh.get(key, fallback.get(name, (name, name, ''))[0])
        english = original_en.get(key, fallback.get(name, (name, name, ''))[1])
        desc_zh = re.sub('§.', '', original_zh.get(key+'.description.1', fallback.get(name, ('','',''))[2]))
        desc_en = re.sub('§.', '', original_en.get(key+'.description.1', english))
        if name == 'sacred_oath':
            desc_zh = '立下神圣誓言，短暂获得相位状态。'
            desc_en = 'Take a sacred oath and briefly gain Phasing.'
        if name == 'dematerialize':
            desc_zh = '目标生命值低于阈值时，对其施加解离伤害；受法术抗性影响。'
            desc_en = 'Deal dissolution damage when the target is below the health threshold; spell resistance applies.'
        if name == 'edge_of_oblivion':
            desc_zh = '释放扩散的反物质冲击波，击退并伤害附近生物。'
            desc_en = 'Release an expanding antimatter wave that pushes back and damages nearby creatures.'
        for lang, title, desc in [(zh,chinese,desc_zh),(en,english,desc_en)]:
            lang['spell.brutality_iron.'+name] = title
            lang['spell.brutality_iron.'+name+'.guide'] = desc
        old_icon = f'assets/brutality/textures/gui/spells/{old_school}/{name}.png'
        dest = ASSET/f'textures/gui/spell_icons/{name}.png'
        dest.parent.mkdir(parents=True, exist_ok=True)
        if old_icon in jar.namelist():
            dest.write_bytes(jar.read(old_icon))
        else:
            # Missing original art receives a compact native-pixel school motif.
            im=Image.new('RGBA',(16,16)); draw=ImageDraw.Draw(im)
            if old_school == 'umbrancy':
                draw.ellipse((2,1,13,14), fill='#bdd5f8')
                draw.ellipse((6,0,15,11), fill=(0,0,0,0))
                if name == 'piercing_moonlight': draw.line((0,14,15,1),fill='#ffffff',width=2)
                if name == 'crescent_dart': draw.polygon([(9,9),(15,12),(11,15)],fill='#718fc7')
            elif old_school == 'brimwielder':
                draw.polygon([(1,12),(4,5),(6,8),(9,1),(14,12),(9,15)],fill='#e51a00')
                draw.polygon([(5,12),(8,6),(11,12),(9,14)],fill='#ffc7bf')
            else:
                draw.ellipse((2,2,13,13),outline='#dbe8ff',width=2)
                draw.line((3,12,12,3),fill='#8198d7',width=2)
            im.save(dest)
        target_school = {'voidwalker':'brutality_iron:nihility', 'celestia':'brutality_iron:celestia',
                         'brimwielder':'brutality_iron:inferno', 'cosmic':'alex_caves_spellsbook:hazen_cosmic',
                         'umbrancy':'alex_caves_spellsbook:hazen_shadow'}[old_school]
        inventory.append({'id':'brutality_iron:'+name,'original_school':old_school,'school':target_school,
                          'name':chinese,'source_class':'net.goo.brutality.magic.spells.'+old_school+'.'+path.stem,
                          'ported_source':str(path.relative_to(ROOT)).replace('\\','/'), **progression[name]})

schools=[('nihility','虚无','Nihility','empty_essence','虚无','scroll_nihility'),
         ('celestia','天界','Celestia','angel_feather','天界','scroll_celestia'),
         ('inferno','炼狱','Inferno','chthonic_crystal','炼狱','scroll_inferno')]
for school,chinese,english,focus,folder,stem in schools:
    for lang,label in [(zh,chinese),(en,english)]:
        lang[f'school.brutality_iron.{school}']=label
        lang[f'attribute.brutality_iron.{school}_spell_power']=label+('法术强度' if lang is zh else ' Spell Power')
        lang[f'attribute.brutality_iron.{school}_magic_resist']=label+('魔法抗性' if lang is zh else ' Magic Resistance')
        lang[f'death.attack.brutality_iron.{school}']='%1$s '+('被'+chinese+'魔法吞没' if lang is zh else 'was consumed by '+english+' magic')
        lang[f'death.attack.brutality_iron.{school}.player']='%1$s '+('被 %2$s 的'+chinese+'魔法击败' if lang is zh else 'was slain by %2$s using '+english+' magic')
    source=ROOT.parent/'generated-art/brutality-iron-scrolls/final'/folder
    selection_file=ROOT/'scroll-selection.json'
    if selection_file.exists():
        selected=json.loads(selection_file.read_text(encoding='utf8')).get(school)
        if selected: source=(ROOT/selected['source']).resolve().parent
    texture=ASSET/f'textures/item/scroll_{school}.png'
    texture.parent.mkdir(parents=True,exist_ok=True)
    shutil.copy2(source/(stem+'.png'),texture)
    shutil.copy2(source/(stem+'.png.mcmeta'),str(texture)+'.mcmeta')
    write_json(ASSET/f'models/item/scroll_{school}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'brutality_iron:item/scroll_{school}'}})
    write_json(DATA/f'tags/items/{school}_focus.json',{'replace':False,'values':['terramity:'+focus]})
    write_json(DATA/f'damage_type/{school}_magic.json',{'exhaustion':0,'message_id':'brutality_iron.'+school,'scaling':'never'})
types=['brutality_iron:'+s[0]+'_magic' for s in schools]
write_json(RES/'data/minecraft/tags/damage_type/bypasses_armor.json',{'replace':False,'values':types})
write_json(RES/'data/forge/tags/damage_type/is_magic.json',{'replace':False,'values':types})
write_json(ASSET/'lang/zh_cn.json',zh)
write_json(ASSET/'lang/en_us.json',en)
write_json(ROOT/'spell-inventory.json',inventory)
print(f'Prepared {len(inventory)} spells, three animated school scrolls and three focus tags')
