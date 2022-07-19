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

package org.eclipse.dataspaceconnector.registration.client;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TestKeyData {
    private TestKeyData() {
    }

    public static final String PRIVATE_KEY_P256;
    public static final String PUBLIC_KEY_P256;
    public static final String PRIVATE_KEY_RSA;

    static {
        PRIVATE_KEY_P256 = readResource("private_p256.pem");
        PRIVATE_KEY_RSA = readResource("private_rsa.pem");
        PUBLIC_KEY_P256 = readResource("public_p256.pem");
    }

    @NotNull
    private static String readResource(String name) {
        try {
            return new String(TestKeyData.class.getClassLoader().getResourceAsStream(name).readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error loading test key data", e);
        }
    }
}