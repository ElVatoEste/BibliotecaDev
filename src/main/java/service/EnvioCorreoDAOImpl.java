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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Properties;

@Named("EnvioCorreo")
public class EnvioCorreoDAOImpl implements Serializable, EnvioCorreoDAO {

    private static final long serialVersionUID = 1L;

    private Session session;

    // Inicializa el formateador de fecha
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM h:mm a", new Locale("es", "ES"));

    // Inicializa la sesión SMTP una sola vez
    private void initializeSession() {
        if (session == null) {
            Properties props = new Properties();
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("mail.properties")) {
                if (input == null) {
                    System.out.println("Sorry, unable to find mail.properties");
                    return;
                }

                props.load(input);
                String username = props.getProperty("mail.smtp.user");
                String password = props.getProperty("mail.smtp.password");

                session = Session.getInstance(props, new jakarta.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
            } catch (IOException ex) {
                System.err.println("Failed to load properties");
                ex.printStackTrace();
            }
        }
    }

    // Método genérico para enviar correos
    private void sendEmail(String to, String subject, String htmlMessage) {
        initializeSession();

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(session.getProperty("mail.smtp.user")));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlMessage, "text/html; charset=UTF-8");

            // Envío de mensaje asíncrono
            new Thread(() -> {
                try {
                    System.out.println("Sending email...");
                    Transport.send(message);
                    System.out.println("Email sent successfully");
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void enviarCorreoExitoso(String correo, LocalDateTime fechaEntrada, LocalDateTime fechaSalida) {
        String cuerpoMensaje = String.format(
                "<html>" +
                        "<body>" +
                        "<p>Estimado/a estudiante,</p>" +
                        "<p>Su reserva de la sala VIP Rubén Darío ha sido agendada exitosamente. A continuación, le presento las indicaciones a seguir:</p>" +
                        "<ol>" +
                        "<li><strong>Asistir puntual</strong> para confirmar su asistencia.</li>" +
                        "<li>Tras pasar los primeros 15 minutos a la hora solicitada de su reserva, automáticamente pierde el derecho y su reserva será revocada.</li>" +
                        "<li>Hacer uso debido de las instalaciones, manteniendo las normas éticas y de aseo.</li>" +
                        "<li>En caso de haber solicitado algún dispositivo, queda en total responsabilidad el cuidado y buen manejo del mismo.</li>" +
                        "</ol>" +
                        "<p><strong>Detalles de la reserva:</strong></p>" +
                        "<p>Fecha y hora de entrada: %s</p>" +
                        "<p>Fecha y hora de salida: %s</p>" +
                        "<br>" +
                        "<p>Saludos cordiales.</p>" +
                        "<p><i>Este es un mensaje auto-generado. Por favor, no responda a este correo.</i></p>" +
                        "</body>" +
                        "</html>",
                DATE_TIME_FORMATTER.format(fechaEntrada),
                DATE_TIME_FORMATTER.format(fechaSalida)
        );

        sendEmail(correo, "Reservación de sala VIP Rubén Darío. Para nuestra comunidad UAM", cuerpoMensaje);
    }

    public void enviarCorreoCancelacion(String correo) {
        String cuerpoMensaje = "<html>" +
                "<body>" +
                "<p>Estimado/a estudiante,</p>" +
                "<p>Lamentamos informarle que su reserva de la sala VIP Rubén Darío ha sido revocada debido a su no presentación a la hora asignada.</p>" +
                "<p>Si tiene alguna pregunta o necesita asistencia adicional, no dude en contactarnos.</p>" +
                "<br>" +
                "<p>Saludos cordiales,</p>" +
                "<p><i>Este es un mensaje auto-generado. Por favor, no responda a este correo.</i></p>" +
                "</body>" +
                "</html>";

        sendEmail(correo, "Aviso sobre la reservación de sala VIP Rubén Darío", cuerpoMensaje);
    }
}
