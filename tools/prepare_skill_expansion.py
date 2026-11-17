"""Materialize the reviewed 38 + 22 skills, retaining source provenance."""
from pathlib import Path
import json, zipfile, re, hashlib
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
ASSET=RES/'assets/brutality_iron'
def write(p,v):
    p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
old=json.loads((ROOT/'candidate-school-assignments.json').read_text(encoding='utf8'))
extra=json.loads((ROOT/'lethality-mortality-spell-candidates.json').read_text(encoding='utf8'))
school_ids={'虚无':'brutality_iron:nihility','天界':'brutality_iron:celestia','炼狱':'brutality_iron:inferno','流风':'wind_spellbooks:wind','岩土':'gtbcs_geomancy_plus:geo','疫病（新建建议）':'brutality_iron:plague','猩红（本体）':'irons_spellbooks:blood'}
# name, registry name, level cap, mana, cooldown seconds, damage, duration ticks,
# cast time ticks, representative original icon, optional visual alias.
specs=[
('引力黑洞','event_horizon',8,60,22,3,160,30,'brutality:event_horizon','black_hole'),
('残酷太阳','cruel_sun',10,80,30,2,200,40,'brutality:rhitta','cruel_sun'),
('圣枪贯光','sacred_lance',5,28,7,10,15,15,'brutality:rhongomyniad','rhongomyniad_ray'),
('暗影背袭','backstep',3,24,9,0,1,0,'brutality:shadowstep',''),
('冰魂震波','soulfrost_wave',8,45,14,6,30,20,'brutality:frostmourne',''),
('流浪者军团','stray_legion',10,80,35,4,400,40,'brutality:frostmourne',''),
('猩红血斩','sanguine_slash',5,20,5,7,60,10,'brutality:crimson_scissor_blade','bloodslash'),
('影焰斩','shadowflame_slash',5,24,6,7,60,10,'brutality:shadowflame_scissor_blade','shadowflame_slash'),
('追踪剑光','seeking_blades',8,40,12,5,70,20,'brutality:exoblade','exoblade_beam'),
('星印引爆','stellar_marks',8,36,5,5,180,15,'brutality:seventh_star','star'),
('念动力操纵','telekinetic_grasp',5,42,12,4,100,15,'brutality:crease_of_creation',''),
('次元乱斩','infernal_crosscut',10,65,20,15,25,35,'brutality:hf_murasama',''),
('蓄能爆裂','charged_detonation',10,100,40,22,40,80,'brutality:first_explosion',''),
('火箭拳','gale_fist',5,32,10,10,18,30,'brutality:doomfist_gauntlet',''),
('深渊追踪弹','abyssal_volley',8,38,10,5,80,20,'brutality:challenger_deep','abyss_projectile'),
('末影换位','void_exchange',5,30,8,0,100,15,'terramity:enderswap_sceptre',''),
('穿顶升行','ceiling_ascent',3,24,12,0,1,20,'terramity:tome_of_ascension',''),
('惯性逆转','inertia_reversal',3,18,5,0,10,0,'terramity:velocity_flip',''),
('烈风退跃','gale_recoil',3,16,5,0,1,0,'terramity:galebounce_tome',''),
('守护者之手','guardian_hand',8,65,25,5,160,30,'terramity:guardian_grimoire','tg_hand'),
('水晶疗愈阵','crystal_sanctuary',5,40,20,0,160,25,'terramity:crystal_heart',''),
('盖亚催生阵','gaia_bloom',5,30,18,0,160,25,'terramity:gaias_grace',''),
('替身遁形','vanishing_decoy',5,32,15,0,100,0,'terramity:shinobis_substitute','decoy'),
('追猎印记','pursuit_anchor',5,25,6,0,600,10,'terramity:chaos_heart_ring',''),
('燃魂加速','burning_zeal',5,28,25,0,200,15,'terramity:burning_spirit_ring',''),
('归途旋风','homeward_gale',8,70,40,0,160,60,'terramity:warp_whistle',''),
('三重圣裁','triple_judgment',8,55,20,9,60,30,'terramity:angel_feather','holy_beam'),
('冥河游魂','stygian_souls',8,40,12,5,100,20,'terramity:chthonic_crystal','stygian_soul'),
('魔仆召集','arcane_retinue',8,60,30,4,320,35,'terramity:accursed_soul',''),
('冰块投射','ice_cube_barrage',5,25,7,6,60,15,'terramity:iceball','ice_cube'),
('震慑咆哮','resonant_roar',5,32,14,0,20,20,'terramity:energized_core',''),
('石像卫队','stone_retinue',10,75,35,6,360,40,'terramity:energized_core',''),
('聚能灵弹','spirit_bomb',10,90,35,18,100,70,'terramity:guided_energy_blast','spirit_bomb'),
('震地威压','seismic_pressure',5,35,15,0,35,20,'terramity:giant_sniffers_hoof',''),
('岩躯震击','stone_body_slam',8,45,18,12,30,30,'terramity:duskrok',''),
('炼狱重击','hellrok_slam',8,50,18,13,30,30,'terramity:hellrok_gauntlet',''),
('疾风乱斩','gale_blitz',8,55,20,7,50,20,'terramity:galebounce_tome',''),
('三重邪光','triple_profane_beam',8,55,20,9,60,30,'terramity:accursed_soul','holy_beam'),
('腐蚀刃波','caustic_wave',8,30,8,7,70,15,'lethality:defiled_greatsword','acidic_slash'),
('追魂魔镰','soulreaping_scythes',8,35,10,7,80,20,'lethality:exalted_oathblade','forbidden_scythe'),
('恶魔兵装','devil_armament',10,55,18,7,80,30,'lethality:devils_devastation','devils_scythe'),
('灰烬追魂','ashen_skulls',8,40,12,6,100,20,'lethality:gaels_greatsword','gael_skull'),
('地狱爆弹','hellish_bolt',5,28,8,10,70,15,'lethality:vehemence','vehemence_bolt'),
('硫火献祭','brimstone_pact',8,50,30,0,160,20,'lethality:grievance',''),
('星芒连刺','starlight_flurry',5,30,9,4,24,15,'lethality:starlight','starlight_stab'),
('铁意回生','ironwill_recovery',8,45,4,0,180,15,'lethality:nightfall',''),
('拘魂钩索','binding_hook',5,28,8,6,60,15,'lethality:foreign_hook','hook_model'),
('静止庇护','stillness_ward',8,45,25,0,180,20,'lethality:pale_branch',''),
('震动感知','tremor_sense',5,25,18,0,160,15,'lethality:warden_spine',''),
('刃域加速','blade_current',8,45,25,0,160,20,'lethality:hf_meowrasama',''),
('疫病注入','plague_infusion',5,26,7,5,120,15,'mortality:plague_injector',''),
('疫雾爆发','pestilent_mist',8,45,18,2,140,25,'mortality:plague_reaper_scythe',''),
('腐甲连爪','corrosive_claws',8,40,12,6,30,20,'mortality:abusive_claw',''),
('裂核重击','core_shatter',8,42,15,10,20,25,'mortality:spear_of_the_champion',''),
('火灵集群','infernal_spirits',8,55,25,5,280,30,'mortality:core_of_the_inferno',''),
('晨曦三叠','dawn_triad',5,30,12,5,160,15,'mortality:dawnmaker_greatsword',''),
('蚀月蓄击','eclipse_charge',8,40,4,8,180,15,'mortality:dawn_unmade_greatsword',''),
('陨星蓄放','meteor_reservoir',10,65,4,8,240,25,'mortality:longsword_of_the_new_world','meteor'),
('生命契约','vital_covenant',5,35,25,0,180,20,'mortality:racher_glaive',''),
('梦境唤回','dream_recall',3,100,90,0,30,60,'mortality:iridium_dreamtwister',''),
]
byname={r['name']:r for r in old+extra}
guides={
'引力黑洞':'召出跟随自身的黑洞，牵引并持续伤害附近敌人。',
'残酷太阳':'头顶升起微型太阳，持续点燃周围敌人。',
'圣枪贯光':'向前释放贯穿沿线敌人的圣枪光束。',
'暗影背袭':'锁定视线内的敌人，瞬移到其背后。',
'冰魂震波':'释放扩散的冰霜波，冻结、减速敌人，命中时恢复自身生命。',
'流浪者军团':'召唤冰霜弓手协助作战，同时最多存在十名。',
'猩红血斩':'发射穿透敌人的血色斩击，命中时恢复自身生命。',
'影焰斩':'发射穿透斩击，点燃敌人并附加影焰。',
'追踪剑光':'释放四道自动寻找敌人的剑光。',
'星印引爆':'向周围射出附着星体，再次施放引爆自己留下的星印。',
'念动力操纵':'将目标悬持于视线前方，移动视线操纵目标，结束时将其投出。',
'次元乱斩':'交错的剑光切割周围敌人，造成范围伤害。',
'蓄能爆裂':'长时间蓄能后，在瞄准位置释放大型爆裂。',
'火箭拳':'蓄力向前突进，伤害并击退撞到的敌人。',
'深渊追踪弹':'向周围发射寻找敌人的深渊弹幕。',
'末影换位':'与锁定的敌人交换位置，片刻后安全换回。',
'穿顶升行':'穿过头顶的障碍，抵达上方可容身的位置。',
'惯性逆转':'反转并放大自身的运动速度，迅速改变方向。',
'烈风退跃':'用气流推动自身向视线反方向跃出。',
'守护者之手':'召出旋转的石质守护之手，以射线扫击敌人。',
'水晶疗愈阵':'在脚下布置法阵，持续为自身、队友与所属召唤物提供再生。',
'盖亚催生阵':'布置催生法阵，逐步促进附近植物生长。',
'替身遁形':'留下吸引追击者的诱饵，自身短暂隐形。',
'追猎印记':'标记视线中的敌人，再次施放可瞬移到其背后。',
'燃魂加速':'点燃自身，换取一段时间的急迫强化。',
'归途旋风':'返回重生点，并在原地留下短暂的归途旋风，自己与队友可通过旋风返回各自重生点。',
'三重圣裁':'在瞄准位置布下三处预警，依次降下圣光打击。',
'冥河游魂':'放出追踪敌人的冥河之魂，命中削弱目标。',
'魔仆召集':'召出魔法随从，协助远程攻击敌人。',
'冰块投射':'投出冰块弹幕，命中使敌人冻结并减速。',
'震慑咆哮':'以石质共振震慑周围敌人，使其迟缓并短暂离地。',
'石像卫队':'召出受控的石像卫队，追击附近敌人。',
'聚能灵弹':'在上方聚集大型能量球，蓄能完成后向目标投出，命中造成范围伤害。',
'震地威压':'震动地面，迟缓并震起周围敌人。',
'岩躯震击':'重击周围地面，伤害并震起敌人。',
'炼狱重击':'以炼狱之力重击周围敌人，使其离地并燃烧。',
'疾风乱斩':'连续向前突进，以风刃切割附近敌人。',
'三重邪光':'在瞄准位置布下三处预警，依次爆发邪光打击。',
'腐蚀刃波':'发射穿透斩击，使敌人感染疫病并燃烧。',
'追魂魔镰':'释放追踪、穿透敌人的影焰镰刃。',
'恶魔兵装':'同时释放三道镰刃与两支叉矛，穿透并灼烧敌人。',
'灰烬追魂':'释放三枚寻敌骷髅，命中引发范围爆裂。',
'地狱爆弹':'射出地狱能量弹，碰撞后爆裂。',
'硫火献祭':'持续承受少量伤害，换取伤害强化与吸血；生命过低时自动解除。',
'星芒连刺':'连续放出迅疾星光刺击，穿透沿线敌人。',
'铁意回生':'获得吸收护盾，再次施放将剩余护盾的一部分转为治疗。',
'拘魂钩索':'投出钩索，命中后将敌人拖向自身。',
'静止庇护':'静止一段时间后获得强力减伤，移动会解除减伤。',
'震动感知':'感知附近正在移动的敌人，以发光标记显露其位置。',
'刃域加速':'提升移动与攻击速度，同时减速周围敌人。',
'疫病注入':'感染瞄准的敌人。疫病定期发作，并向附近敌人传播。',
'疫雾爆发':'在瞄准位置释放疫雾，持续感染并伤害敌人。',
'腐甲连爪':'连续三次爪击前方敌人，施加疫病并削弱护甲。',
'裂核重击':'重击附近敌人，使其离地，削弱护甲与最大生命。',
'火灵集群':'召出火焰灵体，协助远程攻击敌人。',
'晨曦三叠':'短暂强化自身，此后每三次命中触发一次晨曦打击。',
'蚀月蓄击':'短暂积蓄命中层数，再次施放消耗层数重击瞄准的敌人。',
'陨星蓄放':'短暂积蓄命中层数，再次施放按层数向瞄准位置投下陨星。',
'生命契约':'暂时牺牲部分最大生命，换取攻击力强化。',
'梦境唤回':'唤回附近近期死亡后处于旁观状态的玩家，恢复部分生命并给予短暂防护；极限模式不可用。',
}
assert len(specs)==len(byname)==60
jars={}
for mod,pattern in [('brutality','brutality-0.8.5-*.jar'),('terramity','terramity-0.9.8-*.jar'),('lethality','lethality*.jar'),('mortality','mortality*.jar')]:
    jars[mod]=zipfile.ZipFile(next((ROOT.parent/'mods').glob(pattern)))
definitions=[]
zh=json.loads((ASSET/'lang/zh_cn.json').read_text(encoding='utf8'))
en=json.loads((ASSET/'lang/en_us.json').read_text(encoding='utf8'))
for name,id,cap,mana,cool,damage,duration,cast,icon,visual in specs:
    original=byname[name]
    school=original.get('school_id') or school_ids[original['school']]
    description=guides[name]
    entry=dict(name=name,id=id,school=school,max_level=cap,min_rarity='COMMON' if cap>=8 else 'UNCOMMON',mana=mana,cooldown=cool,damage=damage,duration=duration,cast_time=cast,visual=visual,source_mod=original['source_mod'],description=description,icon_source=icon)
    definitions.append(entry)
    zh['spell.brutality_iron.'+id]=name
    zh['spell.brutality_iron.'+id+'.guide']=description
    en['spell.brutality_iron.'+id]=id.replace('_',' ').title()
    en['spell.brutality_iron.'+id+'.guide']=id.replace('_',' ').capitalize()+'.'
    mod,item=icon.split(':');jar=jars[mod];names=jar.namelist()
    choices=[n for n in names if n.startswith('assets/'+mod+'/textures/item') and n.endswith('.png') and (Path(n).stem==item or Path(n).stem==item+'_inventory')]
    if not choices:
        # Boss-derived skills use their existing ingredient or item reference.
        replacement={'iceball':'ice_cube','duskrok':'duskrok_claw','gaias_grace':'crystal_heart','giant_sniffers_hoof':'sniffer_fur'}.get(item,item)
        choices=[n for n in names if '/textures/item' in n and n.endswith('.png') and replacement in Path(n).stem and 'handheld' not in n]
    if not choices:
        choices=[n for n in names if n.startswith('assets/'+mod+'/textures/item') and n.endswith('.png') and any(t in Path(n).stem for t in ['core','crystal','soul'])]
    assert choices,(id,icon)
    src=sorted(choices,key=lambda n:('inventory' not in n,len(n)))[0]
    dest=ASSET/'textures/gui/spell_icons'/(id+'.png');dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(jar.read(src))
    if src+'.mcmeta' in names:Path(str(dest)+'.mcmeta').write_bytes(jar.read(src+'.mcmeta'))
    entry['icon_entry']=src
write(ASSET/'skill_expansion.json',definitions)
write(ROOT/'skill-expansion.json',definitions)
for lang,label in [(zh,'疫病'),(en,'Plague')]:
    lang['school.brutality_iron.plague']=label
    lang['attribute.brutality_iron.plague_spell_power']=label+('法术强度' if lang is zh else ' Spell Power')
    lang['attribute.brutality_iron.plague_magic_resist']=label+('魔法抗性' if lang is zh else ' Magic Resistance')
    lang['death.attack.brutality_iron.plague']='%1$s '+('被疫病吞噬' if lang is zh else 'succumbed to plague magic')
    for effect,title in [('plague','疫病'),('corrosion','腐甲'),('shattered_core','裂核'),('vital_covenant','生命契约'),('brimstone_pact','硫火献祭'),('stillness_ward','静止庇护')]:
        lang['effect.brutality_iron.'+effect]=title if lang is zh else effect.replace('_',' ').title()
write(ASSET/'lang/zh_cn.json',zh);write(ASSET/'lang/en_us.json',en)
write(RES/'data/brutality_iron/tags/items/plague_focus.json',{'replace':False,'values':['terramity:chthonic_crystal',{'id':'mortality:plague_core','required':False}]})
write(RES/'data/brutality_iron/damage_type/plague_magic.json',{'exhaustion':0,'message_id':'brutality_iron.plague','scaling':'never'})
for tag in ['data/forge/tags/damage_type/is_magic.json','data/minecraft/tags/damage_type/bypasses_armor.json']:
    p=RES/tag;d=json.loads(p.read_text());d['values']=list(dict.fromkeys(d['values']+['brutality_iron:plague_magic']));write(p,d)
print('Prepared',len(definitions),'native skill definitions and source-item icons')

# Apply the later user revision after rebuilding the historical candidate list.
import runpy
runpy.run_path(str(ROOT/"tools/prepare_skill_revision.py"),run_name="__main__")
