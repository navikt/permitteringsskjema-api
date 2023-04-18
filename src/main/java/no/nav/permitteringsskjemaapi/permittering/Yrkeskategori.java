package no.nav.permitteringsskjemaapi.permittering;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

// Lombok
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// JPA
@Entity
public class Yrkeskategori {
    @Id
    @JsonIgnore
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "permitteringsskjema_id")
    @JsonIgnore
    @ToString.Exclude
    private Permitteringsskjema permitteringsskjema;

    private Integer konseptId;
    private String styrk08;
    private String label;
    private Integer antall;
}
