package beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Properties;

@Named
@RequestScoped
@Getter
@Setter
public class EnvioCorreoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userName; // Replace with your actual email
    private String password; // Replace with your actual password
    private String toEmail;
    private String subject = "test";
    private String body = "Body test";

    private Properties emailProperties;

    @PostConstruct
    public void init() {
        initEmailProperties();
    }

    private void initEmailProperties() {
        emailProperties = new Properties();
        emailProperties.put("mail.smtp.host", "smtp.gmail.com"); // SMTP Host
        emailProperties.put("mail.smtp.port", "587"); // TLS Port
        emailProperties.put("mail.smtp.auth", "true"); // Enable authentication
        emailProperties.put("mail.smtp.starttls.enable", "true"); // Enable STARTTLS
    }

    private Session createEmailSession() {
        return Session.getInstance(emailProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        });
    }

    public void sendEmail() {
        try {
            Session session = createEmailSession();
            MimeMessage message = new MimeMessage(session);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            System.out.println("Sent message successfully to " + toEmail);
        } catch (Exception e) {
            System.err.println("Error while sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }

}