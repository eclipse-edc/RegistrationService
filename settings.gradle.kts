rootProject.name = "registration-service"

include(":spi:participant-store-spi")
include(":spi:dataspace-authority-spi")
include(":launcher")
include(":extensions:registration-service")
include(":extensions:participant-verifier")
include(":extensions:registration-policy-gaiax-member")
include(":system-tests")
include(":system-tests:launchers:identity-hub")
include(":rest-client")
include(":client-cli")
include(":extensions:store:sql:participant-store-sql")
include(":extensions:store:cosmos:participant-store-cosmos")
