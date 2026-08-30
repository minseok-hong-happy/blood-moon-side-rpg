# 전투 비주얼 임팩트 비교 조사와 적용

조사일: 2026-08-09

## 비교 대상에서 추출한 원칙

- **던전앤파이터 모바일**: 공식 소개가 벨트스크롤 액션, 연계 콤보와 고유 스킬을 핵심으로 내세운다. VAYLORN에는 자산을 복제하지 않고 `접촉 전조 → 밝은 접촉핵 → 파편 → 잔상`의 짧은 타격 문법과 연속 HIT 판독만 적용했다.
- **나 혼자만 레벨업: ARISE**: 공식 소개가 회피 이후의 타이밍 액션, 강력한 보스와 레이드를 강조한다. 데모에는 전투 흐름을 멈추지 않는 짧은 보스 등장 배너와 처치 전용 대형 임팩트를 적용했다.
- **세븐나이츠 키우기**: 공식 소개가 자동전투와 다수 캐릭터 전투의 화려함을 함께 강조한다. 자동사냥 중에도 어떤 혈술이 발동했는지 즉시 읽히도록 짧은 혈술명 컷인을 추가했다.
- **Legend of Slime**: 공식 소개가 자동전투, 지속 성장, 보스와 스킬을 핵심 루프로 제시한다. 방치 흐름은 유지하되 일반타격·강타·처치의 시각 보상 크기를 분리했다.
- **메이플스토리M**: 공식 제품 페이지가 앱을 닫은 동안에도 이어지는 자동전투를 강조한다. 장시간 반복 시 피로가 쌓이지 않도록 화면 흔들림을 늘리지 않고 국소 발광·실루엣 플래시·타이포그래피로 임팩트를 보강했다.

## v4.15.0 적용 결정

1. 4프레임 × 3등급의 오리지널 손그림 타격 아틀라스를 추가했다.
2. 일반타격, 강타, 보스/처치 임팩트의 크기와 재생 시간을 분리했다.
3. 적을 투명하게 깜빡이는 방식 대신 짧은 백색 실루엣 플래시를 사용한다.
4. 피해 숫자에 외곽선, 팝 스케일과 처치 위계를 추가했다.
5. 연속 공격에는 HIT 카운터, 자동 혈술에는 이름 컷인을 표시한다.
6. 보스 출현과 2페이즈 전환에는 전용 배너와 피니셔 임팩트를 사용한다.
7. 이미지 디코딩 실패 시 절차적 광선 효과로 전환하고, 화면 종료 시 아틀라스를 명시적으로 해제한다.

다른 게임의 이미지, 캐릭터, 문양, 로고 및 UI를 복사하지 않았다. 비교 대상에서는 공개된 공식 설명과 일반적인 액션 피드백 원칙만 추출했으며, 최종 이미지는 VAYLORN의 독립적인 붉은 고딕 아트로 새로 제작했다.

## 공식 출처

- [던전앤파이터 모바일 — Google Play](https://play.google.com/store/apps/details?id=com.nexon.mdnf)
- [Solo Leveling: ARISE — Google Play](https://play.google.com/store/apps/details?hl=en_US&id=com.netmarble.sololv)
- [Seven Knights Idle Adventure — Google Play](https://play.google.com/store/apps/details?hl=en_US&id=com.netmarble.skiagb)
- [Legend of Slime — Google Play](https://play.google.com/store/apps/details?hl=en-US&id=com.loadcomplete.slimeidle)
- [MapleStory M — Nexon](https://www.nexon.com/main/en/MapleStory%20M/details)
