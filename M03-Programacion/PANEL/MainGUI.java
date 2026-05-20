package PANEL;


import javax.swing.*;
import DAO.BattleDAO;
import DAO.CivilizationDAO;
import game.Battle;
import game.Civilization;
import interfaces.MilitaryUnit;
import interfaces.Variables;
import units.attack.*;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

// Ventana principal del juego Civilizations.
// Usa JTabbedPane para organizar las secciones.

public class MainGUI extends JFrame implements Variables {

    private Civilization civilization;
    private CivilizationDAO civDAO;
    private BattleDAO battleDAO;
    private int civId;

    // Pestañas principales
    private JTabbedPane tabbedPane;

    // Paneles de cada pestaña
    private StatsPanel    statsPanel;
    private ArmyPanel     armyPanel;
    private BuildingsPanel buildingsPanel;
    private TechPanel     techPanel;
    private BattlePanel   battlePanel;

    // Barra de estado inferior
    private JLabel statusLabel;

    // Timers del juego
    private Timer gameTimer;

    // Ejército enemigo actual
    private ArrayList<MilitaryUnit> enemyArmy;

    public MainGUI() {
        civDAO  = new CivilizationDAO();
        battleDAO = new BattleDAO();
        civId   = civDAO.getFirstCivilizationId();

        // Cargar civilización desde BD o crear nueva
        if (civId != -1) {
            civilization = civDAO.loadCivilization(civId);
        }
        if (civilization == null) {
            civilization = new Civilization();
            civId = 1;
        }

        initUI();
        startTimers();
    }

    private void initUI() {
        setTitle("Civilizations - Projecte AMS i AWS curs 25-26");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- PALETA DE COLORES ---
        Color fondoMuyOscuro = new Color(15, 15, 25);
        Color fondoPanel     = new Color(30, 30, 45);
        Color dorado         = new Color(255, 215, 0);
        Color verdeEstado    = new Color(150, 255, 150);

        getContentPane().setBackground(fondoMuyOscuro);

        // Panel principal con margen
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(fondoMuyOscuro);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Título con estilo
        JLabel title = new JLabel("⚔  CIVILIZATIONS  ⚔", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 28)); // Fuente más épica
        title.setForeground(dorado);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        // Configuración de las Pestañas (JTabbedPane)
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(fondoPanel);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        // Quitar el borde feo por defecto si el LookAndFeel lo permite
        tabbedPane.setFocusable(false);

        statsPanel     = new StatsPanel(civilization);
        armyPanel      = new ArmyPanel(civilization, this);
        buildingsPanel = new BuildingsPanel(civilization, this);
        techPanel      = new TechPanel(civilization, this);
        battlePanel    = new BattlePanel(battleDAO, civId);

        // Añadir pestañas con iconos (puedes mantener tus emojis)
        tabbedPane.addTab("📊 Stats",      statsPanel);
        tabbedPane.addTab("⚔ Ejército",   armyPanel);
        tabbedPane.addTab("🏗 Edificios",  buildingsPanel);
        tabbedPane.addTab("🔬 Tecnología", techPanel);
        tabbedPane.addTab("📜 Batallas",   battlePanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Barra de estado inferior más elegante
        statusLabel = new JLabel("  Sistema listo. Esperando órdenes...");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        statusLabel.setForeground(verdeEstado);
        statusLabel.setBackground(new Color(10, 10, 20));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70), 1));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    // -------------------------------------------------------
    // Temporizadores del juego
    // -------------------------------------------------------
    private void startTimers() {
        gameTimer = new Timer();

        // Generar recursos cada 60 segundos
        TimerTask resourceTask = new TimerTask() {
            public void run() {
                generateResources();
            }
        };

        // Crear ejército enemigo y batalla cada 3 minutos
        TimerTask battleTask = new TimerTask() {
            public void run() {
                createEnemyAndBattle();
            }
        };

        gameTimer.schedule(resourceTask, 60000, 60000);
        gameTimer.schedule(battleTask, 180000, 180000);
    }

    private void generateResources() {
        int foodGen = CIVILIZATION_FOOD_GENERATED + civilization.getFarm() * CIVILIZATION_FOOD_GENERATED_PER_FARM;
        int woodGen = CIVILIZATION_WOOD_GENERATED + civilization.getCarpentry() * CIVILIZATION_WOOD_GENERATED_PER_CARPENTRY;
        int ironGen = CIVILIZATION_IRON_GENERATED + civilization.getSmithy() * CIVILIZATION_IRON_GENERATED_PER_SMITHY;
        int manaGen = civilization.getMagicTower() * CIVILIZATION_MANA_GENERATED_PER_MAGIC_TOWER;

        civilization.setFood(civilization.getFood() + foodGen);
        civilization.setWood(civilization.getWood() + woodGen);
        civilization.setIron(civilization.getIron() + ironGen);
        civilization.setMana(civilization.getMana() + manaGen);

        // Guardar en BD y actualizar UI desde hilo Swing
        civDAO.saveCivilization(civilization, civId);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                refreshAllPanels();
                setStatus("✅ Recursos generados: +" + foodGen + " comida, +" + woodGen + " madera, +" + ironGen + " hierro, +" + manaGen + " maná");
            }
        });
    }

    private void createEnemyAndBattle() {
        enemyArmy = createEnemyArmy();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Mostrar ventana de amenaza
                showThreatDialog();
            }
        });
    }

    private ArrayList<MilitaryUnit> createEnemyArmy() {
        ArrayList<MilitaryUnit> army = new ArrayList<MilitaryUnit>();
        int battles = civilization.getBattles();
        int increase = (int) Math.pow(1 + ENEMY_FLEET_INCREASE / 100.0, battles);

        int ironAvailable  = IRON_BASE_ENEMY_ARMY  * increase;
        int woodAvailable  = WOOD_BASE_ENEMY_ARMY  * increase;
        int foodAvailable  = FOOD_BASE_ENEMY_ARMY  * increase;

        int minFood = FOOD_COST_SWORDSMAN;
        int minWood = WOOD_COST_SWORDSMAN;
        int minIron = IRON_COST_SWORDSMAN;

        while (foodAvailable >= minFood && woodAvailable >= minWood && ironAvailable >= minIron) {
            int rand = (int)(Math.random() * 100);
            MilitaryUnit unit = null;

            if (rand < 35) {
                if (foodAvailable >= FOOD_COST_SWORDSMAN && woodAvailable >= WOOD_COST_SWORDSMAN && ironAvailable >= IRON_COST_SWORDSMAN) {
                    unit = new Swordsman();
                    foodAvailable -= FOOD_COST_SWORDSMAN;
                    woodAvailable -= WOOD_COST_SWORDSMAN;
                    ironAvailable -= IRON_COST_SWORDSMAN;
                }
            } else if (rand < 60) {
                if (foodAvailable >= FOOD_COST_SPEARMAN && woodAvailable >= WOOD_COST_SPEARMAN && ironAvailable >= IRON_COST_SPEARMAN) {
                    unit = new Spearman();
                    foodAvailable -= FOOD_COST_SPEARMAN;
                    woodAvailable -= WOOD_COST_SPEARMAN;
                    ironAvailable -= IRON_COST_SPEARMAN;
                }
            } else if (rand < 80) {
                if (woodAvailable >= WOOD_COST_CROSSBOW && ironAvailable >= IRON_COST_CROSSBOW) {
                    unit = new Crossbow();
                    woodAvailable -= WOOD_COST_CROSSBOW;
                    ironAvailable -= IRON_COST_CROSSBOW;
                }
            } else {
                if (woodAvailable >= WOOD_COST_CANNON && ironAvailable >= IRON_COST_CANNON) {
                    unit = new Cannon();
                    woodAvailable -= WOOD_COST_CANNON;
                    ironAvailable -= IRON_COST_CANNON;
                }
            }

            if (unit != null) {
                army.add(unit);
            } else {
                // No podemos crear la unidad aleatoria, intentamos swordsman
                if (foodAvailable >= FOOD_COST_SWORDSMAN && woodAvailable >= WOOD_COST_SWORDSMAN && ironAvailable >= IRON_COST_SWORDSMAN) {
                    army.add(new Swordsman());
                    foodAvailable -= FOOD_COST_SWORDSMAN;
                    woodAvailable -= WOOD_COST_SWORDSMAN;
                    ironAvailable -= IRON_COST_SWORDSMAN;
                } else {
                    break;
                }
            }
        }
        return army;
    }

    private void showThreatDialog() {
        StringBuilder sb = new StringBuilder("¡NUEVO EJÉRCITO ENEMIGO EN CAMINO!\n\n");
        int sw = 0, sp = 0, cb = 0, ca = 0;
        for (int i = 0; i < enemyArmy.size(); i++) {
            MilitaryUnit u = enemyArmy.get(i);
            if (u instanceof Swordsman)  sw++;
            else if (u instanceof Spearman) sp++;
            else if (u instanceof Crossbow) cb++;
            else if (u instanceof Cannon)   ca++;
        }
        sb.append("Swordsman:  ").append(sw).append("\n");
        sb.append("Spearman:   ").append(sp).append("\n");
        sb.append("Crossbow:   ").append(cb).append("\n");
        sb.append("Cannon:     ").append(ca).append("\n\n");
        sb.append("Total unidades: ").append(enemyArmy.size()).append("\n\n");
        sb.append("¿Preparado para la batalla?");

        int option = JOptionPane.showConfirmDialog(this, sb.toString(),
                "⚠ AMENAZA ENTRANTE", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            startBattle();
        }
    }

    // Inicia la batalla y guarda el resultado
    public void startBattle() {
        if (enemyArmy == null || enemyArmy.size() == 0) {
            JOptionPane.showMessageDialog(this, "No hay ejército enemigo todavía.", "Sin amenaza", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Battle battle = new Battle(civilization.getArmy(), createEnemyArmyGroups(enemyArmy), civilization.getBattles() + 1);
        battle.startBattle();

        civilization.setBattles(civilization.getBattles() + 1);

        // Recoger residuos si ganamos
        if (battle.getWinner().equals("Civilization")) {
            civilization.setWood(civilization.getWood() + battle.getWasteWood());
            civilization.setIron(civilization.getIron() + battle.getWasteIron());
        }

        // Restablecer armaduras del ejército superviviente
        ArrayList<MilitaryUnit>[] army = civilization.getArmy();
        for (int i = 0; i < army.length; i++) {
            for (int j = 0; j < army[i].size(); j++) {
                army[i].get(j).resetArmor();
            }
        }

        // Guardar en BD
        battleDAO.saveBattle(civId, civilization.getBattles(), battle);
        civDAO.saveCivilization(civilization, civId);

        enemyArmy = null;

        // Mostrar resultado
        String resultado = battle.getBattleReport(civilization.getBattles());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                refreshAllPanels();
                battlePanel.refreshBattleList(battleDAO, civId);
                showBattleResultDialog(resultado, battle.getBattleDevelopment());
                setStatus("Batalla " + civilization.getBattles() + " finalizada. Ganador: " + battle.getWinner());
            }
        });
    }

    private void showBattleResultDialog(String report, String development) {
        // Texto de reporte (Consola Verde)
        JTextArea ta = new JTextArea(report);
        ta.setFont(new Font("Monospaced", Font.BOLD, 13));
        ta.setEditable(false);
        ta.setBackground(new Color(10, 15, 10));
        ta.setForeground(new Color(50, 255, 50)); 
        
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(700, 450));
        sp.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 0)));

        // Personalizar el JOptionPane (Título y colores)
        UIManager.put("OptionPane.background", new Color(30, 30, 40));
        UIManager.put("Panel.background", new Color(30, 30, 40));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);

        int opt = JOptionPane.showConfirmDialog(this, sp,
                "📊 INFORME DE INTELIGENCIA MILITAR", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (opt == JOptionPane.YES_OPTION) {
            // Texto de desarrollo (Consola Ámbar)
            JTextArea dev = new JTextArea(development);
            dev.setFont(new Font("Monospaced", Font.PLAIN, 12));
            dev.setEditable(false);
            dev.setBackground(new Color(20, 15, 5)); 
            dev.setForeground(new Color(255, 180, 50));
            
            JScrollPane spDev = new JScrollPane(dev);
            spDev.setPreferredSize(new Dimension(750, 500));
            spDev.setBorder(BorderFactory.createLineBorder(new Color(150, 100, 0)));
            
            JOptionPane.showMessageDialog(this, spDev, "📜 LOG DETALLADO DE COMBATE", JOptionPane.PLAIN_MESSAGE);
        }
    }

    // -------------------------------------------------------
    // Métodos de utilidad para los paneles
    // -------------------------------------------------------
    public void refreshAllPanels() {
        statsPanel.refresh(civilization);
        armyPanel.refresh(civilization);
        buildingsPanel.refresh(civilization);
        techPanel.refresh(civilization);
    }

    public void setStatus(String msg) {
        statusLabel.setText("  " + msg);
    }

    public Civilization getCivilization() {
        return civilization;
    }

    public void saveToDB() {
        civDAO.saveCivilization(civilization, civId);
    }

    // -------------------------------------------------------
    // MAIN: punto de entrada gráfico
    // -------------------------------------------------------
    public static void main(String[] args) {
        // Aspecto oscuro del sistema operativo si está disponible
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // Ignorar, se usa el look por defecto
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainGUI().setVisible(true);
            }
        });
    }
    private ArrayList<MilitaryUnit>[] createEnemyArmyGroups(ArrayList<MilitaryUnit> simpleList) {
        ArrayList<MilitaryUnit>[] groups = new ArrayList[4];
        for (int i = 0; i < 4; i++) {
            groups[i] = new ArrayList<MilitaryUnit>();
        }
        for (MilitaryUnit u : simpleList) {
            if (u instanceof Swordsman) groups[0].add(u);
            else if (u instanceof Spearman) groups[1].add(u);
            else if (u instanceof Crossbow) groups[2].add(u);
            else if (u instanceof Cannon) groups[3].add(u);
        }
        return groups;
    }
}
