package no.nav.permitteringsskjemaapi.permittering;

public enum Årsakskode {
    MANGEL_PÅ_ARBEID("Mangel på arbeid eller oppdrag"),
    RÅSTOFFMANGEL("Råstoffmangel"),
    ARBEIDSKONFLIKT_ELLER_STREIK("Arbeidskonflikt eller streik"),
    BRANN("Brann"),
    PÅLEGG_FRA_OFFENTLIG_MYNDIGHET("Pålegg fra offentlig myndighet"),
    ANDRE_ÅRSAKER("Andre årsaker");

    private final String navn;

    Årsakskode(String navn) {
        this.navn = navn;
    }

    public String getNavn() {
        return navn;
    }
}
