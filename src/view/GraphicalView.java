package src.view;

import javax.swing.*;
import java.util.List;

public class GraphicalView implements View {
    public JFrame frame;
    public JTextArea outputArea;

    public GraphicalView() {
        frame = new JFrame("Gestore Visite");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        outputArea = new JTextArea(20, 50);
        outputArea.setEditable(false);
        frame.add(new JScrollPane(outputArea));
        
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void mostraMessaggio(String messaggio) {
        SwingUtilities.invokeLater(() -> 
            outputArea.append(messaggio + "\n")
        );
    }

    @Override
    public void mostraErrore(String errore) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(frame, errore, "Errore", JOptionPane.ERROR_MESSAGE)
        );
    }

    @Override
    public void mostraElenco(List<String> elementi) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < elementi.size(); i++) {
                outputArea.append(String.format("%d. %s\n", i + 1, elementi.get(i)));
            }
        });
    }

    @Override
    public void mostraElencoConOggetti(List<?> oggetti) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < oggetti.size(); i++) {
                outputArea.append(String.format("%d. %s\n", i + 1, oggetti.get(i).toString()));
            }
        });
    }

    
    public void mostraElencoConList(String[] oggetti) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < oggetti.length; i++) {
                outputArea.append(String.format("%d. %s\n", i + 1, oggetti[i]));
            }
        });
    }

}
