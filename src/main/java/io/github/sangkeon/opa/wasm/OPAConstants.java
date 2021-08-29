package io.github.sangkeon.opa.wasm;

public class OPAConstants {
    // For OPA WASM imports
    public static final String MODULE = "env";
    public static final String MEMORY = "memory";

    public static final String OPA_ABORT = "opa_abort";
    public static final String OPA_BUILTIN0 = "opa_builtin0";
    public static final String OPA_BUILTIN1 = "opa_builtin1";
    public static final String OPA_BUILTIN2 = "opa_builtin2";
    public static final String OPA_BUILTIN3 = "opa_builtin3";
    public static final String OPA_BUILTIN4 = "opa_builtin4";
    public static final String OPA_PRINTLN = "opa_println";

    // For OPA WASM exports
    public static final String OPA_MALLOC = "opa_malloc";
    public static final String OPA_HEAP_PTR_GET = "opa_heap_ptr_get";
    public static final String OPA_HEAP_PTR_SET = "opa_heap_ptr_set";
    public static final String OPA_JSON_DUMP = "opa_json_dump";
    public static final String OPA_JSON_PARSE = "opa_json_parse";
    public static final String OPA_EVAL_CTX_NEW = "opa_eval_ctx_new";
    public static final String OPA_EVAL_CTX_SET_INPUT = "opa_eval_ctx_set_input";
    public static final String OPA_EVAL_CTX_SET_DATA = "opa_eval_ctx_set_data";
    public static final String OPA_EVAL_CTX_GET_RESULT = "opa_eval_ctx_get_result";
    public static final String BUILTINS = "builtins";
    public static final String EVAL = "eval";
    public static final String ENTRYPOINTS = "entrypoints";
    public static final String OPA_EVAL_CTX_SET_ENTRYPOINT = "opa_eval_ctx_set_entrypoint";
    public static final String OPA_FREE = "opa_free";
    public static final String OPA_VALUE_PARSE = "opa_value_parse";
    public static final String OPA_VALUE_DUMP = "opa_value_dump";
    public static final String OPA_VALUE_ADD_PATH = "opa_value_add_path";
    public static final String OPA_VALUE_REMOVE_PATH = "opa_value_remove_path";

    public static final String OPA_EVAL = "opa_eval";

    public static final String OPA_WASM_ABI_VERSION = "opa_wasm_abi_version";
    public static final String OPA_WASM_ABI_MINOR_VERSION = "opa_wasm_abi_minor_version";
}
