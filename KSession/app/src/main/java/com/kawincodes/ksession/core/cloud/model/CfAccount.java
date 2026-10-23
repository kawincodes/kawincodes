package com.kawincodes.ksession.core.cloud.model;

/** A Cloudflare account. */
public final class CfAccount {
    public final String id;
    public final String name;

    public CfAccount(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
