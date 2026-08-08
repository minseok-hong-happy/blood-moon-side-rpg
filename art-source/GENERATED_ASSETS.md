# 생성 아트 기록

모든 게임용 아트는 반실사 3D 렌더가 아닌, 측면 시점의 반사실적 수작업풍 2D 다크 고딕 화풍으로 생성했습니다.

## 투명 스프라이트

- `raw/hero-atlas-chroma.png` → `processed/hero-atlas-alpha.png`: 4×4 주인공 이동·점프·대시·연속 공격·혈기 기술 포즈
- `raw/enemy-atlas-chroma.png` → `processed/enemy-atlas-alpha.png`: 4×3 혈귀·사냥꾼·망령 포즈
- `raw/boss-atlas-chroma.png` → `processed/boss-atlas-alpha.png`: 4×3 태양의 심판관 기본·공격·2페이즈 포즈
- `raw/world-objects-chroma.png` → `processed/world-objects-alpha.png`: 4×3 예배당·성소·나무·폐허·제단·성문·기둥·상자·울타리·석상·플랫폼·바리케이드

스프라이트는 순수 녹색(`#00FF00`) 배경으로 생성한 뒤, 이미지 생성 스킬의 크로마키 도구로 소프트 매트·디스필을 적용했습니다.

## 배경과 아이콘

- `raw/bg-nocturne.png`: 녹턴 성역
- `raw/bg-ashwood.png`: 재의 숲
- `raw/bg-crimson-keep.png`: 진홍 성채
- `raw/bg-duel-arena-portrait.png`: 9:16 세로형 혈월 결투장. 장애물 없는 단일 전투 평면과 중앙 실루엣 가독성을 기준으로 생성
- `raw/app-icon.png`: 정사각 고해상도 원본
- `processed/app-icon-round.png`: Android 런처용 투명 원형 마스크

원본은 보존하며 앱에는 `app/src/main/res/drawable-nodpi`의 처리본만 사용합니다.

### 세로형 결투장 생성 프롬프트 요약

`stylized-concept` 용도의 모바일 세로형 2D 결투 배경. 거대한 진홍빛 달 아래 고딕 성당 안뜰, 측면 시점, 하단 1/3에 평평하고 연속된 전투 바닥, 중앙에는 두 캐릭터가 선명하게 보일 빈 공간. 차가운 남색 그림자와 절제된 진홍 림라이트의 수작업풍 2D 다크 판타지. 캐릭터·장애물·플랫폼·계단·UI·문자·로고·워터마크 제외.
