package com.bountygrid.util;

public final class BadgeEvaluator {
    private BadgeEvaluator() {
    }

    public static String evaluate(int points, int finds) {
        if (points >= 10000 || finds >= 100) return "legend";
        if (points >= 5000 || finds >= 50) return "guardian";
        if (points >= 2500 || finds >= 25) return "hero";
        if (points >= 1000 || finds >= 10) return "champion";
        if (points >= 500 || finds >= 5) return "helper";
        return "newcomer";
    }
}
