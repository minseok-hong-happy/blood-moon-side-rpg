# VAYLORN 5.2.2 Monster Swarm QA

## 자동 회귀 검사

- 최대 동시 몬스터 24마리와 일반 웨이브 초기 16마리 상수 검사
- 신규 몬스터가 x=650 이하의 가시 전투 진입선에서 생성되는지 검사
- 8개 고정 프레임 안에 빈 전투 슬롯이 24마리까지 보충되는지 검사
- 주인공 기본 x=180이 720px 화면 첫 4등분 기준과 일치하는지 검사
- 8개 스킬 카드, 독립 쿨타임 레이어, 읽기 쉬운 모바일 폰트 검사
- 텍스처·중력·회전값을 가진 혈정 파티클과 BGM 감시 재생 검사
- 900프레임 자동전투에서 자동 혈술 반복 발동과 `AUTO · 준비` 판정 검사

## 전체 릴리스 게이트

- Godot 4.7.1 규칙·스토리·아틀라스 테스트
- 900프레임 자동전투 및 HUD·VFX·오디오 통합 시뮬레이션
- Android JVM 테스트와 릴리스 Lint
- APK 콘텐츠, ARM ABI, 업데이트 서명·인증서 검사
- ARM 배포 APK와 x86_64 QA APK의 Godot 에셋 동일성 검사
- API 24 x86_64/OpenGL ES 3의 360×780 화면에서 `GAME_READY` 이후 15초 생존

## 실제 결과

- Godot 통합 테스트: 900프레임 자동전투, 자동 혈술 60회 발동
- 릴리스 APK Gradle 빌드·Lint: 성공
- ARM APK와 x86_64 런타임 스모크 APK의 Godot 에셋 비교: 통과
- API 24 x86_64 에뮬레이터 비스트리밍 설치: `Success`
- `GAME_READY` 이후 15초 자동전투 생존, 치명 오류 없음
- 360×780 첫 프레임 캡처: [QA-v5.2.2-monster-swarm-first-frame.png](QA-v5.2.2-monster-swarm-first-frame.png)

- APK SHA-256: `FFA37AEF3B6B2FFB2D7A61C873334D73CB30A8BAEBC33DA93F57861ABE789F9F`
- 서명 인증서 SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
