package game;

import java.util.ArrayList;
import java.util.Random;

import interfaces.MilitaryUnit;
import interfaces.Variables;

public class Battle implements Variables {

    private ArrayList<MilitaryUnit> civilizationArmy;
    private ArrayList<MilitaryUnit> enemyArmy;

    // armies[0] = civilization, armies[1] = enemy
    // Each row has 9 groups (civilization) or 4 groups (enemy uses 0-3)
    private ArrayList<MilitaryUnit>[][] armies;

    private String battleDevelopment;

    // initialCostFleet[0] = {food, wood, iron} civilization
    // initialCostFleet[1] = {food, wood, iron} enemy
    private int[][] initialCostFleet;

    private int initialNumberUnitsCivilization;
    private int initialNumberUnitsEnemy;

    // wasteWoodIron[0]=wood, wasteWoodIron[1]=iron
    private int[] wasteWoodIron;

    private int enemyDrops;
    private int civilizationDrops;

    // resourcesLooses[0] = {food, wood, iron, weighted} civilization
    // resourcesLooses[1] = {food, wood, iron, weighted} enemy
    private int[][] resourcesLooses;

    // initialArmies[0][0..8] = initial count per group civilization
    // initialArmies[1][0..3] = initial count per group enemy
    private int[][] initialArmies;

    private int[] actualNumberUnitsCivilization;
    private int[] actualNumberUnitsEnemy;

    private int battleNumber;
    private boolean civilizationWon;

    private Random random;

    // Unit names for reporting
    private String[] civUnitNames = {
        "Swordsman", "Spearman", "Crossbow", "Cannon",
        "Arrow Tower", "Catapult", "Rocket Launcher Tower",
        "Magician", "Priest"
    };
    private String[] enemyUnitNames = {"Swordsman", "Spearman", "Crossbow", "Cannon"};

    @SuppressWarnings("unchecked")
    public Battle(ArrayList<MilitaryUnit>[] civArmyGroups, ArrayList<MilitaryUnit>[] enemyArmyGroups, int battleNumber) {
        this.battleNumber = battleNumber;
        random = new Random();
        battleDevelopment = "";
        wasteWoodIron = new int[]{0, 0};
        civilizationDrops = 0;
        enemyDrops = 0;

        // Build flat lists from groups
        civilizationArmy = new ArrayList<MilitaryUnit>();
        enemyArmy = new ArrayList<MilitaryUnit>();

        armies = new ArrayList[2][9];
        int i = 0;
        while (i < 9) {
            armies[0][i] = new ArrayList<MilitaryUnit>();
            i++;
        }
        i = 0;
        while (i < 4) {
            armies[1][i] = new ArrayList<MilitaryUnit>();
            i++;
        }

        // Copy civilization army
        i = 0;
        while (i < 9) {
            int j = 0;
            while (j < civArmyGroups[i].size()) {
                MilitaryUnit unit = civArmyGroups[i].get(j);
                unit.resetArmor();
                armies[0][i].add(unit);
                civilizationArmy.add(unit);
                j++;
            }
            i++;
        }

        // Copy enemy army
        i = 0;
        while (i < 4) {
            int j = 0;
            while (j < enemyArmyGroups[i].size()) {
                MilitaryUnit unit = enemyArmyGroups[i].get(j);
                unit.resetArmor();
                armies[1][i].add(unit);
                enemyArmy.add(unit);
                j++;
            }
            i++;
        }

        initialArmies = new int[2][9];
        actualNumberUnitsCivilization = new int[9];
        actualNumberUnitsEnemy = new int[4];

        initInitialArmies();

        initialNumberUnitsCivilization = civilizationArmy.size();
        initialNumberUnitsEnemy = enemyArmy.size();

        initialCostFleet = new int[2][3];
        fleetResourceCost();

        resourcesLooses = new int[2][4];
    }

    // -------------------------
    // INIT HELPERS
    // -------------------------

    private void initInitialArmies() {
        int i = 0;
        while (i < 9) {
            initialArmies[0][i] = armies[0][i].size();
            i++;
        }
        i = 0;
        while (i < 4) {
            initialArmies[1][i] = armies[1][i].size();
            i++;
        }
        updateActualNumbers();
    }

    private void updateActualNumbers() {
        int i = 0;
        while (i < 9) {
            actualNumberUnitsCivilization[i] = armies[0][i].size();
            i++;
        }
        i = 0;
        while (i < 4) {
            actualNumberUnitsEnemy[i] = armies[1][i].size();
            i++;
        }
    }

    private void fleetResourceCost() {
        // Civilization
        int food = 0, wood = 0, iron = 0;
        int i = 0;
        while (i < 9) {
            int j = 0;
            while (j < initialArmies[0][i]) {
                // Use a representative unit cost (all same type, same cost)
                if (armies[0][i].size() > 0) {
                    MilitaryUnit u = armies[0][i].get(0);
                    food = food + u.getFoodCost();
                    wood = wood + u.getWoodCost();
                    iron = iron + u.getIronCost();
                }
                j++;
            }
            i++;
        }
        initialCostFleet[0][0] = food;
        initialCostFleet[0][1] = wood;
        initialCostFleet[0][2] = iron;

        // Enemy
        food = 0; wood = 0; iron = 0;
        i = 0;
        while (i < 4) {
            int j = 0;
            while (j < initialArmies[1][i]) {
                if (armies[1][i].size() > 0) {
                    MilitaryUnit u = armies[1][i].get(0);
                    food = food + u.getFoodCost();
                    wood = wood + u.getWoodCost();
                    iron = iron + u.getIronCost();
                }
                j++;
            }
            i++;
        }
        initialCostFleet[1][0] = food;
        initialCostFleet[1][1] = wood;
        initialCostFleet[1][2] = iron;
    }

    // -------------------------
    // GROUP SELECTION ALGORITHM
    // -------------------------

    // Select attacker group by probability array
    private int getCivilizationGroupAttacker() {
        int total = 0;
        int i = 0;
        while (i < CHANCE_ATTACK_CIVILIZATION_UNITS.length) {
            if (armies[0][i].size() > 0) {
                total = total + CHANCE_ATTACK_CIVILIZATION_UNITS[i];
            }
            i++;
        }
        if (total == 0) return -1;
        int rnd = random.nextInt(total) + 1;
        int accumulated = 0;
        i = 0;
        while (i < CHANCE_ATTACK_CIVILIZATION_UNITS.length) {
            if (armies[0][i].size() > 0) {
                accumulated = accumulated + CHANCE_ATTACK_CIVILIZATION_UNITS[i];
                if (accumulated >= rnd) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    private int getEnemyGroupAttacker() {
        int total = 0;
        int i = 0;
        while (i < CHANCE_ATTACK_ENEMY_UNITS.length) {
            if (armies[1][i].size() > 0) {
                total = total + CHANCE_ATTACK_ENEMY_UNITS[i];
            }
            i++;
        }
        if (total == 0) return -1;
        int rnd = random.nextInt(total) + 1;
        int accumulated = 0;
        i = 0;
        while (i < CHANCE_ATTACK_ENEMY_UNITS.length) {
            if (armies[1][i].size() > 0) {
                accumulated = accumulated + CHANCE_ATTACK_ENEMY_UNITS[i];
                if (accumulated >= rnd) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    // Select defender group based on unit count proportionality
    private int getGroupDefender(boolean isCivilization) {
        int groups = isCivilization ? 9 : 4;
        ArrayList<MilitaryUnit>[] targetArmy = isCivilization ? armies[0] : armies[1];

        int total = 0;
        int i = 0;
        while (i < groups) {
            total = total + targetArmy[i].size();
            i++;
        }
        if (total == 0) return -1;

        int rnd = random.nextInt(total) + 1;
        int accumulated = 0;
        i = 0;
        while (i < groups) {
            accumulated = accumulated + targetArmy[i].size();
            if (accumulated >= rnd) {
                return i;
            }
            i++;
        }
        return -1;
    }

    // -------------------------
    // BATTLE PERCENTAGE
    // -------------------------

    private int remainderPercentageCivilization() {
        if (initialNumberUnitsCivilization == 0) return 0;
        return (civilizationArmy.size() * 100) / initialNumberUnitsCivilization;
    }

    private int remainderPercentageEnemy() {
        if (initialNumberUnitsEnemy == 0) return 0;
        return (enemyArmy.size() * 100) / initialNumberUnitsEnemy;
    }

    // -------------------------
    // WASTE GENERATION
    // -------------------------

    private void tryGenerateWaste(MilitaryUnit unit) {
        int chance = unit.getChanceGeneratingWaste();
        if (chance == 0) return;
        int rnd = random.nextInt(100) + 1;
        if (rnd <= chance) {
            int woodWaste = unit.getWoodCost() * PERCENTATGE_WASTE / 100;
            int ironWaste = unit.getIronCost() * PERCENTATGE_WASTE / 100;
            wasteWoodIron[0] = wasteWoodIron[0] + woodWaste;
            wasteWoodIron[1] = wasteWoodIron[1] + ironWaste;
        }
    }

    // -------------------------
    // PERFORM ONE ATTACK
    // -------------------------

    // attackerIsCivilization: true = civilization attacks enemy
    private void performAttack(boolean attackerIsCivilization) {
        int attackerGroup;
        ArrayList<MilitaryUnit>[] attackingArmy;
        ArrayList<MilitaryUnit>[] defendingArmy;
        int defenderGroups;
        String attackerSide;
        String defenderSide;

        if (attackerIsCivilization) {
            attackerGroup = getCivilizationGroupAttacker();
            attackingArmy = armies[0];
            defendingArmy = armies[1];
            defenderGroups = 4;
            attackerSide = "Civilization";
            defenderSide = "Enemy";
        } else {
            attackerGroup = getEnemyGroupAttacker();
            attackingArmy = armies[1];
            defendingArmy = armies[0];
            defenderGroups = 9;
            attackerSide = "Enemy";
            defenderSide = "Civilization";
        }

        if (attackerGroup == -1 || attackingArmy[attackerGroup].isEmpty()) return;

        // Pick random attacker unit from the group
        int attackerIndex = random.nextInt(attackingArmy[attackerGroup].size());
        MilitaryUnit attacker = attackingArmy[attackerGroup].get(attackerIndex);

        boolean keepAttacking = true;
        while (keepAttacking) {
            // Pick defender group
            int defenderGroup = getGroupDefender(!attackerIsCivilization);
            if (defenderGroup == -1 || defendingArmy[defenderGroup].isEmpty()) break;

            // Pick random defender unit
            int defenderIndex = random.nextInt(defendingArmy[defenderGroup].size());
            MilitaryUnit defender = defendingArmy[defenderGroup].get(defenderIndex);

            int damage = attacker.attack();
            defender.takeDamage(damage);

            String attackerName = attacker.toString();
            String defenderName = defender.toString();

            battleDevelopment = battleDevelopment
                + "Attacks " + attackerSide + ": " + attackerName + " attacks " + defenderName + "\n"
                + attackerName + " generates damage = " + damage + "\n"
                + defenderName + " stays with armor = " + defender.getActualArmor() + "\n";

            // Check if defender is dead
            if (defender.getActualArmor() <= 0) {
                battleDevelopment = battleDevelopment + "We eliminate " + defenderName + "\n";
                tryGenerateWaste(defender);

                // Remove from group
                defendingArmy[defenderGroup].remove(defenderIndex);
                // Remove from flat list
                if (attackerIsCivilization) {
                    enemyArmy.remove(defender);
                    enemyDrops++;
                } else {
                    civilizationArmy.remove(defender);
                    civilizationDrops++;
                }
                updateActualNumbers();
            }

            // Check if attacker can attack again
            int chanceAgain = attacker.getChanceAttackAgain();
            int rnd = random.nextInt(100) + 1;
            if (rnd <= chanceAgain) {
                // Check if defending army still has units
                int totalDefenders = 0;
                int k = 0;
                while (k < defenderGroups) {
                    totalDefenders = totalDefenders + defendingArmy[k].size();
                    k++;
                }
                if (totalDefenders > 0) {
                    keepAttacking = true;
                } else {
                    keepAttacking = false;
                }
            } else {
                keepAttacking = false;
            }
        }
    }

    // -------------------------
    // RUN BATTLE
    // -------------------------

    public void runBattle() {
        // Decide who starts randomly
        boolean civilizationAttacks = random.nextBoolean();

        battleDevelopment = battleDevelopment + "********************BATTLE START********************\n";

        while (remainderPercentageCivilization() > BATTLE_END_PERCENTAGE
            && remainderPercentageEnemy() > BATTLE_END_PERCENTAGE) {

            battleDevelopment = battleDevelopment + "********************CHANGE ATTACKER********************\n";

            performAttack(civilizationAttacks);

            // Swap attacker
            civilizationAttacks = !civilizationAttacks;
        }

        // Calculate losses and winner
        updateResourcesLooses();

        // Determine winner
        int civLoss = resourcesLooses[0][3];
        int enemyLoss = resourcesLooses[1][3];
        civilizationWon = civLoss <= enemyLoss;

        battleDevelopment = battleDevelopment + "********************BATTLE END********************\n";
    }

    // -------------------------
    // RESOURCES LOSSES
    // -------------------------

    private void updateResourcesLooses() {
        // Civilization losses
        int civFoodLoss = 0, civWoodLoss = 0, civIronLoss = 0;
        int i = 0;
        while (i < 9) {
            int droppedInGroup = initialArmies[0][i] - actualNumberUnitsCivilization[i];
            if (droppedInGroup > 0 && armies[0][i].size() > 0) {
                MilitaryUnit sample = armies[0][i].get(0);
                civFoodLoss = civFoodLoss + droppedInGroup * sample.getFoodCost();
                civWoodLoss = civWoodLoss + droppedInGroup * sample.getWoodCost();
                civIronLoss = civIronLoss + droppedInGroup * sample.getIronCost();
            }
            i++;
        }
        // Fallback: if all died, use initial cost fleet
        if (actualNumberUnitsCivilization[0] == 0 && initialArmies[0][0] > 0) {
            civFoodLoss = initialCostFleet[0][0];
            civWoodLoss = initialCostFleet[0][1];
            civIronLoss = initialCostFleet[0][2];
        }
        resourcesLooses[0][0] = civFoodLoss;
        resourcesLooses[0][1] = civWoodLoss;
        resourcesLooses[0][2] = civIronLoss;
        resourcesLooses[0][3] = civIronLoss + civWoodLoss / 5 + civFoodLoss / 10;

        // Enemy losses
        int enFoodLoss = 0, enWoodLoss = 0, enIronLoss = 0;
        i = 0;
        while (i < 4) {
            int droppedInGroup = initialArmies[1][i] - actualNumberUnitsEnemy[i];
            if (droppedInGroup > 0 && armies[1][i].size() > 0) {
                MilitaryUnit sample = armies[1][i].get(0);
                enFoodLoss = enFoodLoss + droppedInGroup * sample.getFoodCost();
                enWoodLoss = enWoodLoss + droppedInGroup * sample.getWoodCost();
                enIronLoss = enIronLoss + droppedInGroup * sample.getIronCost();
            }
            i++;
        }
        resourcesLooses[1][0] = enFoodLoss;
        resourcesLooses[1][1] = enWoodLoss;
        resourcesLooses[1][2] = enIronLoss;
        resourcesLooses[1][3] = enIronLoss + enWoodLoss / 5 + enFoodLoss / 10;
    }

    // -------------------------
    // GETTERS
    // -------------------------

    public boolean isCivilizationWon() { return civilizationWon; }
    public int[] getWasteWoodIron() { return wasteWoodIron; }
    public int getBattleNumber() { return battleNumber; }

    public String getBattleDevelopment() {
        return battleDevelopment;
    }

    public String getBattleReport() {
        String report = "\nBATTLE NUMBER: " + battleNumber + "\n";
        report = report + "BATTLE STATISTICS\n";
        report = report + String.format("%-30s %-10s %-10s %-30s %-10s %-10s%n",
            "Army Civilization Units", "Initial", "Drops",
            "Army Enemy Units", "Initial", "Drops");

        // Print both armies side by side
        int maxRows = 9;
        int i = 0;
        while (i < maxRows) {
            String civLine = "";
            String enemyLine = "";

            if (i < 9) {
                civLine = civUnitNames[i] + ": " + initialArmies[0][i] + " initial / " + (initialArmies[0][i] - actualNumberUnitsCivilization[i]) + " drops";
            }
            if (i < 4) {
                enemyLine = enemyUnitNames[i] + ": " + initialArmies[1][i] + " initial / " + (initialArmies[1][i] - actualNumberUnitsEnemy[i]) + " drops";
            }
            report = report + String.format("  %-45s %-45s%n", civLine, enemyLine);
            i++;
        }

        report = report + "**************************************************************************************\n";
        report = report + String.format("  %-35s %-35s%n", "Cost Army Civilization", "Cost Army Enemy");
        report = report + String.format("  Food:  %-28s Food:  %-28s%n", initialCostFleet[0][0], initialCostFleet[1][0]);
        report = report + String.format("  Wood:  %-28s Wood:  %-28s%n", initialCostFleet[0][1], initialCostFleet[1][1]);
        report = report + String.format("  Iron:  %-28s Iron:  %-28s%n", initialCostFleet[0][2], initialCostFleet[1][2]);
        report = report + "**************************************************************************************\n";
        report = report + String.format("  %-35s %-35s%n", "Losses Army Civilization", "Losses Army Enemy");
        report = report + String.format("  Food:  %-28s Food:  %-28s%n", resourcesLooses[0][0], resourcesLooses[1][0]);
        report = report + String.format("  Wood:  %-28s Wood:  %-28s%n", resourcesLooses[0][1], resourcesLooses[1][1]);
        report = report + String.format("  Iron:  %-28s Iron:  %-28s%n", resourcesLooses[0][2], resourcesLooses[1][2]);
        report = report + "**************************************************************************************\n";
        report = report + "Waste Generated:\n";
        report = report + "  Wood: " + wasteWoodIron[0] + "\n";
        report = report + "  Iron: " + wasteWoodIron[1] + "\n";

        if (civilizationWon) {
            report = report + "Battle Won by Civilization, We Collect Rubble\n";
        } else {
            report = report + "Battle Won by Enemy\n";
        }
        report = report + "##########################################################################\n";
        return report;
    }

    // After battle: return surviving civilization groups (for updating original army)
    public ArrayList<MilitaryUnit>[] getSurvivingCivilizationGroups() {
        return armies[0];
    }
 // --- MÉTODOS DE COMPATIBILIDAD PARA BATTLEDAO Y MAINGUI ---

    public int getWasteWood() { 
        return wasteWoodIron[0]; 
    }

    public int getWasteIron() { 
        return wasteWoodIron[1]; 
    }

    public String getWinner() { 
        return civilizationWon ? "Civilization" : "Enemy"; 
    }

    public int[][] getInitialArmies() { 
        return initialArmies; 
    }

    public int[][] getDrops() { 
        int[][] drops = new int[2][9];
        // Calculamos las bajas de la civilización
        for (int i = 0; i < 9; i++) {
            drops[0][i] = initialArmies[0][i] - actualNumberUnitsCivilization[i];
        }
        // Calculamos las bajas del enemigo
        for (int i = 0; i < 4; i++) {
            drops[1][i] = initialArmies[1][i] - actualNumberUnitsEnemy[i];
        }
        return drops; 
    }

    // Este es el "alias": cuando alguien pida startBattle, se ejecutará runBattle
    public void startBattle() {
        runBattle();
    }

    // Adaptación para que el reporte acepte el número de batalla que envía el DAO
    public String getBattleReport(int num) {
        this.battleNumber = num;
        return getBattleReport();
    }
}


