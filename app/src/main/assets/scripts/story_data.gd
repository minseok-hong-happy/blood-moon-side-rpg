class_name StoryData
extends RefCounted

const REGION_NAMES := ["잿빛 성역", "망각의 숲", "진홍 성채"]
const REGION_SUBTITLES := ["빛이 죽은 순례자의 길", "이름을 먹는 검은 안개", "붉은 달이 잠들지 않는 왕좌"]
const BOSS_NAMES := ["태양의 심판관", "망각의 문지기", "월식의 군주"]
const BOSS_EPITHETS := ["거짓 태양의 대행자", "죽은 성문의 파수꾼", "붉은 달을 삼킨 왕"]
const SEAL_OBJECTIVES := ["첫 번째 봉인 · 태양의 심판관", "두 번째 봉인 · 망각의 문지기", "마지막 봉인 · 월식의 군주"]

const OPENING_SPEAKERS := [
	["카엘", "리안", "카엘", "리안", "태양의 심판관"],
	["녹스", "리안", "카엘", "망각의 문지기", "망각의 문지기"],
	["카엘", "리안", "솔바르", "월식의 군주", "월식의 군주"],
]

const OPENING_LINES := [
	[
		"성역의 맥박이 떨린다. 첫 봉인은 무너진 제단 너머에 있어.",
		"죽은 성직자들이 길을 막았어. 그들은 기억보다 집요해.",
		"추적대가 가까워진다. 피의 성화를 먼저 꺼야 해.",
		"심판관의 빛이 보인다. 봉인의 수호자가 기다리고 있어.",
		"네 피를 태워 거짓 태양의 재로 만들겠다.",
	],
	[
		"망각의 숲은 버린 기억을 먹어. 네 이름부터 붙잡아.",
		"첫 봉인은 깼다. 하지만 추적대의 냄새는 더 짙어졌어.",
		"기억이 거짓말해도 맹세는 남는다. 계속 간다.",
		"배신자의 맹세여, 이 숲에 무릎 꿇어라.",
		"두 번째 봉인은 네 존재와 함께 사라질 것이다.",
	],
	[
		"성채의 왕좌 아래에 인간의 태양이 묻혀 있어.",
		"두 봉인을 깨면 붉은 달이 완전히 뜬다.",
		"힘이 결말을 정하지 않아. 선택하는 자가 정한다.",
		"돌아온 후계자여, 왕좌가 네 피를 기억한다.",
		"마지막 문을 열려면 인간의 맹세부터 버려라.",
	],
]

const MIDPOINT_SPEAKERS := [
	["리안", "카엘", "녹스", "리안", "녹스"],
	["리안", "카엘", "녹스", "카엘", "리안"],
	["카엘", "솔바르", "녹스", "리안", "카엘"],
]

const MIDPOINT_LINES := [
	[
		"순례자들이 한꺼번에 몰려온다. 누군가 북을 울렸어.",
		"빛은 빌린 힘이야. 내 피를 이길 수 없어.",
		"인간의 빛을 베어낼수록 달의 밤이 돌아온다.",
		"갑옷 틈이 붉게 탄다. 혈술을 집중해.",
		"저 빛 안에 첫 봉인이 있다. 피를 아끼지 마.",
	],
	[
		"숲이 보여 주는 환영보다 네 발자국이 진짜야.",
		"내가 봉인을 깬 이유는 성채에서 말할게. 살아남는다면.",
		"그들은 진실을 미끼로 네 갈증을 길들인다.",
		"문지기의 가면은 연속 혈술로 갈라낼 수 있어.",
		"대답은 리안에게 듣는다. 네 심판은 필요 없어.",
	],
	[
		"붉은 달 아래서 기다리겠다. 이번에는 숨기지 않아.",
		"베스, 내 기억과 네 의지를 혼동하지 마라.",
		"인간으로 끝내려는 집착이 네 칼을 무겁게 한다.",
		"왕좌의 월식은 중심을 막지 못하면 끝이 없어.",
		"나는 그들의 왕도 아니고 네 그림자도 아니다.",
	],
]

const LOOP_SPEAKERS := ["녹스", "리안", "카엘"]
const LOOP_LINES := [
	"같은 밤이지만 적의 진형이 달라졌어. 봉인을 다시 확인해.",
	"되풀이된 숲에서도 발자국은 거짓말하지 않아.",
	"결말은 거부한다. 이번 밤에는 더 멀리 간다.",
]


static func opening(region: int, wave: int, chapter_clears: int) -> Dictionary:
	var safe_region := clampi(region, 0, 2)
	var safe_wave := clampi(wave, 1, 5) - 1
	if chapter_clears > 0 and safe_wave == 0:
		return {"speaker": LOOP_SPEAKERS[safe_region], "line": LOOP_LINES[safe_region]}
	return {"speaker": OPENING_SPEAKERS[safe_region][safe_wave],
		"line": OPENING_LINES[safe_region][safe_wave]}


static func midpoint(region: int, wave: int) -> Dictionary:
	var safe_region := clampi(region, 0, 2)
	var safe_wave := clampi(wave, 1, 5) - 1
	return {"speaker": MIDPOINT_SPEAKERS[safe_region][safe_wave],
		"line": MIDPOINT_LINES[safe_region][safe_wave]}
