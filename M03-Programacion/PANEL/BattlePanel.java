package PANEL;

import javax.swing.*;
import javax.swing.border.*;

import DAO.BattleDAO;

import java.awt.*;
import java.awt.event.*;

//Panel para ver el historial de batallas y sus informes.

public class BattlePanel extends JPanel {

    private BattleDAO battleDAO;
    private int civId;

    private DefaultListModel<String> listModel;
    private JList<String> battleList;
    private JTextArea reportArea;

    public BattlePanel(BattleDAO battleDAO, int civId) {
        this.battleDAO = battleDAO;
        this.civId = civId;
        setBackground(new Color(30, 30, 40));
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Historial de Batallas", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(255, 215, 0));
        add(title, BorderLayout.NORTH);

        // Panel izquierdo: lista de batallas
        listModel = new DefaultListModel<String>();
        battleList = new JList<String>(listModel);
        battleList.setBackground(new Color(40, 40, 55));
        battleList.setForeground(new Color(200, 220, 255));
        battleList.setFont(new Font("Monospaced", Font.PLAIN, 13));
        battleList.setSelectionBackground(new Color(80, 80, 160));
        battleList.setFixedCellHeight(28);

        JScrollPane listScroll = new JScrollPane(battleList);
        listScroll.setPreferredSize(new Dimension(180, 0));
        listScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 160)),
            "Batallas", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), new Color(255, 215, 0)
        ));

        // Panel derecho: log de la batalla seleccionada
        reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setBackground(new Color(20, 20, 30));
        reportArea.setForeground(new Color(200, 240, 200));
        reportArea.setEditable(false);
        reportArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 160)),
            "Log de Batalla", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), new Color(255, 215, 0)
        ));

        // Botón ver log detallado
        JButton btnLog = new JButton("Ver desarrollo paso a paso");
        btnLog.setBackground(new Color(70, 100, 160));
        btnLog.setForeground(Color.WHITE);
        btnLog.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnLog.setFocusPainted(false);
        btnLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selected = battleList.getSelectedIndex();
                if (selected >= 0) {
                    int numBattle = selected + 1;
                    String log = battleDAO.loadBattleLog(civId, numBattle);
                    JTextArea logArea = new JTextArea(log);
                    logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
                    logArea.setEditable(false);
                    logArea.setBackground(new Color(20, 20, 30));
                    logArea.setForeground(new Color(220, 220, 180));
                    JScrollPane lsp = new JScrollPane(logArea);
                    lsp.setPreferredSize(new Dimension(700, 450));
                    JOptionPane.showMessageDialog(BattlePanel.this, lsp,
                        "Desarrollo Batalla " + numBattle, JOptionPane.PLAIN_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(BattlePanel.this,
                        "Selecciona una batalla de la lista.", "Sin selección", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Listener de selección en la lista
        battleList.addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                int idx = battleList.getSelectedIndex();
                if (idx >= 0) {
                    String log = battleDAO.loadBattleLog(civId, idx + 1);
                    if (log.isEmpty()) {
                        reportArea.setText("No hay datos guardados para esta batalla.");
                    } else {
                        // Solo mostrar las primeras líneas como resumen
                        String[] lines = log.split("\n");
                        StringBuilder sb = new StringBuilder();
                        int maxLines = 50;
                        for (int i = 0; i < lines.length && i < maxLines; i++) {
                            sb.append(lines[i]).append("\n");
                        }
                        if (lines.length > maxLines) {
                            sb.append("...(usa 'Ver desarrollo' para ver todo)...");
                        }
                        reportArea.setText(sb.toString());
                        reportArea.setCaretPosition(0);
                    }
                }
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, reportScroll);
        split.setDividerLocation(200);
        split.setBackground(new Color(30, 30, 40));
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
        add(btnLog, BorderLayout.SOUTH);

        // Cargar batallas existentes
        refreshBattleList(battleDAO, civId);
    }

    // Actualiza la lista con el número de batallas guardadas en BD
    public void refreshBattleList(BattleDAO dao, int civId) {
        this.battleDAO = dao;
        this.civId = civId;
        int count = dao.getBattleCount(civId);
        listModel.clear();
        for (int i = 1; i <= count; i++) {
            listModel.addElement("  Batalla " + i);
        }
        if (count == 0) {
            reportArea.setText("Aún no hay batallas registradas.\n" +
                               "Los ejércitos enemigos aparecen cada 3 minutos.");
        }
    }
}
