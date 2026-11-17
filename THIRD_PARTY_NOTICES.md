# Third party notices

This local integration adapts spell and entity implementations from Brutality
0.8.5 (net.goo.brutality) and Holy Beam entity/rendering from Terramity 0.9.8
(net.mcreator.terramity). Their authors retain rights to their respective work.
Original models, animations, sounds and most icons are loaded from those required
mods, which are not bundled in this release JAR. Adapted source files identify
their origin. No redistribution license for upstream content is granted here.

The three animated school scroll textures are the user's approved custom assets.
The six GUI spellbook animations are the user's selected custom native sprites.
Their book silhouettes and palettes were studied from installed Iron's Spellbooks
and Brutality resources. Handheld model/UV/texture/animation resources remain in
the required original Brutality JAR and are referenced, not bundled here.

Version 1.2.0 adds native recreations of reviewed weapon, accessory and entity skills from Brutality 0.8.5, Terramity 0.9.8, Lethality (installed filename 0.4.1), and Mortality (installed filename 0.0.45.6.11). Selected geometry, textures, animation clips and source-item icons are included for this local integration; their exact origins and SHA-256 are recorded in skill-visual-assets.json. These assets retain their upstream authorship. The Plague scroll is an animated derivative of ROACW 1.2.4 scroll_plague.png, selected by the user. No upstream redistribution rights are granted by this notice.

## 2026-11-14 VFX revision

New low-resolution effect textures were generated and palette/grid cleaned for this project; see pixel-art/spell-revision-v2 for prompts and native files. Crystal colors and displayed block models reference Terramity; plague art references the local RoACW materials and the official Calamity wiki visual references. Magic Circles was studied for planar placement and lifecycle; its high-resolution textures and code were not bundled. Original weapon projectile textures are cropped to their single sprite UV half with source credits retained. Brutality black hole and sun glow textures/layers are reused under the existing required Brutality dependency.

## 1.4.0 spell icons

The 53 active spell icons use the user's selected native 16x16 pixel artwork.
These icons were authored on native integer pixel grids for this local project.
The selected versions and their SHA-256 hashes are recorded in
assets/brutality_iron/spell_icon_manifest.json. These selections replace the
earlier source-item icons and rejected image-generation drafts for active spells.

## 1.4.3 school armor

Six user-selected armor sets use model, texture, and animation assets from the installed Hazennstuff 1.1.2 and the local Wans Iron Expansion Hazen recreation. Original creators retain their respective rights. Individual source paths and SHA-256 hashes are recorded in armor-asset-provenance.json. This local integration grants no redistribution rights.


Celestial guard impact sound: `holy_shield_block.ogg` from Holy Hell 1.1.0, bundled as `celestial_guard_hit.ogg`.
