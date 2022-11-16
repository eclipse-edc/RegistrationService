/*
 *  Copyright (c) 2022 Microsoft Corporation
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

package org.eclipse.edc.registration.store.sql;

import org.eclipse.edc.registration.store.spi.ParticipantStore;
import org.eclipse.edc.registration.store.sql.schema.ParticipantStatements;
import org.eclipse.edc.registration.store.sql.schema.PostgresSqlParticipantStatements;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

/**
 * Extension that provides a {@link ParticipantStore} with SQL as backend storage
 */
@Provides(ParticipantStore.class)
@Extension(value = SqlParticipantStoreExtension.NAME)
public class SqlParticipantStoreExtension implements ServiceExtension {

    public static final String NAME = "SQL participants store";

    @Setting
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
