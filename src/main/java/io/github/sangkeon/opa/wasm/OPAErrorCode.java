package io.github.sangkeon.opa.wasm;

public enum OPAErrorCode {
    OPA_ERR_OK(0), OPA_ERR_INTERNAL(1), OPA_ERR_INVALID_TYPE(2), OPA_ERR_INVALID_PATH(3);

    OPAErrorCode(int value) {
        this.value = value;
    }

    private final int value;

    public int value() {
        return value;
    }

    public String message() {
        switch (this.value) {
            case 1:
                return "internal error";
            case 2:
                return "invalid type";
            case 3:
                return "invalid path";
        }

        return "ok";
    }

    public static OPAErrorCode fromValue(int x) {
        switch (x) {
            case 0:
                return OPA_ERR_OK;
            case 1:
                return OPA_ERR_INTERNAL;
            case 2:
                return OPA_ERR_INVALID_TYPE;
            case 3:
                return OPA_ERR_INVALID_PATH;
        }
        return null;
    }
}
