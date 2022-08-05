package org.eclipse.dataspaceconnector.registration;

import org.eclipse.dataspaceconnector.iam.did.spi.document.DidDocument;
import org.eclipse.dataspaceconnector.iam.did.spi.resolution.DidResolver;
import org.eclipse.dataspaceconnector.spi.result.Result;
import org.jetbrains.annotations.NotNull;

/**
 * internal mock did resolver for the "mock" method. Returns an empty {@link DidDocument} for every did in the format
 * "did:mock...."
 */
class MockDidResolver implements DidResolver {
    @Override
    public @NotNull String getMethod() {
        return "mock";
    }

    @Override
    public @NotNull Result<DidDocument> resolve(String didKey) {
        return Result.success(DidDocument.Builder.newInstance()
                .build());
    }
}
