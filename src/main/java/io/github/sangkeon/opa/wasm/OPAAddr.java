package io.github.sangkeon.opa.wasm;

public class OPAAddr {
    private final int addr;

    private OPAAddr(int addr) {
        this.addr = addr;
    }

    public static OPAAddr newAddr(int addr) {
        return new OPAAddr(addr);
    }

    public int getInternal() {
        return addr;
    }

    public boolean isNull() {
        return addr == 0;
    }
}