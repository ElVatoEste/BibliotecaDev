            package entity;
            import jakarta.persistence.*;
            import lombok.Getter;
            import lombok.Setter;

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
            public class Reserva {

                @Id
                @GeneratedValue(strategy = GenerationType.IDENTITY)
                @Column(name = "id_reserva", nullable = false)
                private Long IdReserva; // Clave primaria

                @Column(name = "Nombre", length = 50 ,nullable = false)
                private String nombreEstudiante;

                @Column(name = "cif", nullable = false)
                private Long cif;

                @Column(name = "asunto_reserva", length = 75, nullable = false)
                private String asuntoReserva;

                @Column(name = "cantidad_personas", nullable = false)
                private int cantidadPersonas;

                @Temporal(TemporalType.TIMESTAMP)
                @Column(name = "fecha_entrada", nullable = false)
                private LocalDateTime fechaEntrada;

                @Temporal(TemporalType.TIMESTAMP)
                @Column(name = "fecha_salida", nullable = false)
                private LocalDateTime fechaSalida;

                @Column(name = "utiliza_pizarra", nullable = false)
                private boolean utilizaPizarra;

                @Column(name = "utiliza_proyector", nullable = false)
                private boolean utilizaProyector;

                @Column(name = "utiliza_computadora", nullable = false)
                private boolean utilizaComputadora;

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



            }

