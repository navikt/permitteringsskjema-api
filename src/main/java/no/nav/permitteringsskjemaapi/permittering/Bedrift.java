package no.nav.permitteringsskjemaapi.permittering;

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
public class Bedrift {
    @Id
    @JsonIgnore
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "permitteringsskjema_id")
    @JsonIgnore
    @ToString.Exclude
    private Permitteringsskjema permitteringsskjema;

    private Integer antall;
    private String navn;
    private String bedriftsnr;
}
