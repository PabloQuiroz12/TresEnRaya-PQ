package edu.upb.tresenraya.ui;

import edu.upb.tresenraya.Mediador;
import edu.upb.tresenraya.MessageListener;
import edu.upb.tresenraya.bl.*;
import edu.upb.tresenraya.database.ContactoDAO;
import edu.upb.tresenraya.logic.*;
import edu.upb.tresenraya.network.*;
import edu.upb.tresenraya.payment.*;
import edu.upb.tresenraya.utils.*;
import java.awt.BorderLayout;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class TresEnRayaUI extends javax.swing.JFrame implements MessageListener, SocketListener {

    private ServidorJuego servidorJuego;
    private TicTacToe game;
    private JPanel[] cells;
    private Socket socket;
    private SocketClient socketClient;
    static String jugadorActual;
    static String oponente;
    private Contactos contactosList;
    private Map<String, GameState> gameStates = new HashMap<>();
    private GameState gameState;
    private final DefaultListModel<Contacto> contacModel = new DefaultListModel<>();
//    private Map<String, TicTacToe> gameStates = new HashMap<>();

    public TresEnRayaUI() {
        initComponents();
        Mediador.addListener(this);
        MediadorContactos.getInstance().addListener(this);
        iniciarActualizacionAutomatica();

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                edu.upb.tresenraya.database.ConexionDb.getInstance().cerrarConexion();
            }
        });

        setTitle("Tres en Raya");
        setIconImage(new ImageIcon(getClass().getResource("/sources/images/logo.png")).getImage());

        game = new TicTacToe();
        contactosList = Contactos.getInstance();
        contactos.setModel(contacModel);
        contactos.setCellRenderer(new ContactRenderer());
        initializeCells();
        boolean servidorIniciado = (servidorJuego != null);

        btnInvitacion.setEnabled(true);
        btnIniciarJuego.setEnabled(servidorIniciado);
        btnNuevaPartida.setEnabled(servidorIniciado);

        List<Contacto> contactosLista = Contactos.getInstance().getListaContactos();
        new ContactChecker(contactosLista, this::updateContactList).start();
    }

    private void initializeCells() {
        cells = new JPanel[]{jPanel3, jPanel4, jPanel5, jPanel6, jPanel7, jPanel8, jPanel9, jPanel10, jPanel11};
        for (int i = 0; i < cells.length; i++) {
            final int row = i / 3;
            final int col = i % 3;
            cells[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onCellClicked(row, col);
                }
            });
        }
    }

    private void onCellClicked(int row, int col) {
        try {
            if (gameState == null || gameState.getCurrentPlayer() == null) {
                JOptionPane.showMessageDialog(this, "Debes iniciar una partida antes de jugar.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            System.out.println("Jugador actual esperado: " + gameState.getCurrentPlayer());
            System.out.println("Jugador que intenta jugar: " + jugadorActual);

            if (!gameState.getCurrentPlayer().equals(jugadorActual)) {
                JOptionPane.showMessageDialog(this, "No es tu turno.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (game.getCellMark(row, col) != TicTacToe.Mark.EMPTY) {
                JOptionPane.showMessageDialog(this, "Esta casilla ya está ocupada. Elige otra.", "Movimiento inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Marcar la celda con la ficha del jugador actual
            TicTacToe.Mark mark = jugadorActual.equals("X") ? TicTacToe.Mark.X : TicTacToe.Mark.O;
            SoundUtils.playSound("/sources/sounds/click.wav");

            if (game.markCell(row, col, mark)) {
                updateCellUI(row, col, mark);

                String nuevoTurno = jugadorActual.equals("X") ? "O" : "X";
                gameState.setCurrentPlayer(nuevoTurno);
                System.out.println("✔ Turno cambiado a: " + nuevoTurno);

                // Enviar el movimiento al oponente
                String message = "0008|" + mark.toString() + "|" + row + "|" + col;
                sendMessage(message, socketClient.getIp());

                // Verificar si la partida terminó
                checkGameOver();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void iniciarActualizacionAutomatica() {
        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateContactList();
            }
        });
        timer.start();
    }

    private void updateCellUI(int row, int col, TicTacToe.Mark mark) {
        if (row >= 0 && row < 3 && col >= 0 && col < 3) {
            JPanel cellPanel = cells[row * 3 + col];
            cellPanel.removeAll();

            if (mark != TicTacToe.Mark.EMPTY) {
                JLabel label = new JLabel(mark.toString());
                label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 60));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setVerticalAlignment(SwingConstants.CENTER);

                cellPanel.setLayout(new BorderLayout());
                cellPanel.add(label, BorderLayout.CENTER);

                if (mark == TicTacToe.Mark.X) {
                    cellPanel.setBackground(java.awt.Color.WHITE);
                    label.setForeground(java.awt.Color.BLUE);
                } else if (mark == TicTacToe.Mark.O) {
                    cellPanel.setBackground(java.awt.Color.WHITE);
                    label.setForeground(java.awt.Color.RED);
                }
            } else {
                cellPanel.setBackground(java.awt.Color.WHITE);
            }

            cellPanel.repaint();
            cellPanel.revalidate();
        }
    }

    private void checkGameOver() {
        TicTacToe.Mark winner = game.getWinner();

        if (winner != TicTacToe.Mark.EMPTY) {
            String message = (winner == TicTacToe.Mark.X) ? "¡El jugador X ha ganado!" : "¡El jugador O ha ganado!";
            SoundUtils.playSound("/sources/sounds/win.wav");
            JOptionPane.showMessageDialog(this, message, "Juego terminado", JOptionPane.INFORMATION_MESSAGE);

            if (socketClient != null) {
                sendMessage("0007", socketClient.getIp());
            }

            resetBoard();
        } else if (game.isBoardFull()) {
            JOptionPane.showMessageDialog(this, "¡Empate! No hay más movimientos disponibles.", "Juego terminado", JOptionPane.INFORMATION_MESSAGE);
            SoundUtils.playSound("/sources/sounds/draw.wav");
            if (socketClient != null) {
                sendMessage("0007", socketClient.getIp());
            }

            resetBoard();
        }
    }

    private void resetBoard() {
        game = new TicTacToe();
        for (JPanel cell : cells) {
            cell.setBackground(java.awt.Color.WHITE);
            cell.removeAll();
            cell.repaint();
        }
    }

    private void updateContactList() {
        SwingUtilities.invokeLater(() -> {
            DefaultListModel<Contacto> model = new DefaultListModel<>();
            List<String[]> contactosDB = ContactoDAO.obtenerContactos();

            for (String[] contacto : contactosDB) {
                if (contacto.length == 2) {
                    String nombre = contacto[0];
                    String ip = contacto[1];

                    // Verificamos si ya hay una conexión activa con esta IP
                    boolean estaConectado = Contactos.getInstance().getContactos().containsKey(ip);

                    model.addElement(new Contacto(nombre, ip, estaConectado));
                }
            }

            contactos.setModel(model);
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 15), new java.awt.Dimension(0, 15), new java.awt.Dimension(32767, 15));
        jToolBar1 = new javax.swing.JToolBar();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(15, 0), new java.awt.Dimension(15, 0), new java.awt.Dimension(15, 32767));
        btnServer = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        btnInvitacion = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        btnIniciarJuego = new javax.swing.JButton();
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 32767));
        btnNuevaPartida = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        contactos = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jToolBar1.setForeground(new java.awt.Color(204, 204, 255));
        jToolBar1.setRollover(true);
        jToolBar1.setMargin(new java.awt.Insets(15, 0, 15, 15));
        jToolBar1.add(filler4);

        btnServer.setBackground(new java.awt.Color(0, 102, 255));
        btnServer.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnServer.setForeground(new java.awt.Color(255, 255, 255));
        btnServer.setText("Iniciar Servidor");
        btnServer.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnServer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnServer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnServerActionPerformed(evt);
            }
        });
        jToolBar1.add(btnServer);
        jToolBar1.add(filler1);

        btnInvitacion.setBackground(new java.awt.Color(0, 102, 255));
        btnInvitacion.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnInvitacion.setForeground(new java.awt.Color(255, 255, 255));
        btnInvitacion.setText("Enviar Invitacion");
        btnInvitacion.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnInvitacion.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnInvitacion.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnInvitacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInvitacionActionPerformed(evt);
            }
        });
        jToolBar1.add(btnInvitacion);
        jToolBar1.add(filler2);

        btnIniciarJuego.setBackground(new java.awt.Color(0, 102, 255));
        btnIniciarJuego.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnIniciarJuego.setForeground(new java.awt.Color(255, 255, 255));
        btnIniciarJuego.setText("Iniciar Juego");
        btnIniciarJuego.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnIniciarJuego.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnIniciarJuego.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnIniciarJuego.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarJuegoActionPerformed(evt);
            }
        });
        jToolBar1.add(btnIniciarJuego);
        jToolBar1.add(filler7);

        btnNuevaPartida.setBackground(new java.awt.Color(0, 102, 255));
        btnNuevaPartida.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnNuevaPartida.setForeground(new java.awt.Color(255, 255, 255));
        btnNuevaPartida.setText("Nueva Partida");
        btnNuevaPartida.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnNuevaPartida.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNuevaPartida.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNuevaPartida.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevaPartidaActionPerformed(evt);
            }
        });
        jToolBar1.add(btnNuevaPartida);

        jSplitPane1.setDividerLocation(500);

        jPanel1.setBackground(new java.awt.Color(153, 153, 153));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.setLayout(new java.awt.GridLayout(3, 3, 10, 10));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel3);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel6);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel7);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel4);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel5);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel8);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel11);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel10);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 114, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel9);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 427, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel1);

        contactos.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                contactosValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(contactos);

        jSplitPane1.setRightComponent(jScrollPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 688, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnServerActionPerformed
        if (servidorJuego == null) {
            try {
                servidorJuego = new ServidorJuego(this);
                servidorJuego.start();
                btnServer.setText("Servidor Iniciado");

                btnInvitacion.setEnabled(true);
                btnIniciarJuego.setEnabled(true);
                btnNuevaPartida.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnServerActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String[] options = {"Tarjeta", "QR"};
        String monto = JOptionPane.showInputDialog(this, "Ingrese Monto:", JOptionPane.PLAIN_MESSAGE);
        int choice = JOptionPane.showOptionDialog(this, "Seleccione el método de cobro:", "Método de Cobro", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            String cardNumber = JOptionPane.showInputDialog(this, "Ingrese el número de tarjeta:", "Cobro por Tarjeta", JOptionPane.PLAIN_MESSAGE);
            String pin = JOptionPane.showInputDialog(this, "Ingrese el PIN:", "Cobro por Tarjeta", JOptionPane.PLAIN_MESSAGE);
            if (cardNumber != null && pin != null) {
                Pago pago = PagoFactory.crearPago("Tarjeta", monto, cardNumber, pin);
                pago.procesarPago();
                JOptionPane.showMessageDialog(this, "Cobro realizado con éxito por tarjeta.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Cobro cancelado o información incompleta.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (choice == 1) {
            /* ImageIcon originalIcon = new ImageIcon("C:\\Users\\Pablo\\Documents\\NetBeansProjects\\tres_en_raya-main\\src\\main\\java\\sources\\images\\qr_code.png");
            Image originalImage = originalIcon.getImage();
            int newWidth = 100;
            int newHeight = 100;
            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            JOptionPane.showMessageDialog(this, scaledIcon, "Cobro por QR", JOptionPane.PLAIN_MESSAGE, null);
            Pago pago = PagoFactory.crearPago("QR", monto, null, null);
            pago.procesarPago();
            JOptionPane.showMessageDialog(this, "Cobro realizado con éxito por QR.", "Éxito", JOptionPane.INFORMATION_MESSAGE);*/
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        String ip = JOptionPane.showInputDialog(this, "Ingrese la IP del jugador:", JOptionPane.PLAIN_MESSAGE);
        String nombre = JOptionPane.showInputDialog(this, "Ingrese su Nombre:", JOptionPane.PLAIN_MESSAGE);
        sendMessage("0001|" + nombre, ip);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnIniciarJuegoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarJuegoActionPerformed
        if (socketClient == null) {
            JOptionPane.showMessageDialog(this, "Debes conectarte a un jugador primero", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String ip = socketClient.getIp();

        String[] options = {"X", "O"};
        int choice = JOptionPane.showOptionDialog(null, "Seleccione su Ficha:", "Ficha",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        String miFicha = (choice == 0) ? TicTacToe.Mark.X.toString() : TicTacToe.Mark.O.toString();
        String oponenteFicha = miFicha.equals(TicTacToe.Mark.X.toString()) ? TicTacToe.Mark.O.toString() : TicTacToe.Mark.X.toString();
        jugadorActual = miFicha;
        oponente = oponenteFicha;

        boolean esMiTurno = miFicha.equals("X"); // Solo el jugador que elige "X" inicia primero
        gameState = new GameState(game, jugadorActual, oponenteFicha, esMiTurno);
        gameStates.put(ip, gameState);
        sendMessage("0004|" + miFicha, ip);

    }//GEN-LAST:event_btnIniciarJuegoActionPerformed

    private void btnInvitacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInvitacionActionPerformed
        String ip = JOptionPane.showInputDialog(this, "Ingrese la IP del jugador:", null);
        if (ip == null || ip.trim().isEmpty()) {
            return;
        }

        String nombre = JOptionPane.showInputDialog(this, "Ingrese su Nombre:", null);
        if (nombre == null || nombre.trim().isEmpty()) {
            return;
        }

        // Guardar en la base de datos
        ContactoDAO.agregarContacto(nombre, ip);

        // Enviar mensaje y actualizar lista
        sendMessage("0001|" + nombre, ip);
        updateContactList();
    }//GEN-LAST:event_btnInvitacionActionPerformed

    private void btnNuevaPartidaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaPartidaActionPerformed
        if (socketClient == null) {
            JOptionPane.showMessageDialog(this, "No hay un jugador conectado. Conéctate a alguien antes de iniciar una nueva partida.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String ip = socketClient.getIp();
        game = new TicTacToe(); // Reiniciar el juego

        for (JPanel cell : cells) {
            cell.setBackground(java.awt.Color.WHITE);
            cell.removeAll();
            cell.repaint();
        }

        boolean esMiTurno = jugadorActual.equals("X");

        if (!gameStates.containsKey(ip)) {
            gameState = new GameState(game, jugadorActual, oponente, esMiTurno);
            gameStates.put(ip, gameState);
        } else {
            gameState = gameStates.get(ip);
            gameState.setGame(game);
            gameState.setCurrentPlayer(jugadorActual);
            gameState.setOpponent(oponente);
        }

        sendMessage("0007", ip);

        System.out.println("🔄 Tablero reiniciado correctamente.");
        gameState.getGame().printBoard();
        System.out.println("📌 Jugador Actual: " + gameState.getCurrentPlayer());
        System.out.println("📌 Oponente: " + gameState.getOpponent());
    }//GEN-LAST:event_btnNuevaPartidaActionPerformed

    public void agregarContactoLista(Contacto contacto) {
        DefaultListModel<Contacto> model = (DefaultListModel<Contacto>) contactos.getModel();

        boolean existe = false;
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).getName().equals(contacto.getName())) {
                existe = true;
                break;
            }
        }

        if (!existe) {
            model.addElement(contacto);
        }
    }

    private void contactosValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_contactosValueChanged
        if (!evt.getValueIsAdjusting()) {
            Contacto selectedContact = contactos.getSelectedValue();

            if (selectedContact != null) {
                if (!selectedContact.isStateConnect()) {
                    System.out.println("Contacto no disponible: " + selectedContact.getName());
                    return;
                }

                socketClient = Contactos.getInstance().getContactos().get(selectedContact.getIp());

                if (socketClient == null) {
                    System.err.println("Error: No se encontró un socketClient para la IP: " + selectedContact.getIp());
                    return;
                }

                System.out.println("Contacto seleccionado: " + selectedContact.getName() + " | IP: " + selectedContact.getIp());
                // lblContactoSeleccionado.setText("Contacto seleccionado: " + selectedContact.getName() + " (" + selectedContact.getIp() + ")");
            }
        }
    }//GEN-LAST:event_contactosValueChanged

    public static void main(String args[]) {
        try {
            // Esta línea hace que los componentes respeten los colores personalizados
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("No se pudo aplicar Look and Feel plano: " + ex.getMessage());
        }

        // Iniciar la interfaz en el hilo de eventos
        java.awt.EventQueue.invokeLater(() -> {
            new TresEnRayaUI().setVisible(true);
        });
    }

    @Override
    public void onMessageReceived(String message) {
//        System.out.println(message);
        SwingUtilities.invokeLater(() -> {
            //jTextArea1.append("mensaje: " + message + "\n");
        });
    }

    @Override
    public void onMessageDuplicateRecieved(String message) {
        //        System.out.println(message);
        SwingUtilities.invokeLater(() -> {
            //jTextArea1.append("mensaje: " + message + "\n");
        });
    }

    @Override
    public void onIpReceived(String ip) {
        SwingUtilities.invokeLater(() -> {
            //jTextArea1.append("IP: " + ip + "\n");
        });
    }

    @Override
    public void onChangeTheme(String color) {
        SwingUtilities.invokeLater(() -> {
            btnServer.setBackground(ColorUtils.getColorFromString(color));
            //jTextArea1.setBackground(ColorUtils.getColorFromString(color));
            contactos.setBackground(ColorUtils.getColorFromString(color));
        });
    }

    @Override
    public void onSendInvitation(String nombre, String ip) {
        SoundUtils.playSound("/sources/sounds/invite.wav");
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showConfirmDialog(
                    null, nombre + " envió una solicitud", "Invitación",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    socketClient = new SocketClient(new Socket(ip, 1825));
                    socketClient.start();
                    System.out.println("Conexión establecida con " + ip);

                    // agregar a la base de datos el contacto invitado
                    ContactoDAO.agregarContacto(nombre, ip);

                    // actualizar la lista de contactos en la UI
                    updateContactList();

                    // enviar mensaje de aceptación
                    sendMessage("0003|" + "Pablo", ip);
                } catch (IOException e) {
                    System.err.println("❌ Error al conectar con el jugador: " + e.getMessage());
                    JOptionPane.showMessageDialog(null, "Error al conectar con el jugador.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    @Override
    public void onAcepted(String nombre) {
        SoundUtils.playSound("/sources/sounds/invite.wav");
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, nombre + " ha aceptado tu invitación", "Aceptada", JOptionPane.INFORMATION_MESSAGE);
        });

        if (socketClient == null) {
            System.err.println("❌ Error: socketClient es nulo en onAcepted.");
            return; // evita agregar un cliente nulo
        }

        // agregar a la base de datos el contacto que acepto la invitacion
        ContactoDAO.agregarContacto(nombre, socketClient.getIp());

        // actualizar la lista de contactos en la UI
        updateContactList();
    }

    @Override
    public void onRejected() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "La invitación ha sido rechazada", "Rechazada", JOptionPane.WARNING_MESSAGE); //172.16.61.10
        });
    }

    @Override
    public void onMarked(String symbol) {
        SwingUtilities.invokeLater(() -> {
            if (symbol == null || symbol.length() < 3) {
                System.err.println("Error: Mensaje recibido en onMarked() no válido: " + symbol);
                return;
            }

            System.out.println("Mensaje recibido en onMarked(): " + symbol);

            // Extraer ficha y coordenadas del mensaje
            char ficha = symbol.charAt(0);
            int posX = Character.getNumericValue(symbol.charAt(1));
            int posY = Character.getNumericValue(symbol.charAt(2));

            if (ficha != 'X' && ficha != 'O') {
                System.err.println("Error: Ficha inválida en onMarked(): " + ficha);
                return;
            }

            TicTacToe.Mark mark = (ficha == 'X') ? TicTacToe.Mark.X : TicTacToe.Mark.O;

            // Marcar celda si está libre
            if (game.getCellMark(posX, posY) == TicTacToe.Mark.EMPTY) {
                game.markCell(posX, posY, mark);
                updateCellUI(posX, posY, mark);

                // 🔄 **Corrección: Alternar turno correctamente**
                gameState.setCurrentPlayer(mark == TicTacToe.Mark.X ? "O" : "X");

                System.out.println("Se marcó " + mark + " en (" + posX + ", " + posY + ")");
                System.out.println("Nuevo turno: " + gameState.getCurrentPlayer());

                checkGameOver();
            } else {
                JOptionPane.showMessageDialog(this, "Se intentó marcar una celda ya ocupada por el oponente.", "Error de sincronización", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    @Override
    public void onBeginGame(String nombre, String symbol, String ip) {
        SoundUtils.playSound("/sources/sounds/invite.wav");
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showConfirmDialog(null, nombre + " quiere iniciar una partida", "Iniciar Juego", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                jugadorActual = symbol.equals("X") ? "O" : "X";
                oponente = symbol;

                boolean esMiTurno = (jugadorActual.equals("X") && symbol.equals("O")) || (jugadorActual.equals("O") && symbol.equals("X"));

                gameState = new GameState(game, jugadorActual, oponente, esMiTurno);
                gameStates.put(ip, gameState);

                sendMessage("0006", ip);
            } else if (choice == JOptionPane.NO_OPTION) {
                sendMessage("0005", ip); // rechazar la partida
            }
        });
    }

    @Override
    public void onRejectGame() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "La partida ha sido rechazada", "Juego Rechazado", JOptionPane.WARNING_MESSAGE);
        });
    }

    @Override
    public void onAcceptGame() {
        SoundUtils.playSound("/sources/sounds/invite.wav");
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "La partida ha sido aceptada", "Juego Aceptado", JOptionPane.INFORMATION_MESSAGE);
        });

    }

    @Override
    public void onNewGame() {
        SwingUtilities.invokeLater(() -> {
            game = new TicTacToe();

            if (socketClient != null && socketClient.getIp() != null) {
                String ip = socketClient.getIp();
                boolean esMiTurno = jugadorActual.equals("X");

                if (!gameStates.containsKey(ip)) {
                    gameState = new GameState(game, jugadorActual, oponente, esMiTurno);
                    gameStates.put(ip, gameState);
                } else {
                    gameState = gameStates.get(ip);
                    gameState.setGame(game);
                    gameState.setCurrentPlayer(jugadorActual);
                    gameState.setOpponent(oponente);
                }
            } else {
                System.err.println("Error: socketClient es nulo, no se pudo reiniciar la partida.");
                return;
            }

            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    updateCellUI(row, col, TicTacToe.Mark.EMPTY);
                }
            }

            System.out.println("Tablero reiniciado con nuevo estado de juego.");
        });

    }

    public void sendMessage(String message, String ipAddress) {
        try {
            if (socketClient == null || socketClient.getIp() == null) {
                System.err.println("Error: Intentando enviar mensaje sin conexión activa.");
                return;
            }

            if (socket == null || socket.isClosed()) {
                socket = new Socket(ipAddress, 1825);
                socketClient = new SocketClient(socket);
                socketClient.start();
                System.out.println("Conexión reestablecida con " + ipAddress);
            }

            socketClient.send((message + System.lineSeparator()).getBytes());
            System.out.println("Mensaje enviado: " + message);

        } catch (Exception e) {
            System.err.println("Error al enviar mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadGameState(String ip) {
        if (gameStates.containsKey(ip)) {
            gameState = gameStates.get(ip);
            jugadorActual = gameState.getCurrentPlayer();
            oponente = gameState.getOpponent();
            game = gameState.getGame();
            socketClient = Contactos.getInstance().getContactos().get(ip);
            updateBoardUI();
        }
    }

    private void updateBoardUI() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                TicTacToe.Mark mark = game.getCellMark(row, col);
                updateCellUI(row, col, mark);
            }
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnIniciarJuego;
    private javax.swing.JButton btnInvitacion;
    private javax.swing.JButton btnNuevaPartida;
    private javax.swing.JButton btnServer;
    private javax.swing.JList<Contacto> contactos;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void onNewClient(SocketClient sc) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Nuevo cliente conectado: " + sc.getIp());

            // Verificamos si el cliente ya está en la lista
            for (int i = 0; i < contactos.getModel().getSize(); i++) {
                Contacto c = contactos.getModel().getElementAt(i);
                if (c.getIp().equals(sc.getIp())) {
                    c.setStateConnect(true);  // marcar como conectado
                    break;
                }
            }

            updateContactList(); // refrescar UI
        });
    }

    @Override
    public void onClienteEliminado(String nombre) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    "El jugador '" + nombre + "' se ha desconectado.",
                    "Jugador Desconectado",
                    JOptionPane.WARNING_MESSAGE);

            System.out.println("Cliente eliminado: " + nombre);
            Contactos.getInstance().eliminarContacto(nombre);
            updateContactList();

            // Invalidar el estado del juego si el oponente se desconectó
            if (oponente.equals(nombre)) {
                gameState = null;
                socketClient = null;
                System.out.println("Juego cancelado, oponente desconectado.");
            }
        });
    }

    @Override
    public void onMovimientoExtra(String message) {
        SwingUtilities.invokeLater(() -> {
            String[] partes = message.split("\\|");
            if (partes.length != 3) {
                System.out.println("Error: formato incorrecto en onMovimientoExtra.");
                return;
            }

            String simbolo = partes[0];
            int posX = Integer.parseInt(partes[1]);
            int posY = Integer.parseInt(partes[2]);

            TicTacToe.Mark mark = simbolo.equals("X") ? TicTacToe.Mark.X : TicTacToe.Mark.O;

            if (game.markCell(posX, posY, mark)) {
                updateCellUI(posX, posY, mark);
            }
        });
    }

}
