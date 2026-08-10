# VAYLORN 5.0.1 Engine Hotfix

Godot 엔진 프리뷰 APK가 실행 즉시 종료되던 문제를 수정한 필수 안정화 업데이트입니다.

## 원인과 수정

- v5.0.0의 R8 축소 과정에서 Godot JNI 브리지의 `getRenderView()`가 변경·제거됨
- Godot 초기화 중 `NoSuchMethodError`와 네이티브 `SIGABRT`가 발생하는 것을 ARM Android에서 재현
- 공식 Android 템플릿과 동일하게 릴리스 R8·리소스 축소 비활성화
- 향후 설정이 바뀌어도 Godot 클래스와 JNI 메서드를 보존하는 전체 keep 규칙 추가
- Android 런타임이 요구하는 `project.binary`를 정식 생성하고 ECFG 헤더와 APK 포함 여부 검증
- Activity 생성부터 실제 게임 장면 준비까지 확인하는 부팅 마커 추가
- ARM 배포본과 바이트 단위로 동일한 Godot 에셋을 담은 x86_64 QA APK 생성
- QA APK가 Android에서 실제 게임 장면과 초기 전투까지 생존해야만 배포되는 필수 게이트 추가

## 호환성

- Android 7.0(API 24) 이상
- `arm64-v8a`, `armeabi-v7a`
- 기존 `com.example.bloodmoonnightfall` 설치본 위에 업데이트 가능
- 기존 업데이트 인증서와 R4 성장 저장 데이터 유지

- APK SHA-256: `CDE6A28887BA59BBB63215A6FC4FC3367EAB9008D04375CA1D88FC0BC73F3D87`
- 서명 인증서 SHA-256: `41B6CCB282C142826227EB6D0B6108E5A8F78FC2CFF08E845B9458B2F08FA99D`
