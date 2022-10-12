rootProject.name = "registration-service"

include(":launcher")
include(":extensions:registration-service")
include(":extensions:participant-verifier")
include(":extensions:registration-policy-gaiax-member")
include(":extensions:participant-store-spi")
include(":extensions:dataspace-authority-spi")
include(":system-tests")
include(":system-tests:launchers:identity-hub")
include(":rest-client")
include(":client-cli")
include(":extensions:store:sql:participant-store-sql")
