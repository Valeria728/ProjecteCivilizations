package DAO;

import java.sql.*;
import java.util.ArrayList;

import game.Battle;

// DAO para guardar y recuperar resultados de batallas.

public class BattleDAO {

    private Connection con;

    public BattleDAO() {
        this.con = DatabaseConnection.getConnection();
    }

    // Guarda el resultado completo de una batalla
    public void saveBattle(int civId, int numBattle, Battle battle) {
        saveBattleStats(civId, numBattle, battle);
        saveCivilizationUnitsStats(civId, numBattle, battle);
        saveEnemyUnitsStats(civId, numBattle, battle);
        saveBattleLog(civId, numBattle, battle.getBattleDevelopment());
    }

    // Guarda stats globales de la batalla (madera/hierro obtenidos, ganador)
    private void saveBattleStats(int civId, int numBattle, Battle battle) {
        String sql = "INSERT INTO battle_stats (civilization_id, num_battle, wood_acquired, iron_acquired, winner) " +
                     "VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, civId);
            ps.setInt(2, numBattle);
            ps.setInt(3, battle.getWasteWood());
            ps.setInt(4, battle.getWasteIron());
            ps.setString(5, battle.getWinner());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error guardando battle_stats: " + e.getMessage());
        }
    }

    // Guarda unidades de nuestra civilización por tipo (ataque, defensa, especial)
    private void saveCivilizationUnitsStats(int civId, int numBattle, Battle battle) {
        String[] attackTypes  = {"Swordsman", "Spearman", "Crossbow", "Cannon"};
        String[] defenseTypes = {"ArrowTower", "Catapult", "RocketLauncherTower"};
        String[] specialTypes = {"Magician", "Priest"};

        int[][] initialArmies = battle.getInitialArmies();
        int[][] drops = battle.getDrops(); // drops[0] = nuestra civ, drops[1] = enemigo

        // Ataque (posiciones 0-3)
        for (int i = 0; i <= 3; i++) {
            saveUnitStatRow(civId, numBattle, attackTypes[i],
                    initialArmies[0][i], drops[0][i],
                    "civilization_attack_stats");
        }

        // Defensa (posiciones 4-6)
        for (int i = 4; i <= 6; i++) {
            saveUnitStatRow(civId, numBattle, defenseTypes[i - 4],
                    initialArmies[0][i], drops[0][i],
                    "civilization_defense_stats");
        }

        // Especiales (posiciones 7-8)
        for (int i = 7; i <= 8; i++) {
            saveUnitStatRow(civId, numBattle, specialTypes[i - 7],
                    initialArmies[0][i], drops[0][i],
                    "civilization_special_stats");
        }
    }

    // Guarda unidades enemigas
    private void saveEnemyUnitsStats(int civId, int numBattle, Battle battle) {
        String[] enemyTypes = {"Swordsman", "Spearman", "Crossbow", "Cannon"};
        int[][] initialArmies = battle.getInitialArmies();
        int[][] drops = battle.getDrops();

        for (int i = 0; i <= 3; i++) {
            saveUnitStatRow(civId, numBattle, enemyTypes[i],
                    initialArmies[1][i], drops[1][i],
                    "enemy_attack_stats");
        }
    }

    private void saveUnitStatRow(int civId, int numBattle, String type, int initial, int unitDrops, String tableName) {
        String sql = "INSERT INTO " + tableName + " (civilization_id, num_battle, type, initial_count, drops) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, civId);
            ps.setInt(2, numBattle);
            ps.setString(3, type);
            ps.setInt(4, initial);
            ps.setInt(5, unitDrops);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error guardando unidad en " + tableName + ": " + e.getMessage());
        }
    }

    // Guarda el log de batalla línea a línea
    private void saveBattleLog(int civId, int numBattle, String development) {
        String[] lines = development.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String sql = "INSERT INTO battle_log (civilization_id, num_battle, num_line, log_entry) VALUES (?,?,?,?)";
            try {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, civId);
                ps.setInt(2, numBattle);
                ps.setInt(3, i + 1);
                ps.setString(4, lines[i]);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                System.out.println("Error guardando log línea " + i + ": " + e.getMessage());
            }
        }
    }

    // Carga el log de una batalla como String
    public String loadBattleLog(int civId, int numBattle) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT log_entry FROM battle_log WHERE civilization_id=? AND num_battle=? ORDER BY num_line";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, civId);
            ps.setInt(2, numBattle);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sb.append(rs.getString("log_entry")).append("\n");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error cargando log: " + e.getMessage());
        }
        return sb.toString();
    }

    // Devuelve el número total de batallas guardadas para una civilización
    public int getBattleCount(int civId) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM battle_stats WHERE civilization_id=?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, civId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error contando batallas: " + e.getMessage());
        }
        return count;
    }
}
