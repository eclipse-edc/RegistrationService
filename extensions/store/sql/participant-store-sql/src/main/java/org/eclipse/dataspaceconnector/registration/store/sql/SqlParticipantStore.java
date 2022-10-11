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

import com.fasterxml.jackson.core.type.TypeReference;
import org.eclipse.dataspaceconnector.registration.authority.model.Participant;
import org.eclipse.dataspaceconnector.registration.authority.model.ParticipantStatus;
import org.eclipse.dataspaceconnector.registration.store.spi.ParticipantStore;
import org.eclipse.dataspaceconnector.registration.store.sql.schema.ParticipantStatements;
import org.eclipse.dataspaceconnector.spi.persistence.EdcPersistenceException;
import org.eclipse.dataspaceconnector.spi.transaction.TransactionContext;
import org.eclipse.dataspaceconnector.spi.transaction.datasource.DataSourceRegistry;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import static java.lang.String.format;
import static org.eclipse.dataspaceconnector.sql.SqlQueryExecutor.executeQuery;
import static org.eclipse.dataspaceconnector.sql.SqlQueryExecutor.executeQuerySingle;

/**
 * SQL implementation for {@link ParticipantStore}
 */

public class SqlParticipantStore implements ParticipantStore {

    private final DataSourceRegistry dataSourceRegistry;
    private final String dataSourceName;
    private final TransactionContext transactionContext;


    private final ParticipantStatements participantStatements;
    private final TypeManager typeManager;


    public SqlParticipantStore(DataSourceRegistry dataSourceRegistry, String dataSourceName, TransactionContext transactionContext, TypeManager typeManager, ParticipantStatements participantStatements) {
        this.dataSourceRegistry = Objects.requireNonNull(dataSourceRegistry);
        this.dataSourceName = Objects.requireNonNull(dataSourceName);
        this.transactionContext = Objects.requireNonNull(transactionContext);
        this.typeManager = typeManager;
        this.participantStatements = participantStatements;
    }

    @Override
    public @Nullable Participant findByDid(String did) {
        Objects.requireNonNull(did);
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                return executeQuerySingle(connection, true, this::participantMapper, participantStatements.getSelectParticipantByDidTemplate(), did);
            } catch (Exception e) {
                if (e instanceof EdcPersistenceException) {
                    throw (EdcPersistenceException) e;
                } else {
                    throw new EdcPersistenceException(e.getMessage(), e);
                }
            }
        });

    }

    @Override
    public List<Participant> listParticipants() {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                try (var stream = executeQuery(connection, true, this::participantMapper, participantStatements.getSelectParticipantTemplate())) {
                    return stream.collect(Collectors.toList());
                }
            } catch (Exception e) {
                if (e instanceof EdcPersistenceException) {
                    throw (EdcPersistenceException) e;
                } else {
                    throw new EdcPersistenceException(e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public void save(Participant participant) {
        var existingParticipant = findByDid(participant.getDid());
        if (existingParticipant == null) {
            insert(participant);
        } else {
            update(existingParticipant, participant);
        }
    }

    @Override
    public Collection<Participant> listParticipantsWithStatus(ParticipantStatus state) {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                try (var stream = executeQuery(connection, true, this::participantMapper, participantStatements.getSelectParticipantByStateTemplate(), state.code())) {
                    return stream.collect(Collectors.toList());
                }
            } catch (Exception e) {
                if (e instanceof EdcPersistenceException) {
                    throw (EdcPersistenceException) e;
                } else {
                    throw new EdcPersistenceException(e.getMessage(), e);
                }
            }
        });
    }

    public void update(Participant oldParticipant, Participant participant) {

        if (!oldParticipant.getId().equals(participant.getId())) {
            throw new EdcPersistenceException(format("Failed to update Participant with did %s: participant id didn't match", participant.getDid()));
        }
        transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                executeQuery(connection, participantStatements.getUpdateParticipantTemplate(),
                        participant.getState(),
                        participant.getStateCount(),
                        participant.getStateTimestamp(),
                        participant.getErrorDetail(),
                        toJson(participant.getTraceContext()),
                        participant.getUpdatedAt(),
                        participant.getDid()
                );


            } catch (Exception e) {
                if (e instanceof EdcPersistenceException) {
                    throw (EdcPersistenceException) e;
                } else {
                    throw new EdcPersistenceException(e.getMessage(), e);
                }
            }
        });
    }

    protected Participant participantMapper(ResultSet resultSet) throws SQLException {
        return Participant.Builder.newInstance()
                .did(resultSet.getString(participantStatements.getDidColumn()))
                .id(resultSet.getString(participantStatements.getParticipantIdColumn()))
                .traceContext(fromJson(resultSet.getString(participantStatements.getTraceContextColumn()), new TypeReference<>() {
                }))
                .createdAt(resultSet.getLong(participantStatements.getCreatedAtColumn()))
                .updatedAt(resultSet.getLong(participantStatements.getUpdatedAtColumn()))
                .stateTimestamp(resultSet.getLong(participantStatements.getStateTimestampColumn()))
                .state(resultSet.getInt(participantStatements.getStateColumn()))
                .build();
    }

    protected void insert(Participant participant) {

        transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                executeQuery(connection, participantStatements.getInsertParticipantsTemplate(),
                        participant.getId(),
                        participant.getDid(),
                        participant.getState(),
                        participant.getStateCount(),
                        participant.getStateTimestamp(),
                        participant.getErrorDetail(),
                        toJson(participant.getTraceContext()),
                        participant.getCreatedAt(),
                        participant.getUpdatedAt()
                );

            } catch (Exception e) {
                if (e instanceof EdcPersistenceException) {
                    throw (EdcPersistenceException) e;
                } else {
                    throw new EdcPersistenceException(e.getMessage(), e);
                }
            }
        });

    }

    private String toJson(Object object) {
        return typeManager.writeValueAsString(object);
    }

    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        return typeManager.readValue(json, typeReference);
    }

    private DataSource getDataSource() {
        return Objects.requireNonNull(dataSourceRegistry.resolve(dataSourceName), format("DataSource %s could not be resolved", dataSourceName));
    }

    private Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}
