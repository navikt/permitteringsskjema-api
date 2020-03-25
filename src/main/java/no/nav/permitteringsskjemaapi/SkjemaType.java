package no.nav.permitteringsskjemaapi;

public enum SkjemaType {
    MASSEOPPSIGELSE("Masseoppsigelse"),
    PERMITTERING_UTEN_LØNN("Permittering uten lønn"),
    INNSKRENKNING_I_ARBEIDSTID("Innskrenkning i arbeidstid");

    private final String navn;

    SkjemaType(String navn) {
        this.navn = navn;
    }

    public String getNavn() {
        return navn;
    }
}
