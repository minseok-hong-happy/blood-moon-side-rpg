# v5.2 스킬·장비·파티클 아트 프롬프트

이번 자산은 모두 Codex의 ImageGen 내장 모드(`stylized-concept`)로 생성했다. 아이콘은 작은 모바일 화면에서도 한눈에 구분되도록 단일 실루엣, 높은 명암, 셀별 독립 안전영역을 사용했다. 파티클은 크로마키 원본을 만든 뒤 알파 PNG로 후처리했다.

## 혈술 아이콘 8종

최종 파일: `app/src/main/assets/art/skill_icons_atlas_v2.png`

```text
Use case: stylized-concept
Asset type: production game UI icon atlas for a portrait 2D dark-fantasy mobile RPG
Input images: Image 1 is a style and palette reference only; create a new atlas, do not edit or copy its compositions.
Primary request: create exactly eight original vampire blood-magic skill icons in a strict 4-column by 2-row atlas.
Cell order, left to right:
Top row: piercing blood spear; cyan vampiric siphon vortex; violet-crimson blood nova; blue-red vein rush slash.
Bottom row: scarlet blade rain; cyan blood-chain lightning; orange-crimson blood pillar; purple eclipse halo.
Style/medium: premium hand-painted 2D anime action-RPG icon art, crisp cel-painted edges, luminous cores, layered magical filaments, controlled bloom, bold readable silhouettes.
Composition/framing: exact 2:1 landscape canvas; eight equal square cells; perfectly aligned 4×2 grid; generous consistent black gutters; each effect centered and fully contained inside the inner 72% safe area of its cell; no element may touch, cross, or bleed into a neighboring cell.
Icon plate: each cell has its own dark navy-black circular medallion with a thin colored rim and subtle inner glow; outside the medallions is pure black.
Readability: recognizable at 58×58 pixels; one dominant symbol per icon; high contrast and distinct color identity.
Constraints: no text, letters, numbers, characters, hands, scenery, logos, watermark, frames outside each cell, cropped elements, overlapping cells, duplicated symbols, or 3D photorealism.
```

## 장비 아이콘 3종

최종 파일: `app/src/main/assets/art/equipment_icons_atlas_v1.png`

```text
Use case: stylized-concept
Asset type: production equipment UI icon atlas for the same portrait 2D dark-fantasy mobile RPG
Input images: Image 1 is the exact visual-style reference for medallion shape, painted finish, contrast, rim lighting, and black backdrop. Create new equipment symbols.
Primary request: create exactly three original vampire equipment icons in a strict 3-column by 1-row atlas.
Cell order, left to right: an elegant black-steel blood longsword with crimson fuller; a black-silver gothic vampire cuirass with a crimson mantle collar; a faceted bloodstone relic held inside a delicate silver crescent setting.
Style/medium: premium hand-painted 2D anime action-RPG inventory icons, crisp cel-painted edges, polished metal and gemstone textures, controlled magical glow, strong silhouettes.
Composition/framing: exact 3:1 landscape canvas; three equal square cells; perfectly aligned; generous consistent black gutters; one item centered in each dark navy-black circular medallion; each item fully contained inside the inner 68% safe area; no element may touch or bleed into another cell.
Readability: recognizable at 64×64 pixels; dramatic highlights; weapon crimson-red, armor cold silver-blue with crimson accent, relic violet-red.
Constraints: no text, letters, numbers, characters, hands, scenery, logos, watermark, cropped item, overlapping cells, duplicate equipment, or photorealistic 3D render.
```

## 물리 파티클용 혈정 파편

최종 파일: `app/src/main/assets/art/vfx_blood_shard_particle_v1.png`

```text
Use case: stylized-concept
Asset type: single particle sprite for Godot CPUParticles2D in a premium 2D anime dark-fantasy mobile RPG
Input images: Image 1 is a visual-style reference only for crimson energy and painted edge treatment.
Primary request: one original razor-shaped blood-crystal spark shard with a bright white-hot core, deep crimson body, tiny cyan specular edge, and a compact four-point impact glint.
Scene/backdrop: perfectly flat solid #00ff00 chroma-key background for local background removal.
Style/medium: crisp hand-painted 2D VFX sprite, high contrast, clean silhouette, compact controlled glow; not photorealistic.
Composition/framing: square canvas; exactly one shard centered; diagonal lower-left to upper-right; subject occupies about 52% of canvas; generous empty padding on every side; fully contained and never cropped.
Constraints: background must be one uniform #00ff00 color with no shadows, gradients, texture, reflections, floor plane, or lighting variation; do not use green anywhere in the shard; no smoke, no soft translucent cloud, no cast shadow, no text, letters, numbers, logos, watermark, extra shards, border, or scenery.
```

후처리는 `remove_chroma_key.py`의 테두리 자동 키 추출, 투명 임계값 12, 불투명 임계값 220, 디스필을 사용했다. 결과물은 투명 RGBA PNG이며 Godot `CPUParticles2D`의 중력·회전·감속 파라미터와 함께 사용한다.
