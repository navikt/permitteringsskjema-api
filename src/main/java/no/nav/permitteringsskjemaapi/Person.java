package no.nav.permitteringsskjemaapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.UUID;

// Lombok
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// JPA
@Entity
public class Person {
    @Id
    @JsonIgnore
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "permitteringsskjema_id")
    @JsonIgnore
    @ToString.Exclude
    private Permitteringsskjema permitteringsskjema;

    private String fnr;
    private Integer grad;
    private String kommentar;
}
