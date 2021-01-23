package com.safeline.ipmapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Index extends JFrame implements ActionListener, WindowListener {
    private JPanel contentPane;
    private JPanel panel;

    public Index() {
        setTitle("IP MAPPER SURELINE");
        initComponents();
        this.setMinimumSize(this.getSize());
        this.setVisible(true);
    }

    private void initComponents() {
        addWindowListener(this);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 450, 250);
        this.contentPane = new JPanel();
        this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(this.contentPane);
        this.contentPane.setLayout(null);

        this.panel = new JPanel();
        this.panel.setBounds(64, 48, 319, 120);
        this.contentPane.add(this.panel);
        this.panel.setLayout(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
