package game;

import java.util.ArrayList;
import java.util.Random;

import interfaces.MilitaryUnit;
import interfaces.Variables;

import java.util.ArrayList;
import java.util.Random;

import interfaces.MilitaryUnit;
import interfaces.Variables;

public class Battle implements Variables {

  private ArrayList<MilitaryUnit> civilizationArmy;
  private ArrayList<MilitaryUnit> enemyArmy;

  // armies[0] = Civilización (9 grupos), armies[1] = Enemigo (4 grupos)
  private ArrayList<MilitaryUnit>[][] armies;

  private String battleDevelopment;

  // initialCostFleet[0] = {comida, madera, hierro} de la civilización
  // initialCostFleet[1] = {comida, madera, hierro} del enemigo
  private int[][] initialCostFleet;

  private int initialNumberUnitsCivilization;
  private int initialNumberUnitsEnemy;

  // wasteWoodIron[0] = madera, wasteWoodIron[1] = hierro
  private int[] wasteWoodIron;

  private int enemyDrops;
  private int civilizationDrops;

  // resourcesLooses[0] = {comida, madera, hierro, coste_ponderado} civilización
  // resourcesLooses[1] = {comida, madera, hierro, coste_ponderado} enemigo
  private int[][] resourcesLooses;

  // Almacenamiento inicial y actual de unidades por tipo de grupo
  private int[][] initialArmies;
  private int[] actualNumberUnitsCivilization;
  private int[] actualNumberUnitsEnemy;

  private int battleNumber;
  private boolean civilizationWon;
  private Random random;

  // Nombres estandarizados para los informes
  private String[] civUnitNames = {
      "Swordsman", "Spearman", "Crossbow", "Cannon",
      "Arrow Tower", "Catapult", "Rocket Launcher Tower",
      "Magician", "Priest"
  };
  private String[] enemyUnitNames = {"Swordsman", "Spearman", "Crossbow", "Cannon"};

  @SuppressWarnings("unchecked")
  public Battle(ArrayList<MilitaryUnit>[] civArmyGroups, ArrayList<MilitaryUnit>[] enemyArmyGroups, int battleNumber) {
      this.battleNumber = battleNumber;
      this.random = new Random();
      this.battleDevelopment = "";
      this.wasteWoodIron = new int[]{0, 0};
      this.civilizationDrops = 0;
      this.enemyDrops = 0;

      this.civilizationArmy = new ArrayList<>();
      this.enemyArmy = new ArrayList<>();

      // Instanciación de la matriz de ejércitos por grupos
      this.armies = new ArrayList[2][9];
      for (int i = 0; i < 9; i++) {
          this.armies[0][i] = new ArrayList<>();
      }
      for (int i = 0; i < 4; i++) {
          this.armies[1][i] = new ArrayList<>();
      }

      // Clonado y reinicio de armadura de la Civilización
      for (int i = 0; i < 9; i++) {
          for (int j = 0; j < civArmyGroups[i].size(); j++) {
              MilitaryUnit unit = civArmyGroups[i].get(j);
              unit.resetArmor();
              this.armies[0][i].add(unit);
              this.civilizationArmy.add(unit);
          }
      }

      // Clonado y reinicio de armadura del Enemigo
      for (int i = 0; i < 4; i++) {
          for (int j = 0; j < enemyArmyGroups[i].size(); j++) {
              MilitaryUnit unit = enemyArmyGroups[i].get(j);
              unit.resetArmor();
              this.armies[1][i].add(unit);
              this.enemyArmy.add(unit);
          }
      }

      this.initialArmies = new int[2][9];
      this.actualNumberUnitsCivilization = new int[9];
      this.actualNumberUnitsEnemy = new int[4];

      initInitialArmies();

      this.initialNumberUnitsCivilization = this.civilizationArmy.size();
      this.initialNumberUnitsEnemy = this.enemyArmy.size();

      this.initialCostFleet = new int[2][3];
      fleetResourceCost();

      this.resourcesLooses = new int[2][4];
  }

  // ------------------------------------------------------------------------
  // MÉTODOS DE INICIALIZACIÓN Y CONTROL
  // ------------------------------------------------------------------------

  private void initInitialArmies() {
      for (int i = 0; i < 9; i++) {
          this.initialArmies[0][i] = this.armies[0][i].size();
      }
      for (int i = 0; i < 4; i++) {
          this.initialArmies[1][i] = this.armies[1][i].size();
      }
      updateActualNumbers();
  }

  private void updateActualNumbers() {
      for (int i = 0; i < 9; i++) {
          this.actualNumberUnitsCivilization[i] = this.armies[0][i].size();
      }
      for (int i = 0; i < 4; i++) {
          this.actualNumberUnitsEnemy[i] = this.armies[1][i].size();
      }
  }

  private void fleetResourceCost() {
      // Cálculo del coste inicial total de la Civilización
      int foodCiv = 0, woodCiv = 0, ironCiv = 0;
      for (int i = 0; i < 9; i++) {
          if (!this.armies[0][i].isEmpty()) {
              MilitaryUnit sample = this.armies[0][i].get(0);
              foodCiv += this.initialArmies[0][i] * sample.getFoodCost();
              woodCiv += this.initialArmies[0][i] * sample.getWoodCost();
              ironCiv += this.initialArmies[0][i] * sample.getIronCost();
          }
      }
      this.initialCostFleet[0][0] = foodCiv;
      this.initialCostFleet[0][1] = woodCiv;
      this.initialCostFleet[0][2] = ironCiv;

      // Cálculo del coste inicial total del Enemigo
      int foodEn = 0, woodEn = 0, ironEn = 0;
      for (int i = 0; i < 4; i++) {
          if (!this.armies[1][i].isEmpty()) {
              MilitaryUnit sample = this.armies[1][i].get(0);
              foodEn += this.initialArmies[1][i] * sample.getFoodCost();
              woodEn += this.initialArmies[1][i] * sample.getWoodCost();
              ironEn += this.initialArmies[1][i] * sample.getIronCost();
          }
      }
      this.initialCostFleet[1][0] = foodEn;
      this.initialCostFleet[1][1] = woodEn;
      this.initialCostFleet[1][2] = ironEn;
  }

  // ------------------------------------------------------------------------
  // ALGORITMOS DE SELECCIÓN DE GRUPOS
  // ------------------------------------------------------------------------

  private int getCivilizationGroupAttacker() {
      int total = 0;
      for (int i = 0; i < CHANCE_ATTACK_CIVILIZATION_UNITS.length; i++) {
          if (!this.armies[0][i].isEmpty()) {
              total += CHANCE_ATTACK_CIVILIZATION_UNITS[i];
          }
      }
      if (total == 0) return -1;
      
      int rnd = this.random.nextInt(total) + 1;
      int accumulated = 0;
      for (int i = 0; i < CHANCE_ATTACK_CIVILIZATION_UNITS.length; i++) {
          if (!this.armies[0][i].isEmpty()) {
              accumulated += CHANCE_ATTACK_CIVILIZATION_UNITS[i];
              if (accumulated >= rnd) {
                  return i;
              }
          }
      }
      return -1;
  }

  private int getEnemyGroupAttacker() {
      int total = 0;
      for (int i = 0; i < CHANCE_ATTACK_ENEMY_UNITS.length; i++) {
          if (!this.armies[1][i].isEmpty()) {
              total += CHANCE_ATTACK_ENEMY_UNITS[i];
          }
      }
      if (total == 0) return -1;

      int rnd = this.random.nextInt(total) + 1;
      int accumulated = 0;
      for (int i = 0; i < CHANCE_ATTACK_ENEMY_UNITS.length; i++) {
          if (!this.armies[1][i].isEmpty()) {
              accumulated += CHANCE_ATTACK_ENEMY_UNITS[i];
              if (accumulated >= rnd) {
                  return i;
              }
          }
      }
      return -1;
  }

  private int getGroupDefender(boolean isCivilization) {
      int groups = isCivilization ? 9 : 4;
      ArrayList<MilitaryUnit>[] targetArmy = isCivilization ? this.armies[0] : this.armies[1];

      int total = 0;
      for (int i = 0; i < groups; i++) {
          total += targetArmy[i].size();
      }
      if (total == 0) return -1;

      int rnd = this.random.nextInt(total) + 1;
      int accumulated = 0;
      for (int i = 0; i < groups; i++) {
          accumulated += targetArmy[i].size();
          if (accumulated >= rnd) {
              return i;
          }
      }
      return -1;
  }

  // ------------------------------------------------------------------------
  // COMPROBACIONES DE PORCENTAJES DE LÍMITE DE BATALLA
  // ------------------------------------------------------------------------

  private int remainderPercentageCivilization() {
      if (this.initialNumberUnitsCivilization == 0) return 0;
      return (this.civilizationArmy.size() * 100) / this.initialNumberUnitsCivilization;
  }

  private int remainderPercentageEnemy() {
      if (this.initialNumberUnitsEnemy == 0) return 0;
      return (this.enemyArmy.size() * 100) / this.initialNumberUnitsEnemy;
  }

  // ------------------------------------------------------------------------
  // GENERACIÓN DE ESCOMBROS (WASTE)
  // ------------------------------------------------------------------------

  private void tryGenerateWaste(MilitaryUnit unit) {
      int chance = unit.getChanceGeneratingWaste();
      if (chance == 0) return;
      
      int rnd = this.random.nextInt(100) + 1;
      if (rnd <= chance) {
          int woodWaste = (unit.getWoodCost() * PERCENTATGE_WASTE) / 100;
          int ironWaste = (unit.getIronCost() * PERCENTATGE_WASTE) / 100;
          this.wasteWoodIron[0] += woodWaste;
          this.wasteWoodIron[1] += ironWaste;
      }
  }

  // ------------------------------------------------------------------------
  // DESARROLLO DEL ATAQUE
  // ------------------------------------------------------------------------

  private void performAttack(boolean attackerIsCivilization) {
      int attackerGroup;
      ArrayList<MilitaryUnit>[] attackingArmy;
      ArrayList<MilitaryUnit>[] defendingArmy;
      int defenderGroups;
      String attackerSide;
      String defenderSide;

      if (attackerIsCivilization) {
          attackerGroup = getCivilizationGroupAttacker();
          attackingArmy = this.armies[0];
          defendingArmy = this.armies[1];
          defenderGroups = 4;
          attackerSide = "Civilization";
          defenderSide = "Enemy";
      } else {
          attackerGroup = getEnemyGroupAttacker();
          attackingArmy = this.armies[1];
          defendingArmy = this.armies[0];
          defenderGroups = 9;
          attackerSide = "Enemy";
          defenderSide = "Civilization";
      }

      if (attackerGroup == -1 || attackingArmy[attackerGroup].isEmpty()) return;

      int attackerIndex = this.random.nextInt(attackingArmy[attackerGroup].size());
      MilitaryUnit attacker = attackingArmy[attackerGroup].get(attackerIndex);

      boolean keepAttacking = true;
      while (keepAttacking) {
          int defenderGroup = getGroupDefender(!attackerIsCivilization);
          if (defenderGroup == -1 || defendingArmy[defenderGroup].isEmpty()) break;

          int defenderIndex = this.random.nextInt(defendingArmy[defenderGroup].size());
          MilitaryUnit defender = defendingArmy[defenderGroup].get(defenderIndex);

          int damage = attacker.attack();
          defender.takeDamage(damage);

          String attackerName = attacker.toString();
          String defenderName = defender.toString();

          this.battleDevelopment += "Attacks " + attackerSide + ": " + attackerName + " attacks " + defenderName + "\n"
                  + attackerName + " generates damage = " + damage + "\n"
                  + defenderName + " stays with armor = " + defender.getActualArmor() + "\n";

          if (defender.getActualArmor() <= 0) {
              this.battleDevelopment += "We eliminate " + defenderName + "\n";
              tryGenerateWaste(defender);

              defendingArmy[defenderGroup].remove(defenderIndex);
              if (attackerIsCivilization) {
                  this.enemyArmy.remove(defender);
                  this.enemyDrops++;
              } else {
                  this.civilizationArmy.remove(defender);
                  this.civilizationDrops++;
              }
              updateActualNumbers();
          }

          // Comprobación de ataque extra recurrente
          int chanceAgain = attacker.getChanceAttackAgain();
          int rnd = this.random.nextInt(100) + 1;
          if (rnd <= chanceAgain) {
              int totalDefenders = 0;
              for (int k = 0; k < defenderGroups; k++) {
                  totalDefenders += defendingArmy[k].size();
              }
              keepAttacking = totalDefenders > 0;
          } else {
              keepAttacking = false;
          }
      }
  }

  // ------------------------------------------------------------------------
  // EJECUCIÓN DEL COMBATE PRINCIPAL
  // ------------------------------------------------------------------------

  public void runBattle() {
      boolean civilizationAttacks = this.random.nextBoolean();

      this.battleDevelopment += "********************BATTLE START********************\n";

      while (remainderPercentageCivilization() > BATTLE_END_PERCENTAGE
              && remainderPercentageEnemy() > BATTLE_END_PERCENTAGE) {

          this.battleDevelopment += "********************CHANGE ATTACKER********************\n";
          performAttack(civilizationAttacks);
          civilizationAttacks = !civilizationAttacks;
      }

      updateResourcesLooses();

      // El bando con menores pérdidas ponderadas totales gana la batalla
      int civLossWeighted = this.resourcesLooses[0][3];
      int enemyLossWeighted = this.resourcesLooses[1][3];
      this.civilizationWon = civLossWeighted <= enemyLossWeighted;

      this.battleDevelopment += "********************BATTLE END********************\n";
  }

  // ------------------------------------------------------------------------
  // CÁLCULO DE PÉRDIDAS (RESOURCES LOSSES)
  // ------------------------------------------------------------------------

  private void updateResourcesLooses() {
      // Pérdidas de la Civilización
      int civFoodLoss = 0, civWoodLoss = 0, civIronLoss = 0;
      for (int i = 0; i < 9; i++) {
          int droppedInGroup = this.initialArmies[0][i] - this.actualNumberUnitsCivilization[i];
          if (droppedInGroup > 0) {
              // Buscamos un espécimen vivo o usamos un cálculo preventivo de costes fijos
              MilitaryUnit sample = !this.armies[0][i].isEmpty() ? this.armies[0][i].get(0) : null;
              if (sample != null) {
                  civFoodLoss += droppedInGroup * sample.getFoodCost();
                  civWoodLoss += droppedInGroup * sample.getWoodCost();
                  civIronLoss += droppedInGroup * sample.getIronCost();
              } else {
                  // Si todo el grupo fue aniquilado, inferimos el coste proporcional desde el coste total original
                  civFoodLoss += (this.initialCostFleet[0][0] / this.initialNumberUnitsCivilization) * droppedInGroup;
                  civWoodLoss += (this.initialCostFleet[0][1] / this.initialNumberUnitsCivilization) * droppedInGroup;
                  civIronLoss += (this.initialCostFleet[0][2] / this.initialNumberUnitsCivilization) * droppedInGroup;
              }
          }
      }
      this.resourcesLooses[0][0] = civFoodLoss;
      this.resourcesLooses[0][1] = civWoodLoss;
      this.resourcesLooses[0][2] = civIronLoss;
      // Coste ponderado oficial: Hierro + Madera/5 + Comida/10
      this.resourcesLooses[0][3] = civIronLoss + (civWoodLoss / 5) + (civFoodLoss / 10);

      // Pérdidas del Enemigo
      int enFoodLoss = 0, enWoodLoss = 0, enIronLoss = 0;
      for (int i = 0; i < 4; i++) {
          int droppedInGroup = this.initialArmies[1][i] - this.actualNumberUnitsEnemy[i];
          if (droppedInGroup > 0) {
              MilitaryUnit sample = !this.armies[1][i].isEmpty() ? this.armies[1][i].get(0) : null;
              if (sample != null) {
                  enFoodLoss += droppedInGroup * sample.getFoodCost();
                  enWoodLoss += droppedInGroup * sample.getWoodCost();
                  enIronLoss += droppedInGroup * sample.getIronCost();
              } else {
                  enFoodLoss += (this.initialCostFleet[1][0] / this.initialNumberUnitsEnemy) * droppedInGroup;
                  enWoodLoss += (this.initialCostFleet[1][1] / this.initialNumberUnitsEnemy) * droppedInGroup;
                  enIronLoss += (this.initialCostFleet[1][2] / this.initialNumberUnitsEnemy) * droppedInGroup;
              }
          }
      }
      this.resourcesLooses[1][0] = enFoodLoss;
      this.resourcesLooses[1][1] = enWoodLoss;
      this.resourcesLooses[1][2] = enIronLoss;
      this.resourcesLooses[1][3] = enIronLoss + (enWoodLoss / 5) + (enFoodLoss / 10);
  }

  // ------------------------------------------------------------------------
  // GETTERS Y MÉTODOS DE COMPATIBILIDAD INTERFAZ/DAO/GUI
  // ------------------------------------------------------------------------

  public boolean isCivilizationWon() { return this.civilizationWon; }
  public int[] getWasteWoodIron() { return this.wasteWoodIron; }
  public int getBattleNumber() { return this.battleNumber; }
  public String getBattleDevelopment() { return this.battleDevelopment; }

  public ArrayList<MilitaryUnit>[] getSurvivingCivilizationGroups() {
      return this.armies[0];
  }

  public int getWasteWood() { 
      return this.wasteWoodIron[0]; 
  }

  public int getWasteIron() { 
      return this.wasteWoodIron[1]; 
  }

  public String getWinner() { 
      return this.civilizationWon ? "Civilization" : "Enemy"; 
  }

  public int[][] getInitialArmies() { 
      return this.initialArmies; 
  }

  public int[][] getDrops() { 
      int[][] drops = new int[2][9];
      for (int i = 0; i < 9; i++) {
          drops[0][i] = this.initialArmies[0][i] - this.actualNumberUnitsCivilization[i];
      }
      for (int i = 0; i < 4; i++) {
          drops[1][i] = this.initialArmies[1][i] - this.actualNumberUnitsEnemy[i];
      }
      return drops; 
  }

  public void startBattle() {
      runBattle();
  }

  public String getBattleReport(int num) {
      this.battleNumber = num;
      return getBattleReport();
  }

  public String getBattleReport() {
      StringBuilder report = new StringBuilder();
      report.append("\nBATTLE NUMBER: ").append(this.battleNumber).append("\n");
      report.append("BATTLE STATISTICS\n");
      report.append(String.format("%-30s %-10s %-10s %-30s %-10s %-10s%n",
          "Army Civilization Units", "Initial", "Drops",
          "Army Enemy Units", "Initial", "Drops"));

      int maxRows = 9;
      for (int i = 0; i < maxRows; i++) {
          String civLine = "";
          String enemyLine = "";

          if (i < 9) {
              civLine = this.civUnitNames[i] + ": " + this.initialArmies[0][i] + " initial / " + (this.initialArmies[0][i] - this.actualNumberUnitsCivilization[i]) + " drops";
          }
          if (i < 4) {
              enemyLine = this.enemyUnitNames[i] + ": " + this.initialArmies[1][i] + " initial / " + (this.initialArmies[1][i] - this.actualNumberUnitsEnemy[i]) + " drops";
          }
          report.append(String.format("  %-45s %-45s%n", civLine, enemyLine));
      }

      report.append("**************************************************************************************\n");
      report.append(String.format("  %-35s %-35s%n", "Cost Army Civilization", "Cost Army Enemy"));
      report.append(String.format("  Food:  %-28s Food:  %-28s%n", this.initialCostFleet[0][0], this.initialCostFleet[1][0]));
      report.append(String.format("  Wood:  %-28s Wood:  %-28s%n", this.initialCostFleet[0][1], this.initialCostFleet[1][1]));
      report.append(String.format("  Iron:  %-28s Iron:  %-28s%n", this.initialCostFleet[0][2], this.initialCostFleet[1][2]));
      report.append("**************************************************************************************\n");
      report.append(String.format("  %-35s %-35s%n", "Losses Army Civilization", "Losses Army Enemy"));
      report.append(String.format("  Food:  %-28s Food:  %-28s%n", this.resourcesLooses[0][0], this.resourcesLooses[1][0]));
      report.append(String.format("  Wood:  %-28s Wood:  %-28s%n", this.resourcesLooses[0][1], this.resourcesLooses[1][1]));
      report.append(String.format("  Iron:  %-28s Iron:  %-28s%n", this.resourcesLooses[0][2], this.resourcesLooses[1][2]));
      report.append("**************************************************************************************\n");
      report.append("Waste Generated:\n");
      report.append("  Wood: ").append(this.wasteWoodIron[0]).append("\n");
      report.append("  Iron: ").append(this.wasteWoodIron[1]).append("\n");

      if (this.civilizationWon) {
          report.append("Battle Won by Civilization, We Collect Rubble\n");
      } else {
          report.append("Battle Won by Enemy\n");
      }
      report.append("##########################################################################\n");
      return report.toString();
  }
}