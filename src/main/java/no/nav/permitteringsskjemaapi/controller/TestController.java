package no.nav.permitteringsskjemaapi.controller;

import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {
    @Unprotected
    @GetMapping(value="/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("hei");
    }

    @Protected
    @GetMapping(value="/testprotected")
    public ResponseEntity<String> testprotected() {
        return ResponseEntity.ok("hei");
    }



}
