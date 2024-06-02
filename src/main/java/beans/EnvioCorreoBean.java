package beans;

import jakarta.mail.Session;
import jakarta.mail.Transport;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.Serializable;
import java.net.PasswordAuthentication;
import java.util.Properties;


public class EnvioCorreoBean implements Serializable {

    public void init(){
        final String userName = "contactenos@gmail.com"; //same fromMail
        final String password = "1234567891123456";
        final String toEmail = "jj7000@gmail.com";

        System.out.println("TLSEmail Start");
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
        props.put("mail.smtp.port", "587"); //TLS Port
        props.put("mail.smtp.auth", "true"); //enable authentication
        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS


        Session session = Session.getDefaultInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication();

            }
        });

        try{
            MimeMessage message = new MimeMessage(session);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("destinatario@mailserver.com.co", true));
            message.setSubject("Prueba");
            message.setText("Blablabla");
            System.out.println("sending...");
            Transport.send(message);
            System.out.println("Sent message successfully....");

        }catch (MessagingException me){
            System.out.println("Exception: "+me);

        }
    }
}
