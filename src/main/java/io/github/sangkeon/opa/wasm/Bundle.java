package io.github.sangkeon.opa.wasm;

public class Bundle {
    private byte[] policy;
    private String data;

    public Bundle() {     
    }

    public Bundle(byte[] policy, String data) {
        this.policy = policy;
        this.data = data;
    }

    public byte[] getPolicy() {
        return policy;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setPolicy(byte[] policy) {
        this.policy = policy;
    }
}
