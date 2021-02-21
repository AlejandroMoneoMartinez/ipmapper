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
    public static final String COLOR_PRIMARY = "#007aff";
    public static final String COLOR_SUCCESS = "#5fa125";
    public static final String DEFAULT_SURELINE_IP = "192.168.2.10";

    private JPanel contentPane;
    private JLabel lbl1, lbl2, lbl3, lbl4, lbl5, lblIp, lblFooter;
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
        this.lbl1.setBounds(20, 250, 760, 30);
        this.contentPane.add(lbl1);

        this.lbl2 = new JLabel("2. (Opcional): Pulse el botón de reset durante 10 seg. para restablecer la configuración TCP/IP de fábrica");
        this.lbl2.setBounds(20, 300, 760, 30);
        this.contentPane.add(lbl2);

        this.lbl3 = new JLabel("<html>3. Asegúrese de que su ordenador esté conectado a la misma red y haga clic en el botón \"SET LOCAL IP\"</html>");
        this.lbl3.setBounds(20, 350, 760, 30);
        this.contentPane.add(lbl3);

        this.lbl4 = new JLabel("4. IMPORTANTE: Anótese la IP asignada");
        this.lbl4.setBounds(20, 400, 760, 30);
        this.contentPane.add(lbl4);

        this.lbl5 = new JLabel("5. Visualice y gestione la unidad desde la IP asignada");
        this.lbl5.setBounds(20, 450, 760, 30);
        this.contentPane.add(lbl5);

        this.lblIp = new JLabel("ASSIGNED IP:");
        this.lblIp.setForeground(Color.decode(COLOR_SUCCESS));
        this.lblIp.setBounds(450, 250, 90, 30);
        this.lblIp.setVisible(false);
        this.contentPane.add(lblIp);

        this.btnSetIp = new JButton("SET LOCAL IP");
        this.btnSetIp.addActionListener(this);
        this.btnSetIp.setBounds(550, 250, 150, 30);
        this.btnSetIp.setForeground(Color.white);
        this.btnSetIp.setBackground(Color.decode(COLOR_PRIMARY));
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
                BufferedReader reader = new BufferedReader(new InputStreamReader(procIpConfig.getInputStream()));
                String line, ip = "", mask = "", gateway = "";

                while ((line = reader.readLine()) != null && ip.length() == 0){
                    if (StringUtils.containsIgnoreCase(line, "ipv4")) {
                        ip = line.split(" : ")[1];
                        line = reader.readLine();
                        mask = line.split(" : ")[1];
                        line = reader.readLine();
                        gateway = line.split(" : ")[1];
                    }
                }

                Process procRoute = Runtime.getRuntime().exec("route add " + DEFAULT_SURELINE_IP + " " + ip);
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(procRoute.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(procRoute.getErrorStream()));

                while ((line = stdInput.readLine()) != null) {
                    log.info(line);
                }
                while ((line = stdError.readLine()) != null) {
                    log.error(line);
                }

                String newIp = getAvailableIp(ip);

                if (newIp == null)
                    throw new NullPointerException();

                sendTCPCommand(DEFAULT_SURELINE_IP, "80", newIp, "80", mask, gateway);

                this.btnSetIp.setVisible(false);

                this.txtIp = new JTextField(newIp);
                this.txtIp.setBounds(550, 250, 150, 30);
                this.txtIp.setForeground(Color.decode(COLOR_SUCCESS));
                this.txtIp.setBackground(Color.WHITE);
                this.txtIp.setEditable(false);
                this.txtIp.setBorder(new LineBorder(Color.decode(COLOR_SECONDARY)));

                this.lblIp.setVisible(true);

                this.contentPane.add(this.txtIp);

                wait(1500);

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                    Desktop.getDesktop().browse(new URI("http://" + newIp));

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
        for (int i = Integer.parseInt(localIp.substring(localIp.lastIndexOf(".") + 1)) + 1; i < 255; i++) {
            String candidateIp = localIp.substring(0, localIp.lastIndexOf(".") + 1) + i;
            if (!InetAddress.getByName(candidateIp).isReachable(500))
                return candidateIp;
        }
        return null;
    }

    private void sendTCPCommand(String ip, String port, String newIp, String newPort, String newMask, String newGateway) throws UnitConnectionException {
        try {
            RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
            ResponseEntity<String> response = restTemplate.exchange("http://" + ip + ":" + port + "/Z1234z1680=IP:" + newIp + "&PORT:" + newPort + "&MASK:" + newMask + "&GATEWAY:" + newGateway + "&", HttpMethod.POST, new HttpEntity(new HttpHeaders()), String.class);
            int status = response.getStatusCode().value();
            if (status != 200)
                throw new UnitConnectionException(status);
        } catch (ResourceAccessException e){ //timeout has produced
            throw new UnitConnectionException(408); //Comentado ya que la unidad retorna 200 desde la nueva IP por tanto siempre se produce el timeout
        } catch (UnitConnectionException e){
            throw new UnitConnectionException(e.getStatus());
        } catch (Exception e){
            throw new UnitConnectionException(404);
        }
    }

    private void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() { //needs org.apache.httpcomponents dependency in pom
        int timeout = 3000;
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout);
        return clientHttpRequestFactory;
    }
}
