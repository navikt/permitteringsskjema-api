package no.nav.permitteringsskjemaapi;

public enum Årsakskode {
    AIKKE("Arbeidsgiver kan ikke påvirke"),
    AKAN("Arbeidsgiver kan påvirke"),
    AMANG("Mangel på arbeid"),
    BRANN("Brann"),
    FISYK("Fiske- og skjellsykdommer"),
    FORCE("Force Majeure"),
    LOCK("Lock-out"),
    OPPUS("Ombygging/oppussing av lokaler"),
    PAND("Pandemi"),
    POFFM("Pålegg fra offentlig myndighet"),
    PRVEM("Pris/Vekt/Markedsmessig"),
    PTRUK("Permittering trukket"),
    RASTM("Råstoffmangel"),
    STREI("Streik"),
    UNAVK("Under avklaring"),
    VEDOK("Venter på dokumentasjon");

    private final String navn;

    Årsakskode(String navn) {
        this.navn = navn;
    }
}
