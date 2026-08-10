# 생성 아트 기록

게임용 아트는 반실사 3D 렌더가 아닌 측면 시점의 2D 다크 판타지 화풍으로 생성합니다. v4 데모부터 주인공은 또렷한 선화와 평면 셀 채색을 사용하는 현대적인 초자연 액션 애니메이션 방향으로 전환했습니다.

## 투명 스프라이트

- `raw/hero-atlas-chroma.png` → `processed/hero-atlas-alpha.png`: 4×4 주인공 이동·점프·대시·연속 공격·혈기 기술 포즈
- `raw/enemy-atlas-chroma.png` → `processed/enemy-atlas-alpha.png`: 4×3 혈귀·사냥꾼·망령 포즈
- `raw/boss-atlas-chroma.png` → `processed/boss-atlas-alpha.png`: 4×3 태양의 심판관 기본·공격·2페이즈 포즈
- `raw/world-objects-chroma.png` → `processed/world-objects-alpha.png`: 4×3 예배당·성소·나무·폐허·제단·성문·기둥·상자·울타리·석상·플랫폼·바리케이드
- `generated/hero-side-atlas-v4-chroma.png`: v4에서 사용한 카엘 아르덴의 4×4 자동 달리기·대시·3연격·혈술 원본
- `generated/hero-side-atlas-v5-chroma.png` → `app/src/main/res/drawable-nodpi/hero_side_atlas_v5.png`: 동일한 발 기준선과 연속 자세를 강화한 4×4 모션 재구축 포즈
- `app/src/main/res/drawable-nodpi/boss_sun_inquisitor_atlas_v3.png`: 거대한 성당 갑주와 대검·다중 태양륜을 지닌 태양의 심판관 12자세
- `app/src/main/res/drawable-nodpi/boss_ash_warden_atlas_v3.png`: 요새형 체격과 영혼 화로·사슬 대낫을 지닌 잿빛 수문장 12자세
- `app/src/main/res/drawable-nodpi/boss_eclipse_sovereign_atlas_v3.png`: 검은 일식륜과 부유 혈검·왕실 망토를 지닌 월식의 여왕 12자세

스프라이트는 순수 녹색(`#00FF00`) 배경으로 생성한 뒤, 이미지 생성 스킬의 크로마키 도구로 소프트 매트·디스필을 적용했습니다.

### v4.17 보스 승격 프롬프트

세 보스는 작은 모바일 화면에서도 서로 다른 체형이 먼저 읽히도록 `중장 집행자 / 요새형 사신 / 월식 군주`로 역할을 분리했습니다. 모든 시트는 4×3 배열 안에서 기본·이동·근접기·원거리기·2페이즈·사망 자세를 공유하며, 세부 장식보다 큰 갑주·무기·후광 덩어리를 우선했습니다. 전체 최종 프롬프트와 후처리 수치는 [`IMAGEGEN_PROMPTS-v4.md`](IMAGEGEN_PROMPTS-v4.md)에 기록했습니다.

## 배경과 아이콘

- `raw/bg-nocturne.png`: 녹턴 성역
- `raw/bg-ashwood.png`: 재의 숲
- `raw/bg-crimson-keep.png`: 진홍 성채
- `raw/bg-duel-arena-portrait.png`: 9:16 세로형 혈월 결투장. 장애물 없는 단일 전투 평면과 중앙 실루엣 가독성을 기준으로 생성
- `raw/app-icon.png`: 정사각 고해상도 원본
- `processed/app-icon-round.png`: Android 런처용 투명 원형 마스크

원본은 보존하며 앱에는 `app/src/main/res/drawable-nodpi`의 처리본만 사용합니다.

### v4 카엘 스프라이트 프롬프트 요약

`style-transfer` 용도의 4×4 Android 전투 스프라이트. 기존 시트의 동작 순서와 셀 배치를 유지하되, 은빛 머리와 청백색 눈, 남색 전투 코트, 진홍 혈검을 지닌 독자적 뱀파이어 카엘로 통일한다. 모든 프레임의 신체 비율·의상·무기·발 기준선을 고정하고 달리기는 접지/하강/교차/상승의 4단계로 구성한다. 또렷한 각진 선화, 평면 셀 채색, 2단계 그림자, 절제된 청백 림라이트와 진홍 혈술. 기존 작품의 캐릭터·복장·상징·대표 포즈를 복제하지 않는다. 녹색 크로마키 배경, 텍스트·워터마크·배경·그림자 제외.

### v5 카엘 모션 프롬프트 요약

`stylized-concept` 용도의 4×4 Android 전투 스프라이트. v4 캐릭터를 외형 참조로 사용해 백발의 성인 남성 뱀파이어, 남흑색 장식 코트와 진홍 안감, 혈검, 셀 채색을 모든 셀에서 유지한다. 1행은 좌우 발 접지·하강·교차·상승의 달리기 순환, 2행은 호흡·대시 준비·이동·제동, 3행은 준비·1타·역방향 2타·강한 마무리, 4행은 혈창·흡혈·피격·무릎 꿇기다. 정확한 측면 시점, 동일 크기, 동일 발 기준선, 셀 간 겹침과 분리된 이펙트 없음. 균일한 `#00FF00` 크로마 배경, 텍스트·워터마크·3D·그림자 제외. 처리본에는 소프트 매트·디스필·1픽셀 가장자리 수축을 적용한다.

### 세로형 결투장 생성 프롬프트 요약

`stylized-concept` 용도의 모바일 세로형 2D 결투 배경. 거대한 진홍빛 달 아래 고딕 성당 안뜰, 측면 시점, 하단 1/3에 평평하고 연속된 전투 바닥, 중앙에는 두 캐릭터가 선명하게 보일 빈 공간. 차가운 남색 그림자와 절제된 진홍 림라이트의 수작업풍 2D 다크 판타지. 캐릭터·장애물·플랫폼·계단·UI·문자·로고·워터마크 제외.
