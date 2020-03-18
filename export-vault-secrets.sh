#!/usr/bin/env bash

if test -f /var/run/secrets/nais.io/srvpermittering-api/password;
then
    export  SYSTEMBRUKER_PASSWORD=$(cat /var/run/secrets/nais.io/srvpermittering-api/password)
    echo "Setting SYSTEMBRUKER_PASSWORD"
fi

if test -f /var/run/secrets/nais.io/srvpermittering-api/username;
then
    export  SYSTEMBRUKER_USERNAME=$(cat /var/run/secrets/nais.io/srvpermittering-api/username)
    echo "Setting SYSTEMBRUKER_USERNAME"
fi

if test -f /var/run/secrets/nais.io/permittering-api/altinn-api-gw-header/x-nav-apiKey;
then
    export  ALTINN_APIGW_HEADER=$(cat /var/run/secrets/nais.io/permittering-api/altinn-api-gw-header/x-nav-apiKey)
    echo "Setting ALTINN_APIGW_HEADER"
fi
