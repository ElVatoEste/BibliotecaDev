package service;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

@Named("EnvioCorreo")
public class EnvioCorreoDAOImpl implements Serializable, EnvioCorreoDAO {

    private static final long serialVersionUID = 1L;

    public void enviarCorreoExitoso(String correo) {
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
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(correo, true));
                message.setSubject("Reservación de sala VIP Rubén Darío. Para nuestra comunidad UAM", "UTF-8");
                message.setText(
                        "Estimado/a estudiante,\n\n" +
                                "Su reserva de la sala VIP Rubén Darío ha sido agendada exitosamente. A continuación, le presento las indicaciones a seguir:\n\n" +
                                "1. Asistir puntual para confirmar su asistencia.\n\n" +
                                "2. Tras pasar los primeros 15 minutos a la hora solicitada de su reserva, automáticamente pierde el derecho y su reserva será revocada.\n\n" +
                                "3. Hacer uso debido de las instalaciones, manteniendo las normas éticas y de aseo.\n\n" +
                                "4. En caso de haber solicitado algún dispositivo, queda en total responsabilidad el cuidado y buen manejo del mismo.\n\n" +
                                "Saludos cordiales.\n\n" +
                                "Este es un mensaje auto-generado. Por favor, no responda a este correo."
                        , "UTF-8");
                System.out.println("sending...");
                Transport.send(message);
                System.out.println("Sent message successfully....");
            } catch (MessagingException me) {
                System.err.println("Failed to send email");
                me.printStackTrace();
                FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo enviar el correo de confirmación, revisar correo ingresado.");
                FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            }
        } catch (IOException ex) {
            System.err.println("Failed to load properties");
            ex.printStackTrace();
        }
    }

    public void enviarCorreoCancelacion(String correo) {
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
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(correo, true));
                message.setSubject("Aviso reservación de sala VIP Rubén Darío.", "UTF-8");
                message.setText(
                        "Estimado/a estudiante,\n\n" +
                                "Lamentamos informarle que su reserva de la sala VIP Rubén Darío ha sido revocada debido a no presentarse a la hora asignada.\n\n" +
                                "Si tiene alguna pregunta o necesita asistencia adicional, por favor, no dude en contactarnos.\n\n" +
                                "Saludos cordiales.\n\n" +
                                "Este es un mensaje auto-generado. Por favor, no responda a este correo."
                        , "UTF-8");
                System.out.println("sending...");

                Transport.send(message);
                System.out.println("Sent message successfully....");
            } catch (MessagingException me) {
                System.err.println("Failed to send email");
                me.printStackTrace();
                FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo enviar el correo de cancelacion, revisar correo ingresado.");
                FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            }
        } catch (IOException ex) {
            System.err.println("Failed to load properties");
            ex.printStackTrace();
        }
    }
}
