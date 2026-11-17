from pathlib import Path
import sys
sys.path.insert(0,r'C:\Users\WANYIWIGE\.codex\skills\pixel-art-studio\scripts')
from pixelstudio import Sprite
p=Path('pixel-art/celestial-guard');p.mkdir(parents=True,exist_ok=True)
s=Sprite(16,16)
s.rect(0,0,15,15,'#24304a');s.rect(1,1,14,14,'#344462');s.rect(2,2,13,13,'#425876')
s.polygon([(1,9),(7,2),(14,6),(8,14)],'#6c4b20');s.polygon([(2,9),(7,3),(13,6),(8,13)],'#e7ac35');s.polygon([(4,9),(7,5),(11,6),(8,11)],'#435777')
s.line(2,8,7,3,'#fff2ae');s.line(7,3,13,6,'#f9dc79');s.line(13,6,8,13,'#b77b22');s.line(8,13,2,9,'#bb8225')
s.line(3,5,10,3,'#f6d667');s.line(10,3,13,9,'#fff0a4');s.line(13,9,6,12,'#d59c31');s.line(6,12,3,5,'#a16f27')
s.line(5,5,10,7,'#fff4b5');s.line(10,7,9,11,'#e4a62f');s.line(9,11,5,9,'#f7d262');s.line(5,9,5,5,'#d79b2d')
s.px(7,7,'#ffffff');s.px(8,8,'#fff6cb');s.px(1,3,'#ffffff');s.px(14,11,'#ffeab4')
s.save_png('src/main/resources/assets/brutality_iron/textures/gui/spell_icons/celestial_guard.png');s.preview(str(p/'preview.png'),scale=16)
