package no.nav.permitteringsskjemaapi.integrasjon;

import no.nav.permitteringsskjemaapi.PermittertPerson;
import no.nav.permitteringsskjemaapi.integrasjon.arbeidsgiver.ArbeidsgiverRapport;

public interface Permittering {
    void publiser(PermittertPerson person);

    void publiser(ArbeidsgiverRapport rapport);
}
