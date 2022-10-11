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
import org.eclipse.dataspaceconnector.spi.transaction.NoopTransactionContext;
import org.eclipse.dataspaceconnector.spi.transaction.TransactionContext;
import org.eclipse.dataspaceconnector.spi.transaction.datasource.DataSourceRegistry;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;
import org.eclipse.dataspaceconnector.sql.PostgresqlLocalInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.dataspaceconnector.registration.authority.TestUtils.createParticipant;
import static org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus.AUTHORIZED;
import static org.eclipse.dataspaceconnector.sql.SqlQueryExecutor.executeQuery;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@PostgresqlDbIntegrationTest
public class PostgresParticipantStoreTest extends ParticipantStoreTestBase {

    protected static final String DATASOURCE_NAME = "participant";
    private static final String POSTGRES_USER = "postgres";
    private static final String POSTGRES_PASSWORD = "password";
    private static final String POSTGRES_DATABASE = "itest";

    private TransactionContext transactionContext;
    private Connection connection;
    private SqlParticipantStore store;

    private BaseSqlParticipantStatements statements;

    @BeforeAll
    static void prepare() {
        PostgresqlLocalInstance.createDatabase(POSTGRES_DATABASE);
    }

    @BeforeEach
    void setUp() throws SQLException, IOException {
        transactionContext = new NoopTransactionContext();
        DataSourceRegistry dataSourceRegistry = mock(DataSourceRegistry.class);


        statements = new PostgresSqlParticipantStatements();

        var ds = new PGSimpleDataSource();
        ds.setServerNames(new String[]{ "localhost" });
        ds.setPortNumbers(new int[]{ 5432 });
        ds.setUser(POSTGRES_USER);
        ds.setPassword(POSTGRES_PASSWORD);
        ds.setDatabaseName(POSTGRES_DATABASE);

        // do not actually close
        connection = spy(ds.getConnection());
        doNothing().when(connection).close();

        TypeManager manager = new TypeManager();


        var datasourceMock = mock(DataSource.class);
        when(datasourceMock.getConnection()).thenReturn(connection);
        when(dataSourceRegistry.resolve(DATASOURCE_NAME)).thenReturn(datasourceMock);


        store = new SqlParticipantStore(dataSourceRegistry, DATASOURCE_NAME, transactionContext, manager, statements);

        var schema = Files.readString(Paths.get("docs/schema.sql"));
        try {
            transactionContext.execute(() -> {
                executeQuery(connection, schema);
                return null;
            });
        } catch (Exception exc) {
            fail(exc);
        }
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
    void tearDown() throws Exception {

        transactionContext.execute(() -> {
            var dialect = new PostgresSqlParticipantStatements();
            executeQuery(connection, "DROP TABLE " + dialect.getParticipantTable());
        });
        doCallRealMethod().when(connection).close();
        connection.close();
    }

    @Override
    protected ParticipantStore getStore() {
        return store;
    }
}
