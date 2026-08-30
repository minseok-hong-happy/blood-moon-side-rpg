# VAYLORN 5.3.0 Visual Overdrive QA

## 자동 회귀 검사

- 새 혈월 폭발·혈흔 참격 텍스처가 의도한 해상도와 알파 채널로 임포트되는지 검사
- full-texture VFX의 자동 축소 및 화면 안전 여백 검사
- 시네마틱 VFX가 중앙 플래시와 잔상 두 겹으로 생성되는지 검사
- 평타·스킬·보스 등장/페이즈/처치의 기존 피해·자동전투 흐름 유지 검사
- 영웅 z-index, cast 프레임, 스킬 카드·장비 아이콘·폰트·BGM 회귀 검사

## 전체 릴리스 게이트

- Godot 4.7.1 규칙·아틀라스·스토리 단위 TC
- 900프레임 자동전투 및 HUD·VFX·오디오 통합 시뮬레이션
- Android JVM 테스트와 릴리스 Lint
- APK 콘텐츠, ARM ABI, 업데이트 서명·인증서 검사
- ARM 배포 APK와 x86_64 QA APK의 Godot 에셋 동일성 검사
- API 24 x86_64/OpenGL ES 3의 360×780 화면에서 `GAME_READY` 이후 15초 생존

## 실제 결과

- Godot 단위 TC: 통과
- Godot 통합 TC: 900프레임 자동전투 및 새 full-texture VFX 안전영역 통과
- 릴리스 APK Gradle 빌드·Lint: 성공
- ARM APK와 x86_64 런타임 스모크 APK의 Godot 에셋 비교: 통과
- API 24 x86_64 에뮬레이터에서 동일 패키지 설치 후 런타임 스모크: `ANDROID_RUNTIME_SMOKE_PASS`
- `GAME_READY` 이후 15초 자동전투 생존, 치명 오류 없음
- 360×780 첫 프레임 캡처: [QA-v5.3.0-visual-overdrive-first-frame.png](QA-v5.3.0-visual-overdrive-first-frame.png)

- APK SHA-256: `3D5FDFC1E48BCB4E25E4A6A70C2A182789DA7B5E03EC25AE67A655CA819F2C61`
- 서명 인증서 SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
