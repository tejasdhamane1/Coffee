package com.beanbrew.simulator;

import com.beanbrew.entity.CoffeeType;

import java.time.LocalTime;
import java.util.Random;

/**
 * Produces realistic-looking consumption behaviour based on time of day and department,
 * instead of pure random noise. Used both by seed data generation and by "Start Demo Mode".
 */
public class SimulatorEngine {

    private static final Random RANDOM = new Random();

    /** Relative demand multiplier for a given hour of day (office consumption curve). */
    public static double demandMultiplier(int hour) {
        if (hour >= 8 && hour <= 10) return 1.6;   // morning rush
        if (hour == 11) return 1.1;
        if (hour >= 12 && hour <= 14) return 1.3;  // lunch
        if (hour >= 15 && hour <= 18) return 1.5;  // afternoon/evening peak
        if (hour >= 19 && hour <= 21) return 0.6;
        return 0.15; // night
    }

    /** Department-level baseline activity multiplier. */
    public static double departmentMultiplier(String department) {
        return switch (department.toLowerCase()) {
            case "engineering" -> 1.4;
            case "cafeteria" -> 1.6;
            case "management" -> 0.8;
            case "hr" -> 0.9;
            case "finance" -> 1.0;
            default -> 1.0;
        };
    }

    /** Picks a coffee type with realistic weighting (cappuccino/latte more popular than black coffee). */
    public static CoffeeType weightedCoffeeType() {
        double roll = RANDOM.nextDouble();
        if (roll < 0.28) return CoffeeType.CAPPUCCINO;
        if (roll < 0.50) return CoffeeType.LATTE;
        if (roll < 0.68) return CoffeeType.ESPRESSO;
        if (roll < 0.83) return CoffeeType.AMERICANO;
        if (roll < 0.94) return CoffeeType.MOCHA;
        return CoffeeType.BLACK_COFFEE;
    }

    /** Estimated cups dispensed in one "tick" given hour and department. */
    public static int cupsForTick(int hour, String department) {
        double base = 2.5 * demandMultiplier(hour) * departmentMultiplier(department);
        double noisy = base * (0.7 + RANDOM.nextDouble() * 0.6);
        return Math.max(0, (int) Math.round(noisy));
    }

    public static double inventoryDepletionPerCup(CoffeeType type) {
        return switch (type) {
            case LATTE, CAPPUCCINO, MOCHA -> 1.3;
            case AMERICANO -> 0.9;
            case ESPRESSO -> 0.7;
            case BLACK_COFFEE -> 0.8;
        };
    }

    public static int currentHour() {
        return LocalTime.now().getHour();
    }
}
