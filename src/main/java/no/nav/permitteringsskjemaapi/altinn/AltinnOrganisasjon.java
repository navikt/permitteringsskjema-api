package no.nav.permitteringsskjemaapi.altinn;

import lombok.Value;

@Value
public class AltinnOrganisasjon {
    @JsonProperty("Name")
    String Name;
    @JsonProperty("Type")
    String Type;
    @JsonProperty("OrganizationNumber")
    String OrganizationNumber;
    @JsonProperty("OrganizationForm")
    String OrganizationForm;
    @JsonProperty("Status")
    String Status;
    @JsonProperty("ParentOrganizationNumber")
    String ParentOrganizationNumber;
}
