# VAYLORN 5.0.1 Engine Hotfix QA

## 실제 재현

- 공개 v5.0.0 APK를 ARM Android 7.0 `armeabi-v7a` 환경에 설치: PASS
- 앱 시작 직후 프로세스 종료 재현: PASS
- 1차 원인 확인: `GodotNativeBridge.getRenderView()` `NoSuchMethodError` 및 JNI `SIGABRT`
- 2차 원인 확인: Android 런타임 설정 파일 `res://project.binary` 누락

## 수정 검증

- Godot 4.7.1 게임 규칙·스토리·아틀라스·액션 잠금 테스트
- 900프레임 자동 전투 및 UI 비율 통합 시뮬레이션
- Android JVM 테스트, Lint, 서명 인증서, APK 콘텐츠 검사
- `project.binary` ECFG 헤더 및 APK 내 단일 포함 검사
- ARM 배포 APK와 x86_64 QA APK의 모든 Godot 에셋 바이트 단위 동일성 검사
- API 24 x86_64/OpenGL ES 3 환경에 QA APK 설치 및 실제 게임 장면 `GAME_READY` 확인
- 초기 전투 구간 동안 프로세스 생존 확인
- Java `FATAL EXCEPTION`, JNI 오류, 네이티브 fatal signal 부재 확인

QA APK는 배포 APK와 코드·리소스·Godot 프로젝트가 같고 엔진 ABI만 x86_64입니다. 위 검사는
`tools/build_release.ps1`의 필수 게이트이며, 모두 통과한 ARM APK만 `dist`에 생성됩니다.

- APK SHA-256: `CDE6A28887BA59BBB63215A6FC4FC3367EAB9008D04375CA1D88FC0BC73F3D87`
