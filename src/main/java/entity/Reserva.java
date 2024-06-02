            package entity;
            import jakarta.persistence.*;
            import lombok.Getter;
            import lombok.Setter;

            import java.io.Serializable;
            import java.time.LocalDateTime;
            import java.util.Date;

            @Entity
            @Table(name = "reservas")
            @Getter
            @Setter
            @NamedQueries({
                    @NamedQuery(
                            name = "Reserva.findAll",
                            query = "SELECT r FROM Reserva r"
                    )
            })
            public class Reserva implements Serializable {

                @Id
                @GeneratedValue(strategy = GenerationType.IDENTITY)
                @Column(name = "id_reserva", nullable = false)
                private Long IdReserva; // Clave primaria

                @Column(name = "Nombre", length = 50)
                private String nombreEstudiante;

                @Column(name = "cif")
                private Long cif;

                @Column(name = "correo")
                private String correo;

                @Column(name = "asunto_reserva", length = 75)
                private String asuntoReserva;

                @Column(name = "cantidad_personas")
                private int cantidadPersonas;

                @Temporal(TemporalType.TIMESTAMP)
                @Column(name = "fecha_entrada")
                private LocalDateTime fechaEntrada;

                @Temporal(TemporalType.TIMESTAMP)
                @Column(name = "fecha_salida")
                private LocalDateTime fechaSalida;

                @Column(name = "utiliza_pizarra", nullable = false)
                private boolean utilizaPizarra;

                @Column(name = "utiliza_proyector", nullable = false)
                private boolean utilizaProyector;

                @Column(name = "utiliza_computadora", nullable = false)
                private boolean utilizaComputadora;

                @Enumerated(EnumType.STRING)
                @Column(name = "asistencia", nullable = false)
                private AsistenciaEstado asistencia;

                public Boolean getUtilizaPizarra() {
                    return utilizaPizarra;
                }

                public Boolean getUtilizaProyector() {
                    return utilizaProyector;
                }
                public Boolean getUtilizaComputadora() {
                    return utilizaComputadora;
                }

                public void setUtilizaPizarra(Boolean utilizaPizarra) {
                    this.utilizaPizarra = utilizaPizarra;
                }

                public void setUtilizaProyector(Boolean utilizaProyector) {
                    this.utilizaProyector = utilizaProyector;
                }

                public void setUtilizaComputadora(Boolean utilizaComputadora) {
                    this.utilizaComputadora = utilizaComputadora;
                }

                public enum AsistenciaEstado {
                    PENDIENTE,
                    ASISTENCIA,
                    INASISTENCIA
                }




            }

