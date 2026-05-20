

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import exceptions.*;
import game.Battle;
import game.Civilization;
import interfaces.MilitaryUnit;
import interfaces.Variables;
import units.attack.*;

public class Main implements Variables {

    private static Civilization civilization = new Civilization();
    private static ArrayList<MilitaryUnit>[] enemyArmy;
    private static boolean enemyArrived = false;
    private static ArrayList<String> battleReports = new ArrayList<String>();
    private static ArrayList<String> battleDevelopments = new ArrayList<String>();
    private static Scanner scanner = new Scanner(System.in);
    private static Random random = new Random();
    private static Timer timer = new Timer();

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        System.out.println("===========================================");
        System.out.println("        WELCOME TO CIVILIZATIONS           ");
        System.out.println("===========================================");

        // Give starting resources
        civilization.setFood(200000);
        civilization.setWood(200000);
        civilization.setIron(50000);
        civilization.setMana(0);

        // TimerTask: generate resources every 60 seconds
        TimerTask resourceTask = new TimerTask() {
            public void run() {
                civilization.generateResources();
                System.out.println("\n[RESOURCES GENERATED] Food+" + civilization.getFoodGeneration()
                    + " Wood+" + civilization.getWoodGeneration()
                    + " Iron+" + civilization.getIronGeneration()
                    + " Mana+" + civilization.getManaGeneration());
            }
        };
        timer.schedule(resourceTask, 60000, 60000);

        // TimerTask: create enemy army and battle every 3 minutes
        TimerTask enemyTask = new TimerTask() {
            public void run() {
                enemyArmy = createEnemyArmy();
                enemyArrived = true;
                System.out.println("\n*** WARNING: ENEMY ARMY IS APPROACHING! ***");
                viewThreat();
                System.out.println("(Choose option 5 to battle or it starts automatically in 30 seconds)");

                // Auto-battle after 30 seconds if player hasn't done it
                TimerTask autoBattle = new TimerTask() {
                    public void run() {
                        if (enemyArrived) {
                            System.out.println("\n[AUTO-BATTLE] Enemy attacks!");
                            startBattle();
                        }
                    }
                };
                timer.schedule(autoBattle, 30000);
            }
        };
        timer.schedule(enemyTask, 180000, 180000);

        // Main game loop
        boolean running = true;
        while (running) {
            printMainMenu();
            String option = scanner.nextLine().trim();

            if (option.equals("1")) {
                menuBuildUnits();
            } else if (option.equals("2")) {
                menuBuildings();
            } else if (option.equals("3")) {
                menuTechnology();
            } else if (option.equals("4")) {
                viewBattleReports();
            } else if (option.equals("5")) {
                if (enemyArrived) {
                    startBattle();
                } else {
                    System.out.println("No enemy is attacking right now.");
                }
            } else if (option.equals("6")) {
                if (enemyArrived) {
                    viewThreat();
                } else {
                    System.out.println("No enemy threat at the moment.");
                }
            } else if (option.equals("7")) {
                civilization.printStats();
            } else if (option.equals("0")) {
                System.out.println("Goodbye!");
                timer.cancel();
                running = false;
            } else {
                System.out.println("Invalid option, please try again.");
            }
        }
        scanner.close();
    }

    // -------------------------
    // MENUS
    // -------------------------

    private static void printMainMenu() {
        System.out.println("\n========== MAIN MENU ==========");
        System.out.println("1) Manage Units");
        System.out.println("2) Manage Buildings");
        System.out.println("3) Manage Technology");
        System.out.println("4) View Battle Reports");
        if (enemyArrived) System.out.println("5) BATTLE NOW (Enemy approaching!)");
        else System.out.println("5) Battle (no enemy yet)");
        System.out.println("6) View Enemy Threat");
        System.out.println("7) View Civilization Stats");
        System.out.println("0) Exit");
        System.out.print("Option > ");
    }

    private static void menuBuildUnits() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- BUILD UNITS ---");
            System.out.println("1) Swordsman (Food:" + FOOD_COST_SWORDSMAN + " Wood:" + WOOD_COST_SWORDSMAN + " Iron:" + IRON_COST_SWORDSMAN + ")");
            System.out.println("2) Spearman  (Food:" + FOOD_COST_SPEARMAN + " Wood:" + WOOD_COST_SPEARMAN + " Iron:" + IRON_COST_SPEARMAN + ")");
            System.out.println("3) Crossbow  (Wood:" + WOOD_COST_CROSSBOW + " Iron:" + IRON_COST_CROSSBOW + ")");
            System.out.println("4) Cannon    (Wood:" + WOOD_COST_CANNON + " Iron:" + IRON_COST_CANNON + ")");
            System.out.println("5) Arrow Tower (Wood:" + WOOD_COST_ARROWTOWER + ")");
            System.out.println("6) Catapult  (Wood:" + WOOD_COST_CATAPULT + " Iron:" + IRON_COST_CATAPULT + ")");
            System.out.println("7) Rocket Launcher Tower (Wood:" + WOOD_COST_ROCKETLAUNCHERTOWER + " Iron:" + IRON_COST_ROCKETLAUNCHERTOWER + ")");
            System.out.println("8) Magician  (Food:" + FOOD_COST_MAGICIAN + " Wood:" + WOOD_COST_MAGICIAN + " Iron:" + IRON_COST_MAGICIAN + " Mana:" + MANA_COST_MAGICIAN + ")");
            System.out.println("9) Priest    (Food:" + FOOD_COST_PRIEST + " Mana:" + MANA_COST_PRIEST + ")");
            System.out.println("0) Back");
            System.out.print("Option > ");
            String opt = scanner.nextLine().trim();
            int amount = 0;

            if (opt.equals("0")) {
                back = true;
            } else {
                System.out.print("How many? > ");
                String amountStr = scanner.nextLine().trim();
                try {
                    amount = Integer.parseInt(amountStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number.");
                    continue;
                }
                if (opt.equals("1")) {
                    try { civilization.newSwordsman(amount); }
                    catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
                } else if (opt.equals("2")) {
                    try { civilization.newSpearman(amount); }
                    catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
                } else if (opt.equals("3")) {
                    try { civilization.newCrossbow(amount); }
                    catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
                } else if (opt.equals("4")) {
                    try { civilization.newCannon(amount); }
                    catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
                } else if (opt.equals("5")) {
                    try { civilization.newArrowTower(amount); }
                    catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
                } else if (opt.equals("6")) {
                    try { civilization.newCatapult(amount); }
                    catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
                } else if (opt.equals("7")) {
                    try { civilization.newRocketLauncher(amount); }
                    catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
                } else if (opt.equals("8")) {
                    try { civilization.newMagician(amount); }
                    catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
                    catch (BuildingException e) { System.out.println("[ERROR] " + e.getMessage()); }
                } else if (opt.equals("9")) {
                    try { civilization.newPriest(amount); }
                    catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
                    catch (BuildingException e) { System.out.println("[ERROR] " + e.getMessage()); }
                } else {
                    System.out.println("Invalid option.");
                }
            }
        }
    }

    private static void menuBuildings() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- BUILDINGS ---");
            System.out.println("1) Farm        (Food:" + FOOD_COST_FARM + " Wood:" + WOOD_COST_FARM + " Iron:" + IRON_COST_FARM + ")");
            System.out.println("2) Carpentry   (Food:" + FOOD_COST_CARPENTRY + " Wood:" + WOOD_COST_CARPENTRY + " Iron:" + IRON_COST_CARPENTRY + ")");
            System.out.println("3) Smithy      (Food:" + FOOD_COST_SMITHY + " Wood:" + WOOD_COST_SMITHY + " Iron:" + IRON_COST_SMITHY + ")");
            System.out.println("4) Magic Tower (Food:" + FOOD_COST_MAGICTOWER + " Wood:" + WOOD_COST_MAGICTOWER + " Iron:" + IRON_COST_MAGICTOWER + ")");
            System.out.println("5) Church      (Food:" + FOOD_COST_CHURCH + " Wood:" + WOOD_COST_CHURCH + " Iron:" + IRON_COST_CHURCH + " Mana:" + MANA_COST_CHURCH + ")");
            System.out.println("0) Back");
            System.out.print("Option > ");
            String opt = scanner.nextLine().trim();

            if (opt.equals("0")) {
                back = true;
            } else if (opt.equals("1")) {
                try { civilization.newFarm(); }
                catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
            } else if (opt.equals("2")) {
                try { civilization.newCarpentry(); }
                catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
            } else if (opt.equals("3")) {
                try { civilization.newSmithy(); }
                catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
            } else if (opt.equals("4")) {
                try { civilization.newMagicTower(); }
                catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
            } else if (opt.equals("5")) {
                try { civilization.newChurch(); }
                catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static void menuTechnology() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- TECHNOLOGY ---");
            System.out.println("Current levels: Attack=" + civilization.getTechnologyAttack()
                + " Defense=" + civilization.getTechnologyDefense());
            System.out.println("1) Upgrade Attack Technology");
            System.out.println("2) Upgrade Defense Technology");
            System.out.println("0) Back");
            System.out.print("Option > ");
            String opt = scanner.nextLine().trim();

            if (opt.equals("0")) {
                back = true;
            } else if (opt.equals("1")) {
                try { civilization.upgradeTechnologyAttack(); }
                catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
            } else if (opt.equals("2")) {
                try { civilization.upgradeTechnologyDefense(); }
                catch (ResourceException e) { System.out.println("[WARNING] " + e.getMessage()); }
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    // -------------------------
    // BATTLE REPORTS
    // -------------------------

    private static void viewBattleReports() {
        if (battleReports.isEmpty()) {
            System.out.println("No battles yet.");
            return;
        }
        System.out.println("\n--- BATTLE REPORTS ---");
        int i = 0;
        while (i < battleReports.size()) {
            System.out.println((i + 1) + ") Battle " + (i + 1));
            i++;
        }
        System.out.println("0) Back");
        System.out.print("Select Report (0 to go back): ");
        String opt = scanner.nextLine().trim();
        int selected = -1;
        try {
            selected = Integer.parseInt(opt);
        } catch (NumberFormatException e) {
            System.out.println("Invalid option.");
            return;
        }
        if (selected == 0) return;
        if (selected < 1 || selected > battleReports.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        System.out.println(battleReports.get(selected - 1));
        System.out.print("View Battle Development? (s/n): ");
        String ans = scanner.nextLine().trim().toLowerCase();
        if (ans.equals("s")) {
            System.out.println(battleDevelopments.get(selected - 1));
        }
    }

    // -------------------------
    // ENEMY ARMY CREATION
    // -------------------------

    @SuppressWarnings("unchecked")
    private static ArrayList<MilitaryUnit>[] createEnemyArmy() {
        ArrayList<MilitaryUnit>[] army = new ArrayList[4];
        int i = 0;
        while (i < 4) {
            army[i] = new ArrayList<MilitaryUnit>();
            i++;
        }

        int battles = civilization.getBattles();
        int extraPercent = battles * ENEMY_FLEET_INCREASE;
        int ironAvail = IRON_BASE_ENEMY_ARMY + (IRON_BASE_ENEMY_ARMY * extraPercent / 100);
        int woodAvail = WOOD_BASE_ENEMY_ARMY + (WOOD_BASE_ENEMY_ARMY * extraPercent / 100);
        int foodAvail = FOOD_BASE_ENEMY_ARMY + (FOOD_BASE_ENEMY_ARMY * extraPercent / 100);

        // Keep creating units while we can afford cheapest unit (Swordsman)
        boolean canCreate = true;
        while (canCreate) {
            boolean enoughForSwordsman = foodAvail >= FOOD_COST_SWORDSMAN
                && woodAvail >= WOOD_COST_SWORDSMAN
                && ironAvail >= IRON_COST_SWORDSMAN;

            if (!enoughForSwordsman) {
                canCreate = false;
            } else {
                // Pick unit by probability: Swordsman 35%, Spearman 25%, Crossbow 20%, Cannon 20%
                int rnd = random.nextInt(100) + 1;
                int unitType;
                if (rnd <= 35) {
                    unitType = 0; // Swordsman
                } else if (rnd <= 60) {
                    unitType = 1; // Spearman
                } else if (rnd <= 80) {
                    unitType = 2; // Crossbow
                } else {
                    unitType = 3; // Cannon
                }

                // Check if we can afford chosen unit, if not try swordsman
                boolean canAffordChosen = canAffordUnit(unitType, foodAvail, woodAvail, ironAvail);
                if (!canAffordChosen) {
                    unitType = 0; // fallback to swordsman
                }

                boolean canAffordFallback = canAffordUnit(unitType, foodAvail, woodAvail, ironAvail);
                if (!canAffordFallback) {
                    canCreate = false;
                } else {
                    MilitaryUnit unit = null;
                    if (unitType == 0) {
                        unit = new Swordsman();
                        foodAvail = foodAvail - FOOD_COST_SWORDSMAN;
                        woodAvail = woodAvail - WOOD_COST_SWORDSMAN;
                        ironAvail = ironAvail - IRON_COST_SWORDSMAN;
                    } else if (unitType == 1) {
                        unit = new Spearman();
                        foodAvail = foodAvail - FOOD_COST_SPEARMAN;
                        woodAvail = woodAvail - WOOD_COST_SPEARMAN;
                        ironAvail = ironAvail - IRON_COST_SPEARMAN;
                    } else if (unitType == 2) {
                        unit = new Crossbow();
                        woodAvail = woodAvail - WOOD_COST_CROSSBOW;
                        ironAvail = ironAvail - IRON_COST_CROSSBOW;
                    } else {
                        unit = new Cannon();
                        woodAvail = woodAvail - WOOD_COST_CANNON;
                        ironAvail = ironAvail - IRON_COST_CANNON;
                    }
                    army[unitType].add(unit);
                }
            }
        }
        return army;
    }

    private static boolean canAffordUnit(int unitType, int food, int wood, int iron) {
        if (unitType == 0) return food >= FOOD_COST_SWORDSMAN && wood >= WOOD_COST_SWORDSMAN && iron >= IRON_COST_SWORDSMAN;
        if (unitType == 1) return food >= FOOD_COST_SPEARMAN && wood >= WOOD_COST_SPEARMAN && iron >= IRON_COST_SPEARMAN;
        if (unitType == 2) return wood >= WOOD_COST_CROSSBOW && iron >= IRON_COST_CROSSBOW;
        if (unitType == 3) return wood >= WOOD_COST_CANNON && iron >= IRON_COST_CANNON;
        return false;
    }

    // -------------------------
    // VIEW THREAT
    // -------------------------

    private static void viewThreat() {
        if (enemyArmy == null) {
            System.out.println("No enemy threat.");
            return;
        }
        System.out.println("\nNEW THREAT COMING");
        String[] names = {"Swordsman", "Spearman", "Crossbow", "Cannon"};
        int i = 0;
        while (i < 4) {
            if (enemyArmy[i].size() > 0) {
                System.out.println("  " + names[i] + ": " + enemyArmy[i].size());
            }
            i++;
        }
    }

    // -------------------------
    // START BATTLE
    // -------------------------

    private static void startBattle() {
        enemyArrived = false;
        System.out.println("\n*** BATTLE BEGINS! ***");

        int battleNum = civilization.getBattles() + 1;
        Battle battle = new Battle(civilization.getArmy(), enemyArmy, battleNum);
        battle.runBattle();

        civilization.setBattles(battleNum);

        // Store reports (keep last 5)
        battleReports.add(battle.getBattleReport());
        battleDevelopments.add(battle.getBattleDevelopment());
        if (battleReports.size() > 5) {
            battleReports.remove(0);
            battleDevelopments.remove(0);
        }

        System.out.println(battle.getBattleReport());

        // If civilization won, collect waste
        if (battle.isCivilizationWon()) {
            int woodCollected = battle.getWasteWoodIron()[0];
            int ironCollected = battle.getWasteWoodIron()[1];
            civilization.setWood(civilization.getWood() + woodCollected);
            civilization.setIron(civilization.getIron() + ironCollected);
            System.out.println("Resources collected from rubble: Wood=" + woodCollected + " Iron=" + ironCollected);
        }

        // Update civilization army: remove dead units
        ArrayList<MilitaryUnit>[] survivors = battle.getSurvivingCivilizationGroups();
        int i = 0;
        while (i < 9) {
            civilization.getArmy()[i].clear();
            int j = 0;
            while (j < survivors[i].size()) {
                civilization.getArmy()[i].add(survivors[i].get(j));
                j++;
            }
            i++;
        }

        // Add experience to survivors
        civilization.addExperienceToSurvivors();

        // Check if priests are all dead -> desanctify
        if (civilization.getArmy()[8].isEmpty()) {
            civilization.desanctifyArmy();
            System.out.println("All priests have died. Army is no longer sanctified.");
        }

        System.out.println("Battle over. Enemy cleared.");
        enemyArmy = null;
    }
}
