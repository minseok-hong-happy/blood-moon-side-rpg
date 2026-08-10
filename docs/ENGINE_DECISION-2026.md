# VAYLORN 엔진 선정 기록

## 확인한 사례

공식 개발사·엔진 문서만 비교 자료로 사용했다.

| 게임·사례 | 확인된 엔진 | 이 프로젝트에 참고한 점 |
|---|---|---|
| *Hollow Knight* | Unity | 손그림 PNG, 2D Physics, Sprite Packer, Particle System을 결합해 깊이감 있는 횡스크롤 화면을 구성했다. |
| *Skul: The Hero Slayer* | Unity | Tilemap, Sprite Atlas, Pixel Perfect Camera로 2D 캐릭터와 맵의 일관성과 성능을 확보했다. |
| *Greak: Memories of Azur* Android판 | Unity | 실제 기기 프로파일링, 텍스처 해상도 조절, 빌드 크기 관리가 모바일 이식의 핵심이었다. |
| Godot 공식 쇼케이스의 2D·RPG·Android 작품군 | Godot | 노드 기반 2D 장면, GDScript의 빠른 반복, 모바일 배포가 소규모 팀의 콘셉트 검증에 적합하다. |

출처: [Unity의 Hollow Knight 사례](https://unity.com/made-with-unity/hollow-knight), [Unity의 Skul 사례](https://create.unity.com/skul-the-hero-slayer-case-study), [Unity의 Greak Android 최적화 사례](https://unity.com/blog/optimizing-controls-memory-and-build-size-for-greak-memories-of-azur-android-port), [Godot 공식 쇼케이스](https://godotengine.org/showcase/)

## 판단

상용 규모의 긴 제작 파이프라인과 대규모 인력·플러그인 생태계를 우선하면 Unity가 자연스러운 선택이다. 그러나 VAYLORN의 현재 목표는 스토리, 자동전투 속도, 세로형 횡스크롤 구도, 8종 혈술의 감각을 빠르게 확인하는 콘셉트 데모다.

그래서 Godot 4.7.1을 선택했다.

- 2D 스프라이트 프레임, 트윈, 파티클, 오디오, UI를 한 장면 트리에서 빠르게 조정할 수 있다.
- 엔진 계정이나 별도 라이선스 절차 없이 명령줄 테스트와 APK 빌드를 자동화할 수 있다.
- 기존 Java 저장소와 업데이트 서명을 버릴 필요가 없다. Godot Android AAR을 현재 앱에 임베드하고 런타임 플러그인으로 양방향 연결할 수 있다.
- 현재 AI 아트는 2D 아틀라스이므로 3D 모델링 파이프라인보다 Godot의 `AnimatedSprite2D`와 이미지 기반 VFX가 결과 확인까지 훨씬 짧다.

Godot의 공식 Android 문서는 기존 Android 프로젝트 안에 AAR을 임베드하고 `assets`의 Godot 프로젝트를 구동하는 구조를 지원한다. VAYLORN도 이 구조를 사용한다: [Godot Android library 문서](https://docs.godotengine.org/en/4.7/tutorials/platform/android/android_library.html).

## 적용 구조

```text
Android MainActivity (기존 패키지·서명)
├─ GodotActivity / Godot 4.7.1 AAR
├─ VaylornProgress 플러그인
│  └─ 기존 R4 체크섬·이중 슬롯 저장소
└─ Godot 2D 프로젝트
   ├─ 독립 영웅·몬스터·보스 오브젝트
   ├─ 실제 위치 이동 + 상태 잠금 애니메이션
   ├─ 8종 자동 혈술 + 이미지 시퀀스 VFX
   └─ 세로형 HUD·성장·6칸 가방·스토리
```

패키지 ID와 업데이트 인증서는 이전 버전과 동일하다. 엔진 전환 때문에 저장 형식을 초기화하지 않고, Godot가 JSON 브리지를 통해 기존 `RpgProgressStore`를 그대로 읽고 쓴다.
