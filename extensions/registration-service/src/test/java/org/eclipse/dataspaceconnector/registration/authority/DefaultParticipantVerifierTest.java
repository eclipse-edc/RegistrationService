package org.eclipse.dataspaceconnector.registration.authority;

import org.eclipse.dataspaceconnector.iam.did.spi.credentials.CredentialsVerifier;
import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolverRegistry;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspaceconnector.registration.TestUtils.createParticipant;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultParticipantVerifierTest {

    private final DidResolverRegistry didResolverRegistryMock = mock(DidResolverRegistry.class);
    private final CredentialsVerifier credentialsVerifierMock = mock(CredentialsVerifier.class);
    private final DefaultParticipantVerifier participantVerifier = new DefaultParticipantVerifier(didResolverRegistryMock, credentialsVerifierMock);

    @BeforeEach
    void setUp() {
    }

    @Test
    void verifyParticipant_didNotFound() {
        when(didResolverRegistryMock.resolve(any())).thenReturn(Result.failure("no did found"));
        assertThat(participantVerifier.verifyCredentials(createParticipant().build()).succeeded()).isFalse();
    }

    @Test
    void verifyParticipant_claimsEmpty() {
        when(didResolverRegistryMock.resolve(any())).thenReturn(Result.success(new DidDocument()));
        when(credentialsVerifierMock.getVerifiedCredentials(any())).thenReturn(Result.success(Map.of()));
        assertThat(participantVerifier.verifyCredentials(createParticipant().build()).succeeded()).isFalse();
    }

    @Test
    void verifyParticipant_claimsFailed() {
        when(didResolverRegistryMock.resolve(any())).thenReturn(Result.success(new DidDocument()));
        when(credentialsVerifierMock.getVerifiedCredentials(any())).thenReturn(Result.failure("test failure"));
        assertThat(participantVerifier.verifyCredentials(createParticipant().build()).succeeded()).isFalse();
    }
}