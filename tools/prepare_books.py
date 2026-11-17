"""Package the six selected native animations and view-specific book models."""
from pathlib import Path
import json,shutil,hashlib,zipfile
root=Path(__file__).resolve().parents[1];art=root.parent/'generated-art/brutality-iron-tomes'
res=root/'src/main/resources';assets=res/'assets/brutality_iron'
books=[
 ('daemonic_tome','恶魔魔典','Daemonic Tome','final/恶魔魔典','bielgg_spells:shadow'),
 ('darkist_tome','黑暗之书','Darkist Tome','final/幽匿学派魔法书','echo_spellbooks:echo'),
 ('cosmic_tome','宇宙魔典','Cosmic Tome','cosmic-v2-animated-stars','alex_caves_spellsbook:hazen_cosmic'),
 ('void_tome','虚空魔典','Void Tome','final/虚空魔典','brutality_iron:nihility'),
 ('brimwielder_tome','持剑者宝典','Brimwielder Tome','brimwielder-v2-animated-fire','brutality_iron:inferno'),
 ('celestia_tome','塞拉斯蒂娅之书','Celestia Tome','celestia-v8-halo-only','brutality_iron:celestia')]
def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True)
 path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
manifest=[]
with zipfile.ZipFile(root/'libs/brutality-0.8.5.jar') as original:
 for name,zh,en,folder,school in books:
  selected=art/folder/(name+'_inventory.png')
  target=assets/'textures/item'/(name+'_inventory.png');target.parent.mkdir(parents=True,exist_ok=True)
  for suffix in ['', '.mcmeta']:shutil.copy2(str(selected)+suffix,str(target)+suffix)
  model={'parent':'minecraft:item/handheld','loader':'forge:separate_transforms',
   'base':{'parent':'brutality:item/tome/'+name+'_handheld'},
   'perspectives':{'gui':{'parent':'minecraft:item/generated','textures':{'layer0':'brutality_iron:item/'+name+'_inventory'}}}}
  write(assets/'models/item'/(name+'.json'),model)
  for resource in ['geo/item/tome/'+name+'_handheld.geo.json',
                   'textures/item/tome/'+name+'/'+name+'_handheld.png',
                   'animations/item/tome/'+name+'_handheld.animation.json']:
   assert 'assets/brutality/'+resource in original.namelist(),resource
  manifest.append({'id':'brutality_iron:'+name,'school':school,'selected_source':str(selected),
                   'texture_sha256':hashlib.sha256(selected.read_bytes()).hexdigest(),
                   'original_model':'brutality:item/tome/'+name+'_handheld'})
for locale in ['zh_cn','en_us']:
 path=assets/'lang'/(locale+'.json');lang=json.loads(path.read_text(encoding='utf8'))
 for name,zh,en,folder,school in books:lang['item.brutality_iron.'+name]=zh if locale=='zh_cn' else en
 lang['tooltip.brutality_iron.school_book']='学派专精：%s' if locale=='zh_cn' else 'School specialization: %s'
 write(path,lang)
values=[{'id':'brutality_iron:'+b[0],'required':False} for b in books]
write(res/'data/curios/tags/items/spellbook.json',{'replace':False,'values':values})
write(root/'book-assets.json',manifest)
print('Packaged six approved animated book textures, GUI/handheld models, translations and Curios tag')
