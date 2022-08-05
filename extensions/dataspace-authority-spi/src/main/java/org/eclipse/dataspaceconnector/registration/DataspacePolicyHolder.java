package org.eclipse.dataspaceconnector.registration;


import org.eclipse.dataspaceconnector.policy.model.Policy;

public class DataspacePolicyHolder {
    private final Policy dataspacePolicy;

    public DataspacePolicyHolder(Policy dataspacePolicy) {
        this.dataspacePolicy = dataspacePolicy;
    }

    public Policy get() {
        return dataspacePolicy;
    }
}
