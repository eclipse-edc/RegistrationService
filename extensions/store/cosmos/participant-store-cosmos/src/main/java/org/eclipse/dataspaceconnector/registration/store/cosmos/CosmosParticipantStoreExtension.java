/*
 *  Copyright (c) 2020, 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.registration.store.cosmos;

import dev.failsafe.RetryPolicy;
import org.eclipse.dataspaceconnector.azure.cosmos.CosmosClientProvider;
import org.eclipse.dataspaceconnector.azure.cosmos.CosmosDbApi;
import org.eclipse.dataspaceconnector.azure.cosmos.CosmosDbApiImpl;
import org.eclipse.dataspaceconnector.registration.store.cosmos.model.ParticipantDocument;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Extension;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Inject;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provider;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provides;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.system.health.HealthCheckService;

/**
 * Extension that provides a {@link ParticipantStore} with CosmosDB as backend storage
 */
@Extension(value = CosmosParticipantStoreExtension.NAME)
@Provides(ParticipantStore.class)
public class CosmosParticipantStoreExtension implements ServiceExtension {

    public static final String NAME = "Cosmos Participant Store";
    
    @Inject
    private RetryPolicy<Object> retryPolicy;
    @Inject
    private Vault vault;
    @Inject
    private CosmosClientProvider clientProvider;

    private CosmosDbApi cosmosDbApi;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        context.getService(HealthCheckService.class).addReadinessProvider(() -> cosmosDbApi.get().forComponent(name()));
        context.getTypeManager().registerTypes(ParticipantDocument.class);
    }


    @Provider
    public ParticipantStore participantStore(ServiceExtensionContext context) {
        var configuration = new ParticipantStoreCosmosConfig(context);
        var client = clientProvider.createClient(vault, configuration);
        cosmosDbApi = new CosmosDbApiImpl(configuration, client);
        return new CosmosParticipantStore(cosmosDbApi, configuration.getPartitionKey(), context.getTypeManager().getMapper(), retryPolicy);
    }
}
