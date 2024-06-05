package service;

public interface EnvioCorreoDAO {

    void enviarCorreoExitoso(String correo);

    void enviarCorreoCancelacion(String correo);
}
