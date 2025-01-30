package com.carlohorseprogram.app;

import java.util.Scanner;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Optional;


public class HorseRace {
    private static Scanner sc = new Scanner(System.in);
    private HorseManager manager;
    public static final String[] RANDOM_NAMES = {"Thunder", "Lightning", "Blaze", 
    "Shadow", "Spirit", "Storm", "Flash", "Bolt", "Eclipse", "Comet"};
    public static final String[] RANDOM_WARCRIES = {"Neigh!", "Yeehaw!", "Charge!", 
    "Gallop!", "Let's Go!", "Run!", "Dash!", "Zoom!", "Vroom!", "Go!"};
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("mm:ss:SSS");

    public HorseRace() {
        manager = new HorseManager();
    }

    void addRaceHorse(int numHorse) {
        for (int i = 0; i < numHorse; i++) {
            String choice;
            do {
                System.out.print("Do you want to add a horse from a random horse (R) or create one (C)? ");
                choice = sc.next().trim().toUpperCase();
                
                if (choice.equals("R")) {
                    addRandomHorse();
                } else if (choice.equals("C")) {
                    addCustomHorse();
                } else {
                    System.out.println("Invalid choice. R or C only");
                }
            } while (!choice.equals("R") && !choice.equals("C"));
        }
    }

    private void addCustomHorse() {
        String name = "";
        int age = -1;
        String warCry = "";
        boolean isHealthy = false;
        String condition = "";

        // Input Horse Name
        do{
            System.out.print("Enter Horse Name: ");
            name = sc.next();
            if (name.isEmpty()) {
                System.out.println("Horse name cannot be empty. Please try again.");
            }
        }while(name.isEmpty());


        // Input Horse Age
        do {
            System.out.print("Enter Horse Age: ");
            try {
                age = sc.nextInt();
                if (age > 0) {
                    break;  // Exit the loop if a valid age is entered
                } else {
                    System.out.println("Age must be a positive integer. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
                sc.next(); // Consume the invalid input
            }
        } while (age < 0);  // Continue looping if the age is not positive


        // Input Horse Warcry
        do{
            System.out.print("Enter Horse Warcry: ");
            warCry = sc.next();
            if (warCry.isEmpty()) {
                System.out.println("Horse warcry cannot be empty. Please try again.");
            }
        }while(warCry.isEmpty());


        // Input Horse Condition
        do {
            System.out.print("Enter Horse Condition (H)Healthy, (U)Unhealthy: ");
            condition = sc.next().trim().toUpperCase();
            if (condition.equals("H")) {
                isHealthy = true;
            } else if (condition.equals("U")) {
                isHealthy = false;
            } else {
                System.out.println("Invalid input. Please enter 'H' or 'U'.");
            }
        } while (!condition.equals("H") && !condition.equals("U")); // Repeat while input is invalid


        // Add Horse to Manager
        manager.addHorse(name, age, warCry, isHealthy);
    }

    private void addRandomHorse() {
        Random random = new Random();

        String name;
        do {
            name = RANDOM_NAMES[random.nextInt(RANDOM_NAMES.length)];
        } while (manager.isNameTaken(name));

        String warCry;
        do {
            warCry = RANDOM_WARCRIES[random.nextInt(RANDOM_WARCRIES.length)];
        } while (manager.isWarCryTaken(warCry));

        int age = random.nextInt(25) + 1; // Random age between 1 and 15
        boolean isHealthy = random.nextBoolean();

        manager.addHorse(name, age, warCry, isHealthy);
    }



    void startRace(int distance) {

        Map<String, List<Horse>> ageGroups = manager.classifyHorsesByAge();
    
        System.out.println("Age Group Classification:");
        ageGroups.forEach((group, horses) -> {
            System.out.println(group + ":");
            horses.forEach(horse -> System.out.println(horse.getHorseName()));
        });

        List<Horse> healthyHorses = manager.getAvailableHorses();

        System.out.println("\n3... 2... 1... Go!");

        Map<Horse, Double> horseDistances = new ConcurrentHashMap<>();
        Map<Horse, Integer> horseRunCounts = new ConcurrentHashMap<>();
        List<Horse> finishedHorses = Collections.synchronizedList(new ArrayList<>()); // Synchronized list to store finished horses

        healthyHorses.forEach(horse -> {
            horseDistances.put(horse, (double) distance); // Set the initial distance to target distance
            horseRunCounts.put(horse, 0);  // Initialize the run count
        });

        // Loop = checks if all the horses have finished
        while (finishedHorses.size() < healthyHorses.size()) {

            healthyHorses.parallelStream().forEach(horse -> {

                // Skip horses that have already finished
                if (finishedHorses.contains(horse)) {
                    return;
                }

                String ageGroup = manager.getAgeGroup(horse);
                int runCount = horseRunCounts.get(horse) + 1; // Increment generation run
                horseRunCounts.put(horse, runCount);

                double speed = horse.generateSpeed(runCount, ageGroup);
                double remainingDistance = horseDistances.get(horse);

                // Subtract the speed from the remaining distance
                double newRemainingDistance = remainingDistance - speed;
                //no negative
                if(newRemainingDistance < 0){
                    newRemainingDistance = 0;
                }

                
                horseDistances.put(horse, newRemainingDistance);

                String timestamp = LocalTime.now().format(timeFormatter);

                // Print the distance reduced by speed
                System.out.printf("[%s] Horse#%d: ran %.2f. Remaining Distance: %.1f%n",
                    timestamp, horse.getHorseNum(), speed, newRemainingDistance);

                // Check if the horse has finished the race and add to the list
                if (newRemainingDistance <= 0) {
                    synchronized (finishedHorses) { //thread safe
                        if (!finishedHorses.contains(horse)) {
                            finishedHorses.add(horse);
                            System.out.printf("Horse#%d - %s finished the race!%n", 
                                horse.getHorseNum(), horse.getHorseName());
                        }
                    }
                }

                // Delay
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Race interrupted!");
                }
            });
        }

        // Leaderboard
        System.out.println("\nRace Finished!\nLeaderboard:");
        printLeaderboard(horseDistances, finishedHorses);
    }

    private void printLeaderboard(Map<Horse, Double> horseDistances, List<Horse> finishedHorses) {
        Horse winner = finishedHorses.get(0);
        System.out.printf("Winner! Horse#%d: %s | \"%s\"%n", winner.getHorseNum(),
                        winner.getHorseName(), winner.getWarCry());
        System.out.println("\nFinish Order:");
        finishedHorses.forEach(horse -> 
            System.out.printf("%d. Horse#%d - %s%n", 
                      finishedHorses.indexOf(horse) + 1, 
                      horse.getHorseNum(), 
                      horse.getHorseName()));

    }
    public static void main(String[] args) {
        HorseRace race = new HorseRace();
        boolean exit = false;
        while (!exit) {
            printMenu();
            System.out.print("Action: ");
            int action = sc.nextInt();
            switch (action) {
                case 0 -> exit = true;
                case 1 -> race.actionAddHorse();
                case 2 -> race.manager.viewHorses();
                case 3 -> race.actionStartRace();
                default -> System.out.println("Invalid option, try again.");
            }
        }
    }

    void actionAddHorse(){
        int numHorse = 0;
        boolean addHorseAction = false;
        while (!addHorseAction) {
            try {
                System.out.print("How many horses to add?: ");
                numHorse = sc.nextInt();

                if (numHorse > 0) { // Validate that the number is positive
                    addRaceHorse(numHorse);
                    addHorseAction = true;
                } else {
                    System.out.println("Please enter a positive number.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a valid integer.");
                sc.next(); // Clear the invalid input
            }
        }
    }

    void actionStartRace(){
        //Check on available horses
        Optional<List<Horse>> availHorses = Optional.of(manager.getAvailableHorses());
        availHorses.filter(horses -> !horses.isEmpty())
            .ifPresentOrElse(
            healthyHorses -> {
                System.out.println("Healthy horses available: " + healthyHorses.size());
                boolean started = false;
                while(!started){
                    try{
                        System.out.print("Enter race distance: ");
                        int distance = sc.nextInt();
                        if(distance > 0 ){
                            startRace(distance);
                            started = true;
                        }else{
                            System.out.println("Enter a positive number");
                        }

                    }catch(InputMismatchException e){
                        System.out.println("Please enter a number");
                        sc.next();
                    }
                                            
                }
            },
                () -> System.out.println("No healthy horses to race!")
                );
    }

    static void printMenu() {
        System.out.println("\n[MENU]");
        System.out.println("[1] - Add Horses");
        System.out.println("[2] - View Horses");
        System.out.println("[3] - Start Race");
        System.out.println("[0] - Exit");
    }
}

