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

package org.eclipse.dataspaceconnector.registration.cli;

import org.eclipse.dataspaceconnector.registration.client.TestKeyData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CryptoUtilsTest {

    @Test
    void parseFromPemEncodedObjects_succeeds() {
        var keyPair = CryptoUtils.parseFromPemEncodedObjects(TestKeyData.PRIVATE_KEY_P256);
        assertThat(keyPair.signer()).isNotNull();
    }

    @Test
    void parseFromPemEncodedObjects_onMissingPrivateKey_fails() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> CryptoUtils.parseFromPemEncodedObjects(TestKeyData.PUBLIC_KEY_P256))
                .withMessageContaining("Missing private key");
    }

    @Test
    void parseFromPemEncodedObjects_onUnsupportedKeyType_fails() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> CryptoUtils.parseFromPemEncodedObjects(TestKeyData.PRIVATE_KEY_RSA))
                .withMessageContaining("Unsupported key type: RSA");
    }

    @Test
    void parseFromPemEncodedObjects_onKeyNotParsed_fails() {
        var garbledPrivateKeyData = TestKeyData.PRIVATE_KEY_P256.replace('=', 'x');
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> CryptoUtils.parseFromPemEncodedObjects(garbledPrivateKeyData))
                .withMessageContaining("Key parsing failed");
    }
}