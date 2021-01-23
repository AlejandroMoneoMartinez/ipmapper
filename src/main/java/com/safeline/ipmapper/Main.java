package com.safeline.ipmapper;

import com.safeline.ipmapper.exceptions.UnitConnectionException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Main extends JFrame {
    private JPanel contentPanel;
    private JButton btnSetIp;
    private JTextArea textAreaLog;

    public Main() {
        btnSetIp.addActionListener(e -> {
            Enumeration<NetworkInterface> enumNwi = null;
            String ip = "";
            try {
                enumNwi = NetworkInterface.getNetworkInterfaces();
                while( enumNwi.hasMoreElements() ) {
                    NetworkInterface nwi = enumNwi.nextElement();
                    if ( nwi.isUp() ) { // if the nw is up and running
                        Enumeration<InetAddress> enumInetAddresses = nwi.getInetAddresses();
                        while ( enumInetAddresses.hasMoreElements() ) {
                            InetAddress inetAddress = enumInetAddresses.nextElement();
                            // check if it is not 127.0.0.1
                            if (!inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                ip = inetAddress.getHostAddress();
                            }
                        }
                    }
                }

                String ipAddress = "83.48.10.67";
                InetAddress inet = InetAddress.getByName(ipAddress);
                boolean reachable = inet.isReachable(5000);

                if (StringUtils.hasText(ip)) {
                    String command = "/Z" + "1234" + "z1638=00" + "OF&";
                    sendRelayCommand("83.48.10.67", "75", command);
                    JOptionPane.showMessageDialog(contentPanel, "Ip assigned successfully: " + ip + "-" + reachable,
                            "Success", JOptionPane.OK_OPTION, new ImageIcon(""));
                }
            } catch (SocketException | UnitConnectionException socketException) {
                JOptionPane.showMessageDialog(contentPanel, "Conection timeout",
                "Error", JOptionPane.ERROR_MESSAGE, null);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }

    public JPanel getContentPanel() {
        return contentPanel;
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
