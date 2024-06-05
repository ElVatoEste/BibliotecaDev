package beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

@Named
@RequestScoped
@Getter
@Setter
public class EnvioCorreoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public void enviarCorreo() {
        System.out.println("TLSEmail Start");

        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("mail.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find mail.properties");
                return;
            }

            // Carga el archivo de propiedades
            props.load(input);

            // Obtiene las credenciales
            String username = props.getProperty("mail.smtp.user");
            String password = props.getProperty("mail.smtp.password");

            Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress("bigmalv@gmail.com", true));
                message.setSubject("Prueba", "UTF-8");
                message.setText("Blablabla", "UTF-8");
                System.out.println("sending...");
                Transport.send(message);
                System.out.println("Sent message successfully....");
            } catch (MessagingException me) {
                System.err.println("Failed to send email");
                me.printStackTrace();
            }
        } catch (IOException ex) {
            System.err.println("Failed to load properties");
            ex.printStackTrace();
        }
    }
}
