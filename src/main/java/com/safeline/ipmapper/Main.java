package com.safeline.ipmapper;

import com.safeline.ipmapper.exceptions.UnitConnectionException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;

public class Main extends JFrame implements ActionListener {

    public static final String COLOR_SECONDARY = "#888888";
    public static final String COLOR_SUCCESS = "#5fa125";

    private JPanel contentPane;
    private JLabel lbl1, lbl2, lbl3, lbl4, lblIp, lblFooter;
    private JLabel lblImg;
    private JButton btnSetIp;
    private JTextField txtIp;

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public Main() throws IOException {
        initComponents();
        this.setMinimumSize(new Dimension(800, 600));
        this.setVisible(true);
    }

    private void initComponents() throws IOException {
        setTitle("SURELINE IP MAPPER");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon(new ClassPathResource("static/icon.png").getURL()).getImage());
        setBounds(100, 100, 800, 600);
        this.contentPane = new JPanel();
        this.contentPane.setBackground(Color.white);
        setContentPane(this.contentPane);
        this.contentPane.setLayout(null);

        this.lblImg = new JLabel(new ImageIcon(new ImageIcon(new ClassPathResource("static/7wr-m1-eth-ref.jpg").getURL()).getImage().getScaledInstance(400, 230,  java.awt.Image.SCALE_SMOOTH)));
        this.lblImg.setBounds(200, 0, 400, 230);
        this.contentPane.add(lblImg);

        this.lbl1 = new JLabel("1. Conecte la unidad sureline al router");
        this.lbl1.setBounds(20, 250, 400, 30);
        this.contentPane.add(lbl1);

        this.lbl2 = new JLabel("<html>2. Asegúrese de que su ordenador esté conectado a la misma red y haga clic en el botón \"SET LOCAL IP\"</html>");
        this.lbl2.setBounds(20, 300, 760, 30);
        this.contentPane.add(lbl2);

        this.lbl3 = new JLabel("3. IMPORTANTE: Anótese la IP asignada");
        this.lbl3.setBounds(20, 350, 400, 30);
        this.contentPane.add(lbl3);

        this.lbl3 = new JLabel("4. Visualice y gestione la unidad desde la IP asignada");
        this.lbl3.setBounds(20, 400, 400, 30);
        this.contentPane.add(lbl3);

        this.lblIp = new JLabel("ASSIGNED IP:");
        this.lblIp.setForeground(Color.decode(COLOR_SUCCESS));
        this.lblIp.setBounds(460, 250, 80, 30);
        this.lblIp.setVisible(false);
        this.contentPane.add(lblIp);

        this.btnSetIp = new JButton("SET LOCAL IP");
        this.btnSetIp.addActionListener(this);
        this.btnSetIp.setBounds(550, 250, 150, 30);
        this.btnSetIp.setForeground(Color.white);
        this.btnSetIp.setBackground(Color.decode(COLOR_SECONDARY));
        this.btnSetIp.setBorderPainted(false);
        this.btnSetIp.setFocusPainted(false);
        this.contentPane.add(this.btnSetIp);

        this.lblFooter = new JLabel("© 1992 - " + LocalDate.now().getYear() + " Safeline. Todos los derechos reservados");
        this.lblFooter.setForeground(Color.decode(COLOR_SECONDARY));
        this.lblFooter.setBounds(20, 520, 400, 30);
        this.contentPane.add(lblFooter);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.btnSetIp) {
            try {
                Process procIpConfig = Runtime.getRuntime().exec("ipconfig");
                BufferedReader reader=new BufferedReader(new InputStreamReader(procIpConfig.getInputStream()));
                String line, ip = "", mask = "", gateway = "";

                int i = 0;
                while ((line = reader.readLine()) != null && ip.length() == 0){
                    if (StringUtils.containsIgnoreCase(line, "ipv4")) {
                        ip = line.split(" : ")[1];
                        line = reader.readLine();
                        mask = line.split(" : ")[1];
                        line = reader.readLine();
                        gateway = line.split(" : ")[1];
                    }
                }

                Process procRoute = Runtime.getRuntime().exec("route add 192.168.2.10 " + ip);
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(procRoute.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(procRoute.getErrorStream()));

                while ((line = stdInput.readLine()) != null) {
                    log.info(line);
                }
                while ((line = stdError.readLine()) != null) {
                    log.error(line);
                }

                String unitIp = getAvailableIp(ip);

                if (unitIp == null)
                    throw new NullPointerException();

                //unitIp = "83.48.10.67";
                String command = "/Z" + "1234" + "z1638=00" + "OF&";
                sendRelayCommand(unitIp, "80", command);

                this.btnSetIp.setVisible(false);

                this.txtIp = new JTextField(unitIp);
                this.txtIp.setBounds(550, 250, 150, 30);
                this.txtIp.setForeground(Color.decode(COLOR_SUCCESS));
                this.txtIp.setBackground(Color.WHITE);
                this.txtIp.setEditable(false);
                this.txtIp.setBorder(new LineBorder(Color.decode(COLOR_SECONDARY)));

                this.lblIp.setVisible(true);

                this.contentPane.add(this.txtIp);

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://" + unitIp));
                }

                /*JOptionPane.showMessageDialog(contentPane, "Ip assigned successfully: " + unitIp,
                        "Success", JOptionPane.OK_OPTION, new ImageIcon(""));*/
            } catch (IOException e) {
                JOptionPane.showMessageDialog(contentPane, "Failed to load network interfaces",
                        "Error", JOptionPane.ERROR_MESSAGE, null);
            } catch (NullPointerException e) {
                JOptionPane.showMessageDialog(contentPane, "Not available ip addresses found",
                        "Error", JOptionPane.ERROR_MESSAGE, null);
            } catch (UnitConnectionException e) {
                JOptionPane.showMessageDialog(contentPane, "Connection timeout",
                        "Error", JOptionPane.ERROR_MESSAGE, null);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private String getAvailableIp(String localIp) throws IOException {
        for (int i = Integer.parseInt(localIp.substring(localIp.lastIndexOf(".") + 1)) + 1; i < 256; i++) {
            String candidateIp = localIp.substring(0, localIp.lastIndexOf(".") + 1) + i;
            if (!InetAddress.getByName(candidateIp).isReachable(5000)) {
                return candidateIp;
            }
        }
        return null;
    }

    private void sendRelayCommand(String ip, String port, String command) throws UnitConnectionException {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange("http://" + ip + ":" + port + command, HttpMethod.POST, new HttpEntity(new HttpHeaders()), String.class);
            int status = response.getStatusCode().value();
            if (status != 200)
                throw new UnitConnectionException(status);
        } catch (ResourceAccessException e){ //timeout has produced
            //throw new UnitConnectionException(408);
            //OK solo en equipos antiguos que no sean de la caixa ya que no deulven respuesta
        } catch (UnitConnectionException e){
            throw new UnitConnectionException(e.getStatus());
        } catch (Exception e){
            throw new UnitConnectionException(404);
        }
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() { //needs org.apache.httpcomponents dependency in pom
        int timeout = 5000;
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout);
        return clientHttpRequestFactory;
    }
}
