package com.carlohorseprogram.app;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

class HorseManager {
    private List<Horse> horses;
    private int nextHorseNum;

    public HorseManager() {
        horses = new ArrayList<>();
        nextHorseNum = 1;
    }

    public void addHorse(String horseName, int horseAge, String warCry, boolean isHealthy) {
        horses.add(new Horse(horseName, horseAge, warCry, isHealthy, nextHorseNum));
        System.out.println("Horse #" + nextHorseNum + " added successfully!\n");
        nextHorseNum++;
    }

    public void viewHorses() {
        if (horses.isEmpty()) {
            System.out.println("No Horses Available\n");
            return;
        }
        System.out.println("Horses in the Race:\n");
        horses.forEach(System.out::println);
    }

    public List<Horse> getAvailableHorses() {
        return horses.stream()
                .filter(Horse::isHealthy)
                .collect(Collectors.toList());
    }

    public boolean isNameTaken(String name) {
        return horses.stream().anyMatch(horse -> horse.getHorseName().equals(name));
    }

    public String getAgeGroup(Horse horse) {
        Map<String, List<Horse>> ageGroups = classifyHorsesByAge();

        if (ageGroups.get("Advanced").contains(horse)) return "Advanced";
        if (ageGroups.get("Intermediate").contains(horse)) return "Intermediate";
        if (ageGroups.get("Beginner").contains(horse)) return "Beginner";

        return "Unknown";
    }

    public Map<String, List<Horse>> classifyHorsesByAge() {
        // Calculate average age 
        double averageAge = horses.stream()
                .mapToInt(Horse::getHorseAge)
                .average()
                .orElse(0);


        // calculates the frequent age or mode age
        Map<Integer, Long> ageFrequency = horses.stream()
                .collect(Collectors.groupingBy(Horse::getHorseAge, Collectors.counting()));

        // finding the mode age 
        int modeAge = ageFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        // Group horses based on their age compared to average and mode
        Map<String, List<Horse>> groupedHorses = horses.stream()
                .collect(Collectors.groupingBy(horse -> {
                    int age = horse.getHorseAge();
                    if (age == modeAge) return "Advanced";
                    if (age > averageAge) return "Intermediate";
                    return "Beginner";
                }));

        return groupedHorses;
    }




    public boolean isWarCryTaken(String warCry) {
        return horses.stream().anyMatch(horse -> horse.getWarCry().equals(warCry));
    }
}
