package no.nav.permitteringsskjemaapi.altinn;

import lombok.Value;

@Value
public class AltinnOrganisasjon {
    String Name;
    String Type;
    String OrganizationNumber;
    String OrganizationForm;
    String Status;
    String ParentOrganizationNumber;
}
