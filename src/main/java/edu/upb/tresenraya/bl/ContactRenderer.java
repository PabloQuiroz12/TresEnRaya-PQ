package edu.upb.tresenraya.bl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ContactRenderer extends JLabel implements ListCellRenderer<Contacto> {

    private final ImageIcon onlineIcon;
    private final ImageIcon offlineIcon;

    public ContactRenderer() {
        this.onlineIcon = loadIcon("/sources/images/on-line.png");
        this.offlineIcon = loadIcon("/sources/images/off-line.png");
    }

    private ImageIcon loadIcon(String path) {
        URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        } else {
            System.err.println("⚠ Imagen no encontrada: " + path);
            return createPlaceholderIcon();
        }
    }

    private ImageIcon createPlaceholderIcon() {
        Image img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        return new ImageIcon(img);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Contacto> list, Contacto value, int index,
            boolean isSelected, boolean cellHasFocus) {
        setOpaque(true);
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 🟢 Mostrar imagen online/offline
        setIcon(value.isStateConnect() ? onlineIcon : offlineIcon);

        // 💬 Mostrar nombre e IP
        setText("<html><b>" + value.getName() + "</b><br/><small>IP: " + value.getIp() + "</small></html>");

        // 🎨 Colores
        setForeground(Color.DARK_GRAY);
        setBackground(isSelected ? new Color(220, 220, 255) : Color.WHITE);

        return this;
    }
}
