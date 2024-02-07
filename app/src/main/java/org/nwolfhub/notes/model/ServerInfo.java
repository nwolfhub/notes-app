package org.nwolfhub.notes.model;

public class ServerInfo {
    public String version;
    public String address;
    public String name;

    public ServerInfo() {}

    public ServerInfo(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public ServerInfo(String version, String address, String name) {
        this.version = version;
        this.address = address;
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public ServerInfo setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public ServerInfo setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getName() {
        return name;
    }

    public ServerInfo setName(String name) {
        this.name = name;
        return this;
    }
}
