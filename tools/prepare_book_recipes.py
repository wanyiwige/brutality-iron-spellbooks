"""Generate Terramity material recipes, conditional unlocks, and a readable guide."""
from pathlib import Path
import json,zipfile,re
root=Path(__file__).resolve().parents[1];res=root/'src/main/resources'
specs=[
 ('daemonic_tome','恶魔魔典','daemonium','accursed_soul','black_matter'),
 ('darkist_tome','黑暗之书','dimlite_ingot','warden_soul','black_matter'),
 ('cosmic_tome','宇宙魔典','cosmilite_ingot','energized_core','black_matter'),
 ('void_tome','虚空魔典','void_alloy','empty_essence','black_matter'),
 ('brimwielder_tome','持剑者宝典','daemonium','chthonic_crystal','black_matter'),
 ('celestia_tome','塞拉斯蒂娅之书','reverium','angel_feather','cosmilite_ingot')]
def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True)
 path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
jar=next((root.parent/'mods').glob('terramity*.jar'))
with zipfile.ZipFile(jar) as z:
 zh=json.loads(z.read('assets/terramity/lang/zh_cn.json'))
 names=set(z.namelist())
 for _,_,*items in specs:
  for item in items:
   assert 'assets/terramity/models/item/'+item+'.json' in names,item
manifest=[]
for name,title,primary,focus,binder in specs:
 rid='brutality_iron:'+name
 condition=[{'type':'forge:item_exists','item':rid}]
 key={c:{'item':'terramity:'+item} for c,item in [('I',primary),('F',focus),('C',binder)]}
 key['B']={'item':'minecraft:book'}
 recipe={'type':'minecraft:crafting_shaped','category':'equipment','group':'brutality_iron_school_books',
         'conditions':condition,'pattern':['IFI','CBC','IFI'],'key':key,'result':{'item':rid,'count':1}}
 write(res/'data/brutality_iron/recipes'/(name+'.json'),recipe)
 advancement={'parent':'minecraft:recipes/root','criteria':{
  'has_material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['terramity:'+focus]}]}},
  'has_the_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':rid}}},
  'requirements':[['has_material','has_the_recipe']],'rewards':{'recipes':[rid]}}
 # Forge's native ConditionalAdvancement format is an advancements array.
 write(res/'data/brutality_iron/advancements/recipes/equipment'/(name+'.json'),
       {'advancements':[{'conditions':condition,'advancement':advancement}]})
 manifest.append({'recipe':rid,'book':title,'primary':'terramity:'+primary,'focus':'terramity:'+focus,
                  'binding':'terramity:'+binder,'counts':{'primary':4,'focus':2,'binding':2,'minecraft:book':1}})
write(root/'book-recipes.json',manifest)
lines=['# 六本法术书的 Terramity 配方','',
 '在工作台中按下列布局合成，每次产出一本空白可写法术书。书芯为普通书，其余材料均来自 Terramity。','',
 '```text','I F I','C B C','I F I','```','',
 '`I` 主材（4 个），`F` 学派核心（2 个），`C` 辅材（2 个），`B` 普通书（1 本）。','',
 '| 产物 | 主材 ×4 | 学派核心 ×2 | 辅材 ×2 |','|---|---|---|---|']
for name,title,p,f,b in specs:
 clean=lambda item:re.sub('§.', '', zh['item.terramity.'+item])
 lines.append(f"| {title} | {clean(p)} | {clean(f)} | {clean(b)} |")
lines+=['','获得对应学派核心材料后解锁原生配方书提示，JEI 也可显示工作台配方。',
        '对应书因可选模组或内容设置而未注册时，配方与配方解锁进度一同跳过。',
        '原模组的无咒法典没有找到生存配方或掉落引用，本方案使用可正常获取的普通书作为书芯。',
        '宇宙书的充能核心在当前 Terramity JAR 的试炼守卫掉落表中；其他主材与核心也已核对实际物品 ID。','']
(root/'book-recipes.md').write_text('\n'.join(lines),encoding='utf8')
print('Prepared six shaped recipes and six conditional recipe unlocks')
