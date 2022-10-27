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

package org.eclips.dataspaceconnector.registration.store.sql;

import org.eclipse.dataspaceconnector.common.util.junit.annotations.PostgresqlDbIntegrationTest;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStoreTestBase;
import org.eclipse.dataspaceconnector.registration.store.sql.SqlParticipantStore;
import org.eclipse.dataspaceconnector.registration.store.sql.schema.BaseSqlParticipantStatements;
import org.eclipse.dataspaceconnector.registration.store.sql.schema.PostgresSqlParticipantStatements;
import org.eclipse.dataspaceconnector.spi.persistence.EdcPersistenceException;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;
import org.eclipse.dataspaceconnector.sql.PostgresqlStoreSetupExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.dataspaceconnector.registration.authority.TestUtils.createParticipant;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZED;


@PostgresqlDbIntegrationTest
@ExtendWith(PostgresqlStoreSetupExtension.class)
public class PostgresParticipantStoreTest extends ParticipantStoreTestBase {

    private SqlParticipantStore store;

    private BaseSqlParticipantStatements statements;


    @BeforeEach
    void setUp(PostgresqlStoreSetupExtension extension) throws SQLException, IOException {
        statements = new PostgresSqlParticipantStatements();

        TypeManager manager = new TypeManager();
        manager.registerTypes(Participant.class);

        store = new SqlParticipantStore(extension.getDataSourceRegistry(), extension.getDatasourceName(), extension.getTransactionContext(), manager.getMapper(), statements);

        var schema = Files.readString(Paths.get("docs/schema.sql"));
        extension.runQuery(schema);
    }

    @Test
    void saveAndListParticipants_removesDuplicates() {

        Participant participant1 = createParticipant().did("some.test/url/2").status(AUTHORIZED).build();
        Participant participant2 = createParticipant().did("some.test/url/2").status(AUTHORIZED).build();
        getStore().save(participant1);

        assertThatThrownBy(() -> getStore().save(participant2))
                .isInstanceOf(EdcPersistenceException.class)
                .hasMessageStartingWith(String.format("Failed to update Participant with did %s", participant2.getDid()));
    }

    @AfterEach
    void tearDown(PostgresqlStoreSetupExtension extension) throws Exception {
        var dialect = new PostgresSqlParticipantStatements();
        extension.runQuery("DROP TABLE " + dialect.getParticipantTable());
    }

    @Override
    protected ParticipantStore getStore() {
        return store;
    }
}
