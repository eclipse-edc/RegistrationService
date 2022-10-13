/*
 *  Copyright (c) 2020 - 2022 Microsoft Corporation
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

package org.eclipse.dataspaceconnector.registration.store.sql;

import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.registration.store.sql.schema.ParticipantStatements;
import org.eclipse.dataspaceconnector.registration.store.sql.schema.PostgresSqlParticipantStatements;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.EdcSetting;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Extension;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Inject;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provider;
import org.eclipse.dataspaceconnector.runtime.metamodel.annotation.Provides;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transaction.TransactionContext;
import org.eclipse.dataspaceconnector.spi.transaction.datasource.DataSourceRegistry;

/**
 * Extension that provides a {@link ParticipantStore} with SQL as backend storage
 */
@Provides(ParticipantStore.class)
@Extension(value = SqlParticipantStoreExtension.NAME)
public class SqlParticipantStoreExtension implements ServiceExtension {

    public static final String NAME = "SQL participants store";

    @EdcSetting
    private static final String DATASOURCE_NAME_SETTING = "edc.datasource.participant.name";
    private static final String DEFAULT_DATASOURCE_NAME = "participant";
    @Inject(required = false)
    private ParticipantStatements statements;
    @Inject
    private DataSourceRegistry dataSourceRegistry;
    @Inject
    private TransactionContext trxContext;

    @Override
    public String name() {
        return NAME;
    }


    @Provider
    public ParticipantStore participantStore(ServiceExtensionContext context) {
        return new SqlParticipantStore(dataSourceRegistry, getDataSourceName(context), trxContext, context.getTypeManager().getMapper(), getStatementImpl());
    }

    /**
     * returns an externally-provided sql statement dialect, or postgres as a default
     */
    private ParticipantStatements getStatementImpl() {
        return statements != null ? statements : new PostgresSqlParticipantStatements();
    }

    private String getDataSourceName(ServiceExtensionContext context) {
        return context.getConfig().getString(DATASOURCE_NAME_SETTING, DEFAULT_DATASOURCE_NAME);
    }
}
