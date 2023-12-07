insert into permitteringsskjema_v2 (id,
                                    type,
                                    bedrift_nr,
                                    bedrift_navn,
                                    kontakt_navn,
                                    kontakt_epost,
                                    kontakt_tlf,
                                    antall_berort,
                                    arsakskode,
                                    yrkeskategorier,
                                    start_dato,
                                    slutt_dato,
                                    ukjent_slutt_dato,
                                    sendt_inn_tidspunkt,
                                    opprettet_av)
select permitteringsskjema.id,
       type,
       bedrift_nr,
       bedrift_navn,
       kontakt_navn,
       kontakt_epost,
       kontakt_tlf,
       antall_berørt,
       årsakskode,
       jsonb_agg(to_jsonb(yrkeskategori) - 'id' - 'permitteringsskjema_id' - 'antall') as yrkeskategorier,
       start_dato,
       slutt_dato,
       ukjent_slutt_dato,
       sendt_inn_tidspunkt,
       opprettet_av
from permitteringsskjema
         left join yrkeskategori on permitteringsskjema.id = yrkeskategori.permitteringsskjema_id
where sendt_inn_tidspunkt is not null
  and sendt_inn_tidspunkt > '2021-12-01 00:00:00'
group by permitteringsskjema.id
on conflict (id) do nothing;

