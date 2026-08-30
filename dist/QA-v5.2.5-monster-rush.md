# VAYLORN 5.2.5 Monster Rush QA

## 자동 회귀 검사

- 일반 웨이브 총량 64~120마리와 보스 웨이브 총량 36~52마리 검사
- 최대 동시 몬스터 48마리, 일반 웨이브 초기 32마리, 보스 웨이브 초기 24마리 검사
- 일반 몬스터 270~322px/s 돌진 속도와 종류별 속도 차이 검사
- 18px 전투 간격·5px 스폰 간격과 첫 4등분 주인공 위치(x=180) 검사
- 8개 스킬 카드, 쿨타임 레이어, 모바일 폰트와 BGM 감시 재생 검사
- 900프레임 자동전투에서 자동 혈술 반복 발동과 `AUTO · 준비` 판정 검사

## 전체 릴리스 게이트

- Godot 4.7.1 규칙·아틀라스·스토리 테스트
- 900프레임 자동전투 및 HUD·VFX·오디오 통합 시뮬레이션
- Android JVM 테스트와 릴리스 Lint
- APK 콘텐츠, ARM ABI, 업데이트 서명·인증서 검사
- ARM 배포 APK와 x86_64 QA APK의 Godot 에셋 동일성 검사
- API 24 x86_64/OpenGL ES 3의 360×780 화면에서 `GAME_READY` 이후 15초 생존

## 실제 결과

- Godot 단위 TC: 규칙·아틀라스·스토리 및 2배 웨이브 수치 통과
- Godot 통합 TC: 900프레임 자동전투, 자동 혈술 42회 발동
- 몬스터 48슬롯·초기 32마리·270~322px/s 돌진 속도 통합 검사: 통과
- 릴리스 APK Gradle 빌드·Lint: 성공
- ARM APK와 x86_64 런타임 스모크 APK의 Godot 에셋 비교: 통과
- API 24 x86_64 에뮬레이터 비스트리밍 설치: `Success`
- `GAME_READY` 이후 15초 자동전투 생존, 치명 오류 없음
- 360×780 첫 프레임 캡처: [QA-v5.2.5-monster-rush-first-frame.png](QA-v5.2.5-monster-rush-first-frame.png)

- APK SHA-256: `275FF5D510D99F451B6383E658E22BE589D06BC4D97CE37A2936369F7B52ECF3`
- 서명 인증서 SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
