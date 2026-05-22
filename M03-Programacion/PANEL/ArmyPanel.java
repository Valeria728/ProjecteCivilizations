package PANEL;

import javax.swing.*;
import javax.swing.border.*;

import exceptions.*;
import game.Civilization;

import java.awt.*;
import java.awt.event.*;

//Panel para crear nuevas unidades militares.
public class ArmyPanel extends JPanel {

    private Civilization civ;
    private MainGUI mainGUI;

    // Spinners para la cantidad
    private JSpinner spSwordsman, spSpearman, spCrossbow, spCannon;
    private JSpinner spArrowTower, spCatapult, spRocket;
    private JSpinner spMagician, spPriest;

    // Labels de totales actuales
    private JLabel lblTotalSwordsman, lblTotalSpearman, lblTotalCrossbow, lblTotalCannon;
    private JLabel lblTotalArrowTower, lblTotalCatapult, lblTotalRocket;
    private JLabel lblTotalMagician, lblTotalPriest;

    public ArmyPanel(Civilization civ, MainGUI mainGUI) {
        this.civ = civ;
        this.mainGUI = mainGUI;
        setBackground(new Color(30, 30, 40));
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Crear Unidades Militares", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(255, 215, 0));
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 4, 8, 8));
        grid.setBackground(new Color(30, 30, 40));

        // Cabecera
        grid.add(makeHeader("Unidad"));
        grid.add(makeHeader("Actual"));
        grid.add(makeHeader("Cantidad"));
        grid.add(makeHeader(""));

        // Ataque
        lblTotalSwordsman = addRow(grid, "⚔ Espadachín",    civ.getArmy()[0].size(), spSwordsman = makeSpinner());
        spSwordsman = (JSpinner) getLastSpinner(grid);
        addCreateButton(grid, "Espadachín",   spSwordsman);

        lblTotalSpearman  = addRowFull(grid, "🗡 Lancero",      civ.getArmy()[1].size(), spSpearman  = makeSpinner(), "Lancero");
        lblTotalCrossbow  = addRowFull(grid, "🏹 Ballesta",     civ.getArmy()[2].size(), spCrossbow  = makeSpinner(), "Ballesta");
        lblTotalCannon    = addRowFull(grid, "💣 Cañón",        civ.getArmy()[3].size(), spCannon    = makeSpinner(), "Cañón");

        // Separador defensa
        grid.add(makeSep("── Defensas ──"));
        grid.add(new JLabel()); grid.add(new JLabel()); grid.add(new JLabel());

        lblTotalArrowTower= addRowFull(grid, "🏰 Torre Flecha",  civ.getArmy()[4].size(), spArrowTower= makeSpinner(), "TorreFlecha");
        lblTotalCatapult  = addRowFull(grid, "🪨 Catapulta",     civ.getArmy()[5].size(), spCatapult  = makeSpinner(), "Catapulta");
        lblTotalRocket    = addRowFull(grid, "🚀 Lanzacohetes",  civ.getArmy()[6].size(), spRocket    = makeSpinner(), "Lanzacohetes");

        // Separador especial
        grid.add(makeSep("── Especiales ──"));
        grid.add(new JLabel()); grid.add(new JLabel()); grid.add(new JLabel());

        lblTotalMagician  = addRowFull(grid, "🔮 Mago",          civ.getArmy()[7].size(), spMagician  = makeSpinner(), "Mago");
        lblTotalPriest    = addRowFull(grid, "✝ Sacerdote",     civ.getArmy()[8].size(), spPriest    = makeSpinner(), "Sacerdote");

        JScrollPane scroll = new JScrollPane(grid);
        scroll.getViewport().setBackground(new Color(30, 30, 40));
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    // Añade una fila completa al grid y devuelve el label del total
    private JLabel addRowFull(JPanel grid, String name, int total, JSpinner spinner, String type) {
        JLabel nameLabel  = makeLabel(name);
        JLabel totalLabel = makeLabel(String.valueOf(total));
        grid.add(nameLabel);
        grid.add(totalLabel);
        grid.add(spinner);
        addCreateButtonToGrid(grid, type, spinner, totalLabel);
        return totalLabel;
    }

    // Versión legacy mantenida por compatibilidad
    private JLabel addRow(JPanel grid, String name, int total, JSpinner spinner) {
        JLabel nameLabel  = makeLabel(name);
        JLabel totalLabel = makeLabel(String.valueOf(total));
        grid.add(nameLabel);
        grid.add(totalLabel);
        grid.add(spinner);
        return totalLabel;
    }

    private void addCreateButtonToGrid(JPanel grid, String type, JSpinner spinner, JLabel totalLabel) {
        JButton btn = makeButton("Crear");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int n = (int) spinner.getValue();
                createUnits(type, n, totalLabel);
            }
        });
        grid.add(btn);
    }

    private void addCreateButton(JPanel grid, String type, JSpinner spinner) {
        JButton btn = makeButton("Crear");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int n = (int) spinner.getValue();
                createUnits(type, n, null);
            }
        });
        grid.add(btn);
    }

    private void createUnits(String type, int n, JLabel totalLabel) {
        try {
            if (type.equals("Espadachín"))     { civ.newSwordsman(n);      if (totalLabel != null) totalLabel.setText(String.valueOf(civ.getArmy()[0].size())); }
            else if (type.equals("Lancero"))   { civ.newSpearman(n);       if (totalLabel != null) totalLabel.setText(String.valueOf(civ.getArmy()[1].size())); }
            else if (type.equals("Ballesta"))  { civ.newCrossbow(n);       if (totalLabel != null) totalLabel.setText(String.valueOf(civ.getArmy()[2].size())); }
            else if (type.equals("Cañón"))     { civ.newCannon(n);         if (totalLabel != null) totalLabel.setText(String.valueOf(civ.getArmy()[3].size())); }
            else if (type.equals("TorreFlecha")) { civ.newArrowTower(n);   if (totalLabel != null) totalLabel.setText(String.valueOf(civ.getArmy()[4].size())); }
            else if (type.equals("Catapulta")) { civ.newCatapult(n);       if (totalLabel != null) totalLabel.setText(String.valueOf(civ.getArmy()[5].size())); }
            else if (type.equals("Lanzacohetes")) { civ.newRocketLauncher(n); if (totalLabel != null) totalLabel.setText(String.valueOf(civ.getArmy()[6].size())); }
            else if (type.equals("Mago"))      { civ.newMagician(n);       if (totalLabel != null) totalLabel.setText(String.valueOf(civ.getArmy()[7].size())); }
            else if (type.equals("Sacerdote")) { civ.newPriest(n);         if (totalLabel != null) totalLabel.setText(String.valueOf(civ.getArmy()[8].size())); }

            mainGUI.setStatus("✅ " + n + " " + type + "(s) creados.");
            mainGUI.refreshAllPanels();
            mainGUI.saveToDB();

        } catch (ResourceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sin recursos", JOptionPane.WARNING_MESSAGE);
            mainGUI.refreshAllPanels();
            mainGUI.saveToDB();
        } catch (BuildingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Edificio requerido", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh(Civilization civ) {
        this.civ = civ;
        if (lblTotalSwordsman  != null) lblTotalSwordsman.setText(String.valueOf(civ.getArmy()[0].size()));
        if (lblTotalSpearman   != null) lblTotalSpearman.setText(String.valueOf(civ.getArmy()[1].size()));
        if (lblTotalCrossbow   != null) lblTotalCrossbow.setText(String.valueOf(civ.getArmy()[2].size()));
        if (lblTotalCannon     != null) lblTotalCannon.setText(String.valueOf(civ.getArmy()[3].size()));
        if (lblTotalArrowTower != null) lblTotalArrowTower.setText(String.valueOf(civ.getArmy()[4].size()));
        if (lblTotalCatapult   != null) lblTotalCatapult.setText(String.valueOf(civ.getArmy()[5].size()));
        if (lblTotalRocket     != null) lblTotalRocket.setText(String.valueOf(civ.getArmy()[6].size()));
        if (lblTotalMagician   != null) lblTotalMagician.setText(String.valueOf(civ.getArmy()[7].size()));
        if (lblTotalPriest     != null) lblTotalPriest.setText(String.valueOf(civ.getArmy()[8].size()));
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------
    private JSpinner makeSpinner() {
        JSpinner s = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        s.setBackground(new Color(45, 45, 60));
        s.setForeground(Color.WHITE);
        s.setPreferredSize(new Dimension(80, 28));
        return s;
    }

    // Obtiene el último spinner añadido (truco para compatibilidad)
    private JSpinner getLastSpinner(JPanel grid) {
        Component[] comps = grid.getComponents();
        for (int i = comps.length - 1; i >= 0; i--) {
            if (comps[i] instanceof JSpinner) {
                return (JSpinner) comps[i];
            }
        }
        return makeSpinner();
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(new Color(200, 220, 255));
        return l;
    }

    private JLabel makeHeader(String text) {
        JLabel l = new JLabel(text, JLabel.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(new Color(255, 215, 0));
        return l;
    }

    private JLabel makeSep(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.ITALIC, 12));
        l.setForeground(new Color(150, 150, 200));
        return l;
    }

    private JButton makeButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(70, 100, 160));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return b;
    }
}
