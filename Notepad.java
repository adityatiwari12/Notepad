import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.DefaultHighlighter;

public class Notepad extends JFrame {
    private JTextArea textArea;
    private JLabel statusBar;
    private JFileChooser fileChooser;
    private File currentFile = null;
    private boolean darkMode = true;

    public Notepad() {
        setTitle("Notepad");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setIconImage(new ImageIcon("C:\\Users\\tiwar\\OneDrive\\Desktop\\Java Course\\Notepad\\note.png").getImage());

        textArea = new JTextArea();
        textArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 18));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        statusBar = new JLabel("Ln 1, Col 1 | 0 chars");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusBar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        add(statusBar, BorderLayout.SOUTH);

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Documents (*.txt)", "txt"));

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        menuBar.setBackground(new Color(36, 37, 38));

        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(new Font("Segoe UI", Font.BOLD, 15));
        fileMenu.setForeground(Color.WHITE);

        JMenuItem newItem = createMenuItem("New", "control N", e -> newFile());
        JMenuItem openItem = createMenuItem("Open...", "control O", e -> openFile());
        JMenuItem saveItem = createMenuItem("Save", "control S", e -> saveFile());
        JMenuItem saveAsItem = createMenuItem("Save As...", null, e -> saveFileAs());
        JMenuItem exitItem = createMenuItem("Exit", "control Q", e -> System.exit(0));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setFont(new Font("Segoe UI", Font.BOLD, 15));
        editMenu.setForeground(Color.WHITE);

        JMenuItem cutItem = createMenuItem("Cut", "control X", e -> textArea.cut());
        JMenuItem copyItem = createMenuItem("Copy", "control C", e -> textArea.copy());
        JMenuItem pasteItem = createMenuItem("Paste", "control V", e -> textArea.paste());
        JMenuItem findItem = createMenuItem("Find/Replace", "control F", e -> showFindReplaceDialog());
        JMenuItem selectAllItem = createMenuItem("Select All", "control A", e -> textArea.selectAll());

        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(findItem);
        editMenu.add(selectAllItem);

        JMenu formatMenu = new JMenu("Format");
        formatMenu.setFont(new Font("Segoe UI", Font.BOLD, 15));
        formatMenu.setForeground(Color.WHITE);

        JMenuItem fontItem = createMenuItem("Font...", null, e -> showFontDialog());
        JCheckBoxMenuItem wrapItem = new JCheckBoxMenuItem("Word Wrap", true);
        wrapItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        wrapItem.setForeground(Color.WHITE);
        wrapItem.setBackground(new Color(36, 37, 38));
        wrapItem.addActionListener(e -> textArea.setLineWrap(wrapItem.isSelected()));

        formatMenu.add(fontItem);
        formatMenu.add(wrapItem);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setFont(new Font("Segoe UI", Font.BOLD, 15));
        viewMenu.setForeground(Color.WHITE);

        JCheckBoxMenuItem darkModeItem = new JCheckBoxMenuItem("Dark Mode", darkMode);
        darkModeItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        darkModeItem.setForeground(Color.WHITE);
        darkModeItem.setBackground(new Color(36, 37, 38));
        darkModeItem.addActionListener(e -> toggleDarkMode(darkModeItem.isSelected()));
        viewMenu.add(darkModeItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("Segoe UI", Font.BOLD, 15));
        helpMenu.setForeground(Color.WHITE);

        JMenuItem aboutItem = createMenuItem("About", null, e -> JOptionPane.showMessageDialog(this,
                "<html><center><h2>Futuristic Notepad</h2><br>By Your Name<br><br>Powered by Java Swing</center></html>",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        textArea.addCaretListener(e -> updateStatusBar());
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateStatusBar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateStatusBar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStatusBar(); }
        });

        applyTheme();

        setVisible(true);
    }

    private JMenuItem createMenuItem(String text, String shortcut, ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        item.setBackground(new Color(36, 37, 38));
        item.setForeground(Color.WHITE);
        if (shortcut != null) item.setAccelerator(KeyStroke.getKeyStroke(shortcut));
        item.addActionListener(action);
        item.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return item;
    }

    private void newFile() {
        if (confirmSave()) {
            textArea.setText("");
            currentFile = null;
            setTitle("Notepad");
        }
    }

    private void openFile() {
        if (!confirmSave()) return;
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                textArea.read(br, null);
                currentFile = file;
                setTitle("Futuristic Notepad - " + file.getName());
            } catch (IOException ex) {
                showError("Could not open file.");
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
        } else {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentFile))) {
                textArea.write(bw);
            } catch (IOException ex) {
                showError("Could not save file.");
            }
        }
    }

    private void saveFileAs() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                textArea.write(bw);
                currentFile = file;
                setTitle("Futuristic Notepad - " + file.getName());
            } catch (IOException ex) {
                showError("Could not save file.");
            }
        }
    }

    private boolean confirmSave() {
        if (textArea.getText().isEmpty()) return true;
        int option = JOptionPane.showConfirmDialog(this, "Do you want to save changes?", "Save",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (option == JOptionPane.CANCEL_OPTION) return false;
        if (option == JOptionPane.YES_OPTION) saveFile();
        return true;
    }

    private void showFindReplaceDialog() {
        JDialog dialog = new JDialog(this, "Find/Replace", false);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);

        JLabel findLabel = new JLabel("Find:");
        JTextField findField = new JTextField(15);
        JLabel replaceLabel = new JLabel("Replace:");
        JTextField replaceField = new JTextField(15);

        JButton findNext = new JButton("Find Next");
        JButton replace = new JButton("Replace");
        JButton replaceAll = new JButton("Replace All");

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(findLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; dialog.add(findField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(replaceLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; dialog.add(replaceField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(findNext, gbc);
        gbc.gridx = 1; gbc.gridy = 2; dialog.add(replace, gbc);
        gbc.gridx = 1; gbc.gridy = 3; dialog.add(replaceAll, gbc);

        DefaultHighlighter.DefaultHighlightPainter highlightPainter =
                new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 215, 0, 128));

        findNext.addActionListener(e -> {
            String text = textArea.getText();
            String find = findField.getText();
            int start = textArea.getCaretPosition();
            int idx = text.indexOf(find, start);
            if (idx == -1 && start > 0) idx = text.indexOf(find, 0);
            textArea.getHighlighter().removeAllHighlights();
            if (idx != -1 && !find.isEmpty()) {
                try {
                    textArea.getHighlighter().addHighlight(idx, idx + find.length(), highlightPainter);
                    textArea.setCaretPosition(idx + find.length());
                } catch (Exception ignored) {}
            }
        });

        replace.addActionListener(e -> {
            String find = findField.getText();
            String replaceStr = replaceField.getText();
            int start = textArea.getCaretPosition();
            String text = textArea.getText();
            int idx = text.indexOf(find, start);
            if (idx == -1 && start > 0) idx = text.indexOf(find, 0);
            if (idx != -1 && !find.isEmpty()) {
                textArea.select(idx, idx + find.length());
                textArea.replaceSelection(replaceStr);
            }
        });

        replaceAll.addActionListener(e -> {
            String find = findField.getText();
            String replaceStr = replaceField.getText();
            textArea.setText(textArea.getText().replace(find, replaceStr));
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showFontDialog() {
        JDialog dialog = new JDialog(this, "Choose Font", true);
        dialog.setLayout(new FlowLayout());

        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        JComboBox<String> fontBox = new JComboBox<>(fonts);
        fontBox.setSelectedItem(textArea.getFont().getFamily());

        Integer[] sizes = {12, 14, 16, 18, 20, 22, 24, 28, 32, 36, 40};
        JComboBox<Integer> sizeBox = new JComboBox<>(sizes);
        sizeBox.setSelectedItem(textArea.getFont().getSize());

        JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
            String font = (String) fontBox.getSelectedItem();
            int size = (Integer) sizeBox.getSelectedItem();
            textArea.setFont(new Font(font, Font.PLAIN, size));
            dialog.dispose();
        });

        dialog.add(new JLabel("Font:"));
        dialog.add(fontBox);
        dialog.add(new JLabel("Size:"));
        dialog.add(sizeBox);
        dialog.add(ok);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Dark Mode
    private void toggleDarkMode(boolean enable) {
        darkMode = enable;
        applyTheme();
    }

    private void applyTheme() {
        // MacOS-style colors
        Color bg = darkMode ? new Color(29, 29, 31) : new Color(255, 255, 255);
        Color fg = darkMode ? new Color(220, 220, 220) : new Color(29, 29, 31);
        Color caret = darkMode ? new Color(93, 188, 255) : new Color(0, 122, 255);
        Color menuBg = darkMode ? new Color(36, 36, 38) : new Color(242, 242, 242);
        Color menuFg = darkMode ? new Color(220, 220, 220) : new Color(29, 29, 31);
        Color accent = darkMode ? new Color(93, 188, 255) : new Color(0, 122, 255);
        Color statusBg = darkMode ? new Color(36, 36, 38) : new Color(242, 242, 242);
        Color statusFg = darkMode ? Color.WHITE : new Color(29, 29, 31);

        // Text Area with MacOS style
        textArea.setBackground(bg);
        textArea.setForeground(fg);
        textArea.setCaretColor(caret);
        textArea.setSelectionColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
        textArea.setBorder(BorderFactory.createCompoundBorder(
            new MacOSStyleBorder(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40), 10),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Scrollbars with MacOS style
        JScrollPane scrollPane = (JScrollPane) textArea.getParent().getParent();
        scrollPane.setBorder(null);
        scrollPane.setBackground(bg);
        scrollPane.getViewport().setBackground(bg);
        JScrollBar vBar = scrollPane.getVerticalScrollBar();
        JScrollBar hBar = scrollPane.getHorizontalScrollBar();
        vBar.setUI(new MacOSStyleScrollBarUI(accent, bg));
        hBar.setUI(new MacOSStyleScrollBarUI(accent, bg));

        // Status bar with MacOS style
        statusBar.setOpaque(true);
        statusBar.setBackground(statusBg);
        statusBar.setForeground(statusFg);
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, 
                darkMode ? new Color(0, 0, 0, 50) : new Color(0, 0, 0, 20)),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        // Menu bar with MacOS style
        JMenuBar mb = getJMenuBar();
        if (mb != null) {
            mb.setBackground(menuBg);
            mb.setForeground(menuFg);
            mb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, 
                    darkMode ? new Color(0, 0, 0, 50) : new Color(0, 0, 0, 20)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
            
            // Style all menu items
            for (MenuElement menu : mb.getSubElements()) {
                if (menu.getComponent() instanceof JMenu) {
                    JMenu m = (JMenu) menu.getComponent();
                    m.setBackground(menuBg);
                    m.setForeground(menuFg);
                    m.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    for (Component c : m.getMenuComponents()) {
                        if (c instanceof JMenuItem) {
                            JMenuItem mi = (JMenuItem) c;
                            mi.setBackground(menuBg);
                            mi.setForeground(menuFg);
                            mi.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                            mi.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
                        }
                    }
                }
            }
        }
        
        // Update UI manager for consistent look
        updateMenuUI(menuBg, menuFg, accent);
    }

    private void updateStatusBar() {
        int caretPos = textArea.getCaretPosition();
        int line = 1, column = 1;
        try {
            line = textArea.getLineOfOffset(caretPos) + 1;
            column = caretPos - textArea.getLineStartOffset(line - 1) + 1;
        } catch (Exception ignored) {}
        int chars = textArea.getText().length();
        statusBar.setText("Ln " + line + ", Col " + column + " | " + chars + " chars");
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(Notepad::new);
    }
private void updateMenuUI(Color menuBg, Color menuFg, Color accent) {
    UIManager.put("Menu.selectionBackground", accent);
    UIManager.put("Menu.selectionForeground", menuBg);
    UIManager.put("MenuItem.selectionBackground", accent);
    UIManager.put("MenuItem.selectionForeground", menuBg);
    UIManager.put("CheckBoxMenuItem.selectionBackground", accent);
    UIManager.put("CheckBoxMenuItem.selectionForeground", menuBg);
    UIManager.put("PopupMenu.background", menuBg);
    UIManager.put("PopupMenu.foreground", menuFg);
    UIManager.put("Menu.background", menuBg);
    UIManager.put("Menu.foreground", menuFg);
    UIManager.put("MenuItem.background", menuBg);
    UIManager.put("MenuItem.foreground", menuFg);
    UIManager.put("CheckBoxMenuItem.background", menuBg);
    UIManager.put("CheckBoxMenuItem.foreground", menuFg);
    SwingUtilities.updateComponentTreeUI(this);
}
    // Inner classes go here
    class MacOSStyleBorder extends AbstractBorder {
        private final Color borderColor;
        private final int radius;
        
    public MacOSStyleBorder(Color color, int radius) {
        this.borderColor = color;
        this.radius = radius;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius, radius, radius, radius);
    }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.draw(new RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius));
            g2.dispose();
        }
    }

    class MacOSStyleScrollBarUI extends BasicScrollBarUI {
        private final Color thumbColor;
        private final Color trackColor;

        MacOSStyleScrollBarUI(Color thumb, Color track) {
            this.thumbColor = thumb;
            this.trackColor = track;
        }

        @Override
        protected void configureScrollBarColors() {
            // Colors are handled in constructor and painting methods
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.width == 0 || r.height == 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int arc = Math.min(r.width, r.height);
            Color transparentThumb = new Color(thumbColor.getRed(), 
                                             thumbColor.getGreen(), 
                                             thumbColor.getBlue(), 
                                             160);
            g2.setColor(transparentThumb);
            g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, arc, arc));
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(trackColor);
            g2.fill(r);
            g2.dispose();
        }
    }
} // End of Notepad class
