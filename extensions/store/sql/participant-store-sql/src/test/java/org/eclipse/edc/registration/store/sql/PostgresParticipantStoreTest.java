/*
 *  Copyright (c) 2023 Amadeus
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Amadeus - initial API and implementation
 *
 */

package org.eclipse.edc.registration.store.sql;

import org.eclipse.edc.junit.annotations.PostgresqlDbIntegrationTest;
import org.eclipse.edc.registration.spi.model.Participant;
import org.eclipse.edc.registration.store.spi.ParticipantStore;
import org.eclipse.edc.registration.store.spi.ParticipantStoreTestBase;
import org.eclipse.edc.registration.store.sql.schema.BaseSqlParticipantStatements;
import org.eclipse.edc.registration.store.sql.schema.PostgresSqlParticipantStatements;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.sql.testfixtures.PostgresqlStoreSetupExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.edc.registration.ParticipantUtils.createParticipant;
import static org.eclipse.edc.registration.spi.model.ParticipantStatus.AUTHORIZED;


@PostgresqlDbIntegrationTest
@ExtendWith(PostgresqlStoreSetupExtension.class)
class PostgresParticipantStoreTest extends ParticipantStoreTestBase {

    private SqlParticipantStore store;


    @BeforeEach
    void setUp(PostgresqlStoreSetupExtension extension) throws IOException {
        BaseSqlParticipantStatements statements = new PostgresSqlParticipantStatements();

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

        assertThatExceptionOfType(EdcPersistenceException.class).isThrownBy(() -> getStore().save(participant2))
                .withMessageStartingWith(String.format("Failed to update Participant with did %s", participant2.getDid()));
    }

    @AfterEach
    void tearDown(PostgresqlStoreSetupExtension extension) {
        var dialect = new PostgresSqlParticipantStatements();
        extension.runQuery("DROP TABLE " + dialect.getParticipantTable());
    }

    @Override
    protected ParticipantStore getStore() {
        return store;
    }
}
