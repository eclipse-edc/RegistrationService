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

package org.eclipse.dataspaceconnector.registration.store.sql.schema;

import org.eclipse.dataspaceconnector.sql.dialect.PostgresDialect;

/**
 * Extends {@link BaseSqlParticipantStatements} with custom dialect for PG
 */
public class PostgresSqlParticipantStatements extends BaseSqlParticipantStatements {

    @Override
    protected String getFormatJsonOperator() {
        return PostgresDialect.getJsonCastOperator();
    }

}
