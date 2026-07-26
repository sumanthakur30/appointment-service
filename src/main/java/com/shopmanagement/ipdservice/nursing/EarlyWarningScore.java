package com.shopmanagement.ipdservice.nursing;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight NEWS2-style early warning score from nursing vitals.
 * Configuration-friendly: thresholds are explicit and can later move to rule-engine.
 */
public final class EarlyWarningScore {

    private EarlyWarningScore() {}

    public static Map<String, Object> news2(NursingVital v) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (v == null) {
            out.put("score", 0);
            out.put("risk", "NONE");
            return out;
        }
        int score = 0;
        score += respScore(v.getRespRate());
        score += spo2Score(v.getSpo2());
        score += systolicScore(v.getBpSystolic());
        score += pulseScore(v.getPulseBpm());
        score += tempScore(v.getTemperatureC());
        out.put("score", score);
        out.put("risk", riskBand(score));
        out.put("components", Map.of(
                "resp", respScore(v.getRespRate()),
                "spo2", spo2Score(v.getSpo2()),
                "systolic", systolicScore(v.getBpSystolic()),
                "pulse", pulseScore(v.getPulseBpm()),
                "temp", tempScore(v.getTemperatureC())
        ));
        return out;
    }

    private static String riskBand(int score) {
        if (score >= 7) {
            return "HIGH";
        }
        if (score >= 5) {
            return "MEDIUM";
        }
        if (score >= 1) {
            return "LOW";
        }
        return "NONE";
    }

    private static int respScore(Integer rr) {
        if (rr == null) {
            return 0;
        }
        if (rr <= 8 || rr >= 25) {
            return 3;
        }
        if (rr >= 21) {
            return 2;
        }
        if (rr <= 11) {
            return 1;
        }
        return 0;
    }

    private static int spo2Score(java.math.BigDecimal spo2) {
        if (spo2 == null) {
            return 0;
        }
        double s = spo2.doubleValue();
        if (s <= 91) {
            return 3;
        }
        if (s <= 93) {
            return 2;
        }
        if (s <= 95) {
            return 1;
        }
        return 0;
    }

    private static int systolicScore(Integer sys) {
        if (sys == null) {
            return 0;
        }
        if (sys <= 90 || sys >= 220) {
            return 3;
        }
        if (sys <= 100) {
            return 2;
        }
        if (sys <= 110) {
            return 1;
        }
        return 0;
    }

    private static int pulseScore(Integer pulse) {
        if (pulse == null) {
            return 0;
        }
        if (pulse <= 40 || pulse >= 131) {
            return 3;
        }
        if (pulse >= 111) {
            return 2;
        }
        if (pulse <= 50 || pulse >= 91) {
            return 1;
        }
        return 0;
    }

    private static int tempScore(java.math.BigDecimal temp) {
        if (temp == null) {
            return 0;
        }
        double t = temp.doubleValue();
        if (t <= 35.0) {
            return 3;
        }
        if (t >= 39.1) {
            return 2;
        }
        if (t <= 36.0 || t >= 38.1) {
            return 1;
        }
        return 0;
    }
}
