package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import game.Civilization;
import interfaces.MilitaryUnit;
import units.AttackUnit;
import units.DefenseUnit;
import units.SpecialUnit;
import units.attack.Cannon;
import units.attack.Crossbow;
import units.attack.Spearman;
import units.attack.Swordsman;
//import units.special.Magician;
//import units.special.Priest;

import java.sql.*;
import java.util.ArrayList;

//DAO (Data Access Object) para la clase Civilization.
//Se encarga de guardar y cargar el estado de la civilización en la base de datos.

public class CivilizationDAO {

    private Connection con;

    public CivilizationDAO() {
        this.con = DatabaseConnection.getConnection();
    }

    // -------------------------------------------------------
    // GUARDAR toda la civilización (civilización + ejército)
    // -------------------------------------------------------
    public void saveCivilization(Civilization civ, int civId) {
        saveCivilizationStats(civ, civId);
        deleteArmyFromDB(civId);
        saveArmy(civ, civId);
    }

    // Guarda o actualiza los stats básicos (recursos, edificios, tecnología)
    private void saveCivilizationStats(Civilization civ, int civId) {
        String sql = "UPDATE civilization_stats SET " +
                     "wood_amount=?, iron_amount=?, food_amount=?, mana_amount=?, " +
                     "magicTower_counter=?, church_counter=?, farm_counter=?, " +
                     "smithy_counter=?, carpentry_counter=?, " +
                     "technology_defense_level=?, technology_attack_level=?, battles_counter=? " +
                     "WHERE civilization_id=?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1,  civ.getWood());
            ps.setInt(2,  civ.getIron());
            ps.setInt(3,  civ.getFood());
            ps.setInt(4,  civ.getMana());
            ps.setInt(5,  civ.getMagicTower());
            ps.setInt(6,  civ.getChurch());
            ps.setInt(7,  civ.getFarm());
            ps.setInt(8,  civ.getSmithy());
            ps.setInt(9,  civ.getCarpentry());
            ps.setInt(10, civ.getTechnologyDefense());
            ps.setInt(11, civ.getTechnologyAttack());
            ps.setInt(12, civ.getBattles());
            ps.setInt(13, civId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error guardando stats: " + e.getMessage());
        }
    }

    // Borra el ejército actual en BD para luego guardarlo de nuevo
    private void deleteArmyFromDB(int civId) {
        String[] tables = {"attack_units_stats", "defense_units_stats", "special_units_stats"};
        for (int i = 0; i < tables.length; i++) {
            try {
                PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM " + tables[i] + " WHERE civilization_id=?"
                );
                ps.setInt(1, civId);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                System.out.println("Error borrando ejército de " + tables[i] + ": " + e.getMessage());
            }
        }
    }

    // Guarda cada unidad del ejército en su tabla correspondiente
    private void saveArmy(Civilization civ, int civId) {
        ArrayList<MilitaryUnit>[] army = civ.getArmy();

        // Tipos de cada posición del ejército
        String[] attackTypes  = {"Swordsman", "Spearman", "Crossbow", "Cannon"};
        String[] defenseTypes = {"ArrowTower", "Catapult", "RocketLauncherTower"};
        String[] specialTypes = {"Magician", "Priest"};

        // Posiciones 0-3: unidades de ataque
        for (int i = 0; i <= 3; i++) {
            for (int j = 0; j < army[i].size(); j++) {
                AttackUnit u = (AttackUnit) army[i].get(j);
                saveAttackUnit(civId, attackTypes[i], u.getActualArmor(), u.attack(), u.getExperience(), u.isSanctified());
            }
        }

        // Posiciones 4-6: unidades defensivas
        for (int i = 4; i <= 6; i++) {
            for (int j = 0; j < army[i].size(); j++) {
                DefenseUnit u = (DefenseUnit) army[i].get(j);
                saveDefenseUnit(civId, defenseTypes[i - 4], u.getActualArmor(), u.attack(), u.getExperience(), u.isSanctified());
            }
        }

        // Posiciones 7-8: unidades especiales
        for (int i = 7; i <= 8; i++) {
            for (int j = 0; j < army[i].size(); j++) {
                SpecialUnit u = (SpecialUnit) army[i].get(j);
                saveSpecialUnit(civId, specialTypes[i - 7], u.getActualArmor(), u.attack(), u.getExperience());
            }
        }
    }

    private void saveAttackUnit(int civId, String type, int armor, int damage, int exp, boolean sanctified) {
        String sql = "INSERT INTO attack_units_stats (civilization_id, type, armor, base_damage, experience, sanctified) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, civId);
            ps.setString(2, type);
            ps.setInt(3, armor);
            ps.setInt(4, damage);
            ps.setInt(5, exp);
            ps.setBoolean(6, sanctified);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error guardando unidad ataque: " + e.getMessage());
        }
    }

    private void saveDefenseUnit(int civId, String type, int armor, int damage, int exp, boolean sanctified) {
        String sql = "INSERT INTO defense_units_stats (civilization_id, type, armor, base_damage, experience, sanctified) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, civId);
            ps.setString(2, type);
            ps.setInt(3, armor);
            ps.setInt(4, damage);
            ps.setInt(5, exp);
            ps.setBoolean(6, sanctified);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error guardando unidad defensa: " + e.getMessage());
        }
    }

    private void saveSpecialUnit(int civId, String type, int armor, int damage, int exp) {
        String sql = "INSERT INTO special_units_stats (civilization_id, type, armor, base_damage, experience) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, civId);
            ps.setString(2, type);
            ps.setInt(3, armor);
            ps.setInt(4, damage);
            ps.setInt(5, exp);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error guardando unidad especial: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // CARGAR civilización desde BD
    // -------------------------------------------------------
    public Civilization loadCivilization(int civId) {
        Civilization civ = null;
        String sql = "SELECT * FROM civilization_stats WHERE civilization_id=?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, civId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                civ = new Civilization();
                civ.setWood(rs.getInt("wood_amount"));
                civ.setIron(rs.getInt("iron_amount"));
                civ.setFood(rs.getInt("food_amount"));
                civ.setMana(rs.getInt("mana_amount"));
                civ.setMagicTower(rs.getInt("magicTower_counter"));
                civ.setChurch(rs.getInt("church_counter"));
                civ.setFarm(rs.getInt("farm_counter"));
                civ.setSmithy(rs.getInt("smithy_counter"));
                civ.setCarpentry(rs.getInt("carpentry_counter"));
                civ.setTechnologyDefense(rs.getInt("technology_defense_level"));
                civ.setTechnologyAttack(rs.getInt("technology_attack_level"));
                civ.setBattles(rs.getInt("battles_counter"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error cargando civilización: " + e.getMessage());
        }
        return civ;
    }

    // Devuelve el ID de la primera civilización guardada (para simplificar)
    public int getFirstCivilizationId() {
        int id = -1;
        
        // 1. Validamos que la conexión no sea nula antes de usarla
        if (this.con == null) {
            System.err.println("🚨 Error: 'this.con' es null. No se puede conectar a la base de datos.");
            return id; // Retorna -1 y evita que el programa se cierre bruscamente
        }

        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT civilization_id FROM civilization_stats LIMIT 1");
            
            if (rs.next()) {
                id = rs.getInt("civilization_id");
            }
            
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.out.println("Error obteniendo ID civilización: " + e.getMessage());
        }
        return id;
    }
}
	
