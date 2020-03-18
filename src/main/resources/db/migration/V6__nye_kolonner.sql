alter table permitteringsskjema rename column org_nr to bedrift_nr;
alter table permitteringsskjema add column bedrift_navn varchar;
alter table permitteringsskjema add column kontakt_epost varchar;