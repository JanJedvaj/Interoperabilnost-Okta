package hr.algebra.iis.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 120)
    @Column(unique = true)
    private String externalId;

    @NotBlank(message = "Naziv aplikacije ne smije biti prazan")
    @Size(min = 2, max = 120)
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Labela aplikacije ne smije biti prazna")
    @Size(min = 2, max = 200)
    @Column(nullable = false)
    private String label;

    @NotBlank(message = "Status ne smije biti prazan")
    @Column(nullable = false)
    private String status;

    @NotBlank(message = "Sign-on mode ne smije biti prazan")
    @Column(nullable = false)
    private String signOnMode;
}
