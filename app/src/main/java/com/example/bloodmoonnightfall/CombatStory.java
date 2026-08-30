package com.example.bloodmoonnightfall;

/**
 * Short story beats that can play over automatic combat without pausing the hunt.
 * Every region owns one opening and one midpoint beat for each of its five waves.
 */
public final class CombatStory {
    public static final int REGION_COUNT = 3;
    public static final int WAVE_COUNT = 5;

    public static final String KAEL = "카엘";
    public static final String RIAN = "리안";
    public static final String NOX = "녹스";

    private static final String[][] OPENING_SPEAKERS = {
            {RIAN, NOX, RIAN, KAEL, "태양의 심판관"},
            {RIAN, NOX, KAEL, "잿빛 수문장", "잿빛 수문장"},
            {RIAN, NOX, KAEL, "월식의 여왕", "월식의 여왕"}
    };

    private static final String[][] OPENING_LINES = {
            {
                    "성역의 맥박을 따라가. 첫 봉인은 잠든 제단 아래 있어.",
                    "죽은 혈족의 피를 마셔라. 힘은 기억보다 정직하다.",
                    "교단 추적대가 가까워져. 푸른 성화를 먼저 끊어.",
                    "심판관의 빛이 보인다. 봉인의 수호자가 기다린다.",
                    "혈월의 그릇을 여기서 태양의 재로 만들겠다."
            },
            {
                    "잿빛 숲은 버린 기억을 먹어. 네 이름부터 붙잡아.",
                    "리안이 첫 봉인을 깼다. 아직도 인간을 믿느냐?",
                    "기억이 거짓말해도 약속은 남는다. 계속 간다.",
                    "배신자의 약속을 품은 자여, 재 속에 무릎 꿇어라.",
                    "첫 봉인은 네 족쇄였다. 두 번째는 네 관이 되리라."
            },
            {
                    "성채의 왕좌 아래에 인공 태양의 심장이 있어.",
                    "세 번째 봉인이 열리면 너는 완전한 혈월이 된다.",
                    "힘이 결말을 정하지 않는다. 선택하는 내가 정한다.",
                    "돌아온 후계자여, 왕좌가 네 피를 기억한다.",
                    "마지막 문을 열려면 인간의 약속부터 버려라."
            }
    };

    private static final String[][] MIDPOINT_SPEAKERS = {
            {KAEL, KAEL, NOX, RIAN, NOX},
            {KAEL, RIAN, NOX, RIAN, KAEL},
            {RIAN, KAEL, NOX, RIAN, KAEL}
    };

    private static final String[][] MIDPOINT_LINES = {
            {
                    "굶주린 혈귀들… 내 심장의 녹스가 불러들였군.",
                    "네 힘은 빌리되, 네 목소리에는 굴복하지 않는다.",
                    "인간의 빛을 베어라. 네가 잃은 밤이 돌아온다.",
                    "갑옷 틈이 붉게 타오를 때 혈술을 집중해.",
                    "저 빛 안에 첫 봉인이 있다. 빼앗아, 카엘."
            },
            {
                    "숲이 보여주는 환영보다 내 발자국이 진짜다.",
                    "내가 봉인을 깬 이유는 성채에서 말할게. 살아서 와.",
                    "그녀는 진실을 미끼로 네 갈증을 길들인다.",
                    "수문장의 재갑은 연속 혈술로 갈라낼 수 있어.",
                    "대답은 리안에게 듣는다. 네 재판은 필요 없어."
            },
            {
                    "붉은 회랑 끝에서 기다릴게. 이번에는 숨기지 않아.",
                    "녹스, 네 기억과 내 의지를 혼동하지 마라.",
                    "인간으로 남겠다는 집착이 너를 가장 굶주리게 한다.",
                    "여왕의 월식은 중심이 밝아질 때 끊을 수 있어.",
                    "나는 그릇도 왕도 아니다. 새벽을 만드는 칼이다."
            }
    };

    private static final String[] LOOP_SPEAKERS = {NOX, RIAN, KAEL};
    private static final String[] LOOP_LINES = {
            "또 같은 밤이군. 기억은 되감겨도 네 갈증은 남았다.",
            "되풀이된 숲에서도 네가 나를 믿는지 확인하고 싶어.",
            "같은 결말은 거부한다. 이번 밤에는 더 멀리 간다."
    };

    private static final String[] SEAL_OBJECTIVES = {
            "첫 봉인 · 태양의 심판관",
            "두 번째 봉인 · 잿빛 수문장",
            "마지막 봉인 · 월식의 여왕"
    };

    private CombatStory() {
    }

    public static Beat opening(int region, int wave, int chapterClears) {
        int safeRegion = RpgRules.clamp(region, 0, REGION_COUNT - 1);
        int safeWave = RpgRules.clamp(wave, 1, WAVE_COUNT) - 1;
        if (chapterClears > 0 && safeWave == 0) {
            return new Beat(LOOP_SPEAKERS[safeRegion], LOOP_LINES[safeRegion]);
        }
        return new Beat(OPENING_SPEAKERS[safeRegion][safeWave],
                OPENING_LINES[safeRegion][safeWave]);
    }

    public static Beat midpoint(int region, int wave) {
        int safeRegion = RpgRules.clamp(region, 0, REGION_COUNT - 1);
        int safeWave = RpgRules.clamp(wave, 1, WAVE_COUNT) - 1;
        return new Beat(MIDPOINT_SPEAKERS[safeRegion][safeWave],
                MIDPOINT_LINES[safeRegion][safeWave]);
    }

    public static String sealObjective(int region) {
        return SEAL_OBJECTIVES[RpgRules.clamp(region, 0, REGION_COUNT - 1)];
    }

    public static final class Beat {
        public final String speaker;
        public final String line;

        private Beat(String speaker, String line) {
            this.speaker = speaker;
            this.line = line;
        }
    }
}
