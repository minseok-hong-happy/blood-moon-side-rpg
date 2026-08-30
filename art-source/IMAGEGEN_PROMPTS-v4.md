# v4.17 보스 아트 프롬프트

세 보스는 ImageGen 내장 모드의 `stylized-concept` 작업으로 생성했다. 기존 보스 시트는 색상·무기 연속성만 참고하고, 체형과 실루엣은 각 보스의 역할에 맞게 새로 설계했다. 결과물은 정확한 4열×3행, 셀마다 동일한 발 기준선, 왼쪽을 향한 측면 시점, 균일한 `#00FF00` 크로마 배경을 공통 규칙으로 사용한다.

## 태양의 심판관

참조: `app/src/main/res/drawable-nodpi/boss_side_atlas.png`

```text
Create a production-ready 2D mobile side-scrolling RPG boss sprite sheet in a stylized-concept direction. Use the referenced image only for continuity of the white, gold, and black palette, the solar halo, the execution sword, and the left-facing side view. Do not preserve the old slim body.

Exact layout: 4 columns by 3 rows, landscape 4:3 canvas, twelve evenly sized cells. Design a massive Sun Judgment Executioner with broad cathedral armor, obsidian plates, a white ceremonial tabard, concentric broken solar halos, and a colossal execution sword. Give him a triangular, unmistakable silhouette with very large shoulders and grounded weight.

Row 1: heavy idle, first walk contact, passing step, second walk contact. Row 2: sword anticipation, overhead impact, low recovery, guarded stance. Row 3: solar charge, judgment beam, full solar nova, defeated kneel. Premium hand-painted dark fantasy, graphic silhouette, anime-inspired cel shading, fewer large readable shapes, bright gold-white focal accents. Strict side view facing left. Identical anatomy, armor, weapon, scale, and ground baseline in every cell. Flat uniform #00FF00 background. No text, labels, grid lines, borders, UI, scenery, cast shadow, watermark, 3D render, or extra characters.
```

최종 처리본: `app/src/main/res/drawable-nodpi/boss_sun_inquisitor_atlas_v3.png`

## 잿빛 수문장

참조: `app/src/main/res/drawable-nodpi/boss_ash_warden_atlas_v2.png`

```text
Create a production-ready 2D mobile side-scrolling RPG boss sprite sheet in a stylized-concept direction. Use the referenced image only for continuity of cyan soul fire, black iron, chains, the crescent scythe, and the left-facing side view. Rebuild the silhouette as a fortress-sized guardian.

Exact layout: 4 columns by 3 rows, landscape 4:3 canvas, twelve evenly sized cells. Design the Ash Gate Warden as a hulking hunched reaper-knight with a fortress-like body, asymmetric spiked pauldrons, a cyan furnace ribcage, a faceless horned mask, a huge chained crescent poleaxe, hanging chains, and a smoke-wing mantle. His weight and width must read clearly at small mobile scale.

Row 1: furnace-breath idle, first stomp, passing stomp, second stomp. Row 2: weapon drag anticipation, enormous cleave, grounded recovery, chained guard. Row 3: soul-fire charge, ash eruption, spectral gate nova, collapsed defeat. Premium hand-painted dark fantasy, graphic silhouette, anime-inspired cel shading, fewer large readable shapes, cyan-white focal fire. Strict side view facing left. Identical anatomy, armor, weapon, scale, and ground baseline in every cell. Flat uniform #00FF00 background. No text, labels, grid lines, borders, UI, scenery, cast shadow, watermark, 3D render, or extra characters.
```

최종 처리본: `app/src/main/res/drawable-nodpi/boss_ash_warden_atlas_v3.png`

## 월식의 여왕

참조: `app/src/main/res/drawable-nodpi/boss_eclipse_matriarch_atlas_v2.png`

```text
Create a production-ready 2D mobile side-scrolling RPG boss sprite sheet in a stylized-concept direction. Use the referenced image only for continuity of white hair, black-crimson regalia, the eclipse crown, twin ritual blades, and the left-facing side view. Strengthen her royal mass and supernatural authority.

Exact layout: 4 columns by 3 rows, landscape 4:3 canvas, twelve evenly sized cells. Design the Eclipse Sovereign as a statuesque vampire queen with a broad asymmetric black-wing mantle, cathedral-armored gown, one large readable mass of white hair, a porcelain half-mask, a huge black eclipse disk, twin ritual blades, and several floating blood blades in power poses.

Row 1: sovereign idle, first glide, passing glide, second glide. Row 2: crossed-blade anticipation, luminous X slash, sweeping recovery, ritual guard. Row 3: black-orb charge, crimson crescent wave, black-sun nova, shattered-crown defeat. Premium hand-painted dark fantasy, graphic silhouette, anime-inspired cel shading, fewer large readable shapes, crimson-white focal accents. Strict side view facing left. Identical anatomy, costume, weapons, scale, and ground baseline in every cell. Flat uniform #00FF00 background. No text, labels, grid lines, borders, UI, scenery, cast shadow, watermark, 3D render, or extra characters.
```

최종 처리본: `app/src/main/res/drawable-nodpi/boss_eclipse_sovereign_atlas_v3.png`

## 후처리 기준

- 테두리 중앙값으로 실제 키 색상을 자동 추출한다.
- 투명 임계값 12, 불투명 임계값 220의 소프트 매트를 적용한다.
- 녹색 우세 픽셀에 디스필을 적용하고 알파 매트를 1픽셀 수축한다.
- 세 처리본은 모두 RGBA PNG, `1448×1086`, 4열×3행이다.
