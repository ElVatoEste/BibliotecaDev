package service;

import java.time.LocalDateTime;

public interface EnvioCorreoDAO {

    void enviarCorreoExitoso(String correo, LocalDateTime fechaEntrada, LocalDateTime fechaSalida);

    void enviarCorreoCancelacion(String correo);
}
