package no.nav.permitteringsskjemaapi.refusjon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RefusjonsberegningRepository extends JpaRepository<Refusjonsberegning, UUID> {
    List<Refusjonsberegning> findAllByRefusjonsskjemaId(UUID refusjonsskjemaId);
    List<Refusjonsberegning> findAllByRefusjonsskjemaIdAndOpprettetAv(UUID refusjonsskjemaId, String opprettetAv);
    List<Refusjonsberegning> findAllByInnhentetTidspunktIsNull();
}
