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
                        name = "Agenda.FindEventByCIF",
                        query = "SELECT a FROM Reserva a WHERE a = :id_reserva"
                )
        })
        public class Reserva {

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            @Column(name = "id_reserva", nullable = false)
            private Long IdReserva; // Clave primaria

            @Column(name = "cif", nullable = true)
            private Long cif;

            @Column(name = "Nombre", length = 50 ,nullable = false)
            private String nombreEstudiante;

            @Column(name = "asunto_reserva", length = 75, nullable = false)
            private String asuntoReserva;

            @Column(name = "cantidad_personas", nullable = false)
            private int cantidadPersonas;

            @Temporal(TemporalType.TIMESTAMP)
            @Column(name = "fecha_entrada", nullable = false)
            private Date fechaEntrada;

            @Temporal(TemporalType.TIMESTAMP)
            @Column(name = "fecha_salida", nullable = false)
            private Date fechaSalida;

            @Column(name = "utiliza_pizarra", nullable = false)
            private boolean utilizaPizarra;

            @Column(name = "utiliza_proyector", nullable = false)
            private boolean utilizaProyector;

            @Column(name = "utiliza_computadora", nullable = false)
            private boolean utilizaComputadora;
        }
