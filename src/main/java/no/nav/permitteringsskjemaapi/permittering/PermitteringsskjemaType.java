package no.nav.permitteringsskjemaapi.permittering;

public enum PermitteringsskjemaType {
    MASSEOPPSIGELSE("Masseoppsigelse"),
    PERMITTERING_UTEN_LØNN("Permittering uten lønn"),
    INNSKRENKNING_I_ARBEIDSTID("Innskrenkning i arbeidstid");

    private final String navn;

    PermitteringsskjemaType(String navn) {
        this.navn = navn;
    }

    public String getNavn() {
        return navn;
    }
}
