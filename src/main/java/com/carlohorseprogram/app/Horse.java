package com.carlohorseprogram.app;
import java.util.Random;
import java.util.List;

class Horse {
    private String horseName;
    private int horseAge;
    private String warCry;
    private int horseNumber;
    private boolean horseCondition;

    public Horse(String horseName, int horseAge, String warCry, boolean horseCondition, int horseNumber) {
        this.horseName = horseName;
        this.horseAge = horseAge;
        this.warCry = warCry;
        this.horseCondition = horseCondition;
        this.horseNumber = horseNumber;
    }

    public boolean isHealthy() {
        return horseCondition;
    }

    public int getHorseAge() {
        return horseAge;
    }

    public double generateSpeed(int runCount, String ageGroup) {
        Random rnd = new Random();
        int baseSpeed = rnd.nextInt(10) + 1; // Base speed between 1 and 10
        if(ageGroup.equals("Advanced")  && runCount >= 3){
            return rnd.nextInt(6) + 5;
        }else if(ageGroup == "Intermediate" && runCount >= 5){
            double boostSpeed = baseSpeed * 1.1;
            return (boostSpeed > 10) ? 10 : boostSpeed; // 10% speed boost
        }
        return baseSpeed; // Default to base speed if no condition is met
    }

    public String getWarCry() {
        return warCry;
    }

    public int getHorseNum() {
        return horseNumber;
    }

    public String getHorseName() {
        return horseName;
    }
    

    @Override
    public String toString() {
        return "Horse#" + horseNumber + "\nName: " + horseName + "\nAge: " + horseAge + "\nWar Cry: " + warCry + "\n" +
                "Condition: " + (horseCondition ? "Healthy" : "Unhealthy") + "\n";
    }
}