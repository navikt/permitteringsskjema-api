package no.nav.permitteringsskjemaapi.refusjon;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ArbeidsforholdRepository extends JpaRepository<Arbeidsforhold, UUID> {
    List<Arbeidsforhold> findAllByFnrIn(List<String> fnr);
    Page<Arbeidsforhold> findAllByRefusjonsskjemaIdAndOpprettetAv(UUID refusjonsskjemaId, String opprettetAv, Pageable pageable);
    List<Arbeidsforhold> findAllByInnhentetTidspunktIsNullOrderByFnr();
}
