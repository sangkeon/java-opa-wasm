package io.github.sangkeon.opa.wasm;

import static io.github.kawamuray.wasmtime.WasmValType.I32;

import java.util.Optional;

import io.github.kawamuray.wasmtime.*;

public class OPAExports implements OPAExportsAPI, Disposable {
    private Linker linker;
    private Store<Void> store;

    private Func opaMallocFn = null;
    private Func opaHeapPtrGetFn = null;
    private Func opaHeapPtrSetFn = null;
    private Func opaJsonDumpFn = null;
    private Func opaJsonParseFn = null;
    private Func opaEvalCtxNewFn = null;
    private Func opaEvalCtxSetInputFn = null;
    private Func opaEvalCtxSetDataFn = null;
    private Func opaEvalCtxGetResultFn = null;
    private Func builtinsFn = null;
    private Func evalFn = null;
    private Func entrypointsFn = null;
    private Func opaEvalCtxSetEntryPointFn = null;
    private Func opaFreeFn = null;
    private Func opaValueParseFn = null;
    private Func opaValueDumpFn = null;
    private Func opaValueAddPathFn = null;
    private Func opaValueRemovePathFn = null;
    private Func opaEvalFn = null;
    private Integer abiMajorVersion = null;
    private Integer abiMinorVersion = null;

    private OPAExports(Linker linker, String moduleName, Store<Void> store) {
        this.linker = linker;
        this.store = store;

        initFns(moduleName);
    }

    public static OPAExportsAPI getOPAExports(Linker linker, String moduleName, Store<Void> store) {
        return new OPAExports(linker, moduleName, store);
    }

    public void initFns(String moduleName) {
        opaMallocFn = linker.get(store, moduleName, OPAConstants.OPA_MALLOC).get().func();
        opaHeapPtrGetFn = linker.get(store, moduleName, OPAConstants.OPA_HEAP_PTR_GET).get().func();
        opaHeapPtrSetFn = linker.get(store, moduleName, OPAConstants.OPA_HEAP_PTR_SET).get().func();
        opaJsonDumpFn = linker.get(store, moduleName, OPAConstants.OPA_JSON_DUMP).get().func();
        opaJsonParseFn = linker.get(store, moduleName, OPAConstants.OPA_JSON_PARSE).get().func();
        opaEvalCtxNewFn = linker.get(store, moduleName, OPAConstants.OPA_EVAL_CTX_NEW).get().func();
        opaEvalCtxSetInputFn = linker.get(store, moduleName, OPAConstants.OPA_EVAL_CTX_SET_INPUT).get().func();
        opaEvalCtxSetDataFn = linker.get(store, moduleName, OPAConstants.OPA_EVAL_CTX_SET_DATA).get().func();
        opaEvalCtxGetResultFn = linker.get(store, moduleName, OPAConstants.OPA_EVAL_CTX_GET_RESULT).get().func();
        builtinsFn = linker.get(store, moduleName, OPAConstants.BUILTINS).get().func();
        evalFn = linker.get(store, moduleName, OPAConstants.EVAL).get().func();
        entrypointsFn = linker.get(store, moduleName, OPAConstants.ENTRYPOINTS).get().func();
        opaEvalCtxSetEntryPointFn = linker.get(store, moduleName, OPAConstants.OPA_EVAL_CTX_SET_ENTRYPOINT).get().func();
        opaFreeFn = linker.get(store, moduleName, OPAConstants.OPA_FREE).get().func();
        opaValueParseFn = linker.get(store, moduleName, OPAConstants.OPA_VALUE_PARSE).get().func();
        opaValueDumpFn = linker.get(store, moduleName, OPAConstants.OPA_VALUE_DUMP).get().func();
        opaValueAddPathFn = linker.get(store, moduleName, OPAConstants.OPA_VALUE_ADD_PATH).get().func();
        opaValueRemovePathFn = linker.get(store, moduleName, OPAConstants.OPA_VALUE_REMOVE_PATH).get().func();

        Optional<Extern> opaEvalExtern = linker.get(store, moduleName, OPAConstants.OPA_EVAL);

        if(opaEvalExtern.isPresent()) {
            opaEvalFn = opaEvalExtern.get().func();
        }

        if(linker.get(store, moduleName, OPAConstants.OPA_WASM_ABI_VERSION).isPresent()) {
            Global majorVersion = linker.get(store, moduleName, OPAConstants.OPA_WASM_ABI_VERSION).get().global();

            this.abiMajorVersion = majorVersion.get(store).i32();

            majorVersion.dispose();
        }

        if(linker.get(store, moduleName, OPAConstants.OPA_WASM_ABI_MINOR_VERSION).isPresent()) {
            Global minorVersion = linker.get(store, moduleName, OPAConstants.OPA_WASM_ABI_MINOR_VERSION).get().global();

            this.abiMinorVersion = minorVersion.get(store).i32();

            minorVersion.dispose();
        }
    }

    public void disposeFns() {
        if(opaMallocFn != null) {
            opaMallocFn.dispose();
            opaMallocFn = null;
        }

        if(opaHeapPtrGetFn != null) {
            opaHeapPtrGetFn.dispose();
            opaHeapPtrGetFn = null;
        }

        if(opaHeapPtrSetFn != null) {
            opaHeapPtrSetFn.dispose();
            opaHeapPtrSetFn = null;
        }

        if(opaJsonDumpFn != null) {
            opaJsonDumpFn.dispose();
            opaJsonDumpFn = null;
        }

        if(opaJsonParseFn != null) {
            opaJsonParseFn.dispose();
            opaJsonParseFn = null;
        }

        if(opaEvalCtxNewFn != null) {
            opaEvalCtxNewFn.dispose();
            opaEvalCtxNewFn = null;
        }

        if(opaEvalCtxSetInputFn != null) {
            opaEvalCtxSetInputFn.dispose();
            opaEvalCtxSetInputFn = null;
        }

        if(opaEvalCtxSetDataFn != null) {
            opaEvalCtxSetDataFn.dispose();
            opaEvalCtxSetDataFn = null;
        }

        if(opaEvalCtxGetResultFn != null) {
            opaEvalCtxGetResultFn.dispose();
            opaEvalCtxGetResultFn = null;
        }

        if(builtinsFn != null) {
            builtinsFn.dispose();
            builtinsFn = null;
        }

        if(evalFn != null) {
            evalFn.dispose();
            evalFn = null;
        }

        if(entrypointsFn != null) {
            entrypointsFn.dispose();
            entrypointsFn = null;
        }

        if(opaEvalCtxSetEntryPointFn != null) {
            opaEvalCtxSetEntryPointFn.dispose();
            opaEvalCtxSetEntryPointFn = null;
        }

        if(opaFreeFn != null) {
            opaFreeFn.dispose();
            opaFreeFn = null;
        }

        if(opaValueParseFn != null) {
            opaValueParseFn.dispose();
            opaValueParseFn = null;
        }

        if(opaValueDumpFn != null) {
            opaValueDumpFn.dispose();
            opaValueDumpFn = null;
        }

        if(opaValueAddPathFn != null) {
            opaValueAddPathFn.dispose();
            opaValueAddPathFn = null;
        }

        if(opaValueRemovePathFn != null) {
            opaValueRemovePathFn.dispose();
            opaValueRemovePathFn = null;
        }

        if(opaEvalFn != null) {
            opaEvalFn.dispose();
            opaEvalFn = null;
        }
    }

    public void dispose() {
        disposeFns();
    }

    @Override
    public OPAAddr opaMalloc(int bytes) {
        WasmFunctions.Function1<Integer, Integer> opa_malloc = WasmFunctions.func(store, opaMallocFn, I32, I32);
        int addr  = opa_malloc.call(bytes);

        return OPAAddr.newAddr(addr);
    }

    @Override
    public OPAAddr opaHeapPtrGet() {
        WasmFunctions.Function0<Integer> opa_heap_ptr_get = WasmFunctions.func(store, opaHeapPtrGetFn, I32);
        int addr = opa_heap_ptr_get.call();

        return OPAAddr.newAddr(addr);
    }

    @Override
    public void opaHeapPtrSet(OPAAddr addr) {
        WasmFunctions.Consumer1<Integer> opa_heap_ptr_set = WasmFunctions.consumer(store, opaHeapPtrSetFn, I32);
        opa_heap_ptr_set.accept(addr.getInternal());
    }

    @Override
    public OPAAddr opaJsonDump(OPAAddr valueAddr) {
        WasmFunctions.Function1<Integer, Integer> opa_json_dump = WasmFunctions.func(store, opaJsonDumpFn, I32, I32);
        int strAddr  = opa_json_dump.call(valueAddr.getInternal());

        return OPAAddr.newAddr(strAddr);
    }

    @Override
    public OPAAddr opaJsonParse(OPAAddr addr, int jsonLength) {
        WasmFunctions.Function2<Integer, Integer, Integer> opa_json_parse = WasmFunctions.func(store, opaJsonParseFn, I32, I32, I32);
        int valueAddr = opa_json_parse.call(addr.getInternal(), jsonLength);

        return OPAAddr.newAddr(valueAddr);
    }

    @Override
    public OPAAddr opaEvalCtxNew() {
        WasmFunctions.Function0<Integer> opa_eval_ctx_new = WasmFunctions.func(store, opaEvalCtxNewFn, I32);
        int ctxAddr = opa_eval_ctx_new.call();

        return OPAAddr.newAddr(ctxAddr);
    }

    @Override
    public void opaEvalCtxSetInput(OPAAddr ctxAddr, OPAAddr inputAddr) {
        WasmFunctions.Consumer2<Integer, Integer> opa_eval_ctx_set_input = WasmFunctions.consumer(store, opaEvalCtxSetInputFn, I32, I32);
        opa_eval_ctx_set_input.accept(ctxAddr.getInternal(), inputAddr.getInternal());
    }

    @Override
    public void opaEvalCtxSetData(OPAAddr ctxAddr, OPAAddr dataAddr) {
        WasmFunctions.Consumer2<Integer, Integer> opa_eval_ctx_set_data = WasmFunctions.consumer(store, opaEvalCtxSetDataFn, I32, I32);
        opa_eval_ctx_set_data.accept(ctxAddr.getInternal(), dataAddr.getInternal());
    }

    @Override
    public OPAAddr opaEvalCtxGetResult(OPAAddr ctxAddr) {
        WasmFunctions.Function1<Integer, Integer> opa_eval_ctx_get_result = WasmFunctions.func(store, opaEvalCtxGetResultFn, I32, I32);
        int valueAddr =  opa_eval_ctx_get_result.call(ctxAddr.getInternal());

        return OPAAddr.newAddr(valueAddr);
    }

    @Override
    public OPAAddr builtins() {
        WasmFunctions.Function0<Integer> builtins = WasmFunctions.func(store, builtinsFn, I32);
        int valueAddr = builtins.call();

        return OPAAddr.newAddr(valueAddr);
    }

    @Override
    public OPAErrorCode eval(OPAAddr ctxAddr) {
        WasmFunctions.Function1<Integer, Integer> eval = WasmFunctions.func(store, evalFn, I32, I32);
        int errorCode = eval.call(ctxAddr.getInternal());

        return OPAErrorCode.fromValue(errorCode);
    }

    @Override
    public OPAAddr entrypoints() {
        WasmFunctions.Function0<Integer> entrypoints = WasmFunctions.func(store, entrypointsFn, I32);
        int valueAddr = entrypoints.call();

        return OPAAddr.newAddr(valueAddr);
    }

    @Override
    public void opaEvalCtxSetEntryPoint(OPAAddr ctxAddr, int entrypoint_id) {
        WasmFunctions.Consumer2<Integer, Integer> opa_eval_ctx_set_entrypoint = WasmFunctions.consumer(store, opaEvalCtxSetEntryPointFn, I32, I32);
        opa_eval_ctx_set_entrypoint.accept(ctxAddr.getInternal(), entrypoint_id);
    }

    @Override
    public void opaFree(OPAAddr addr) {
        WasmFunctions.Consumer1<Integer> opa_free = WasmFunctions.consumer(store, opaFreeFn, I32);
        opa_free.accept(addr.getInternal());
    }

    @Override
    public OPAAddr opaValueParse(OPAAddr addr, int jsonLength) {
        WasmFunctions.Function2<Integer, Integer, Integer> opa_value_parse = WasmFunctions.func(store, opaValueParseFn, I32, I32, I32);
        int valueAddr = opa_value_parse.call(addr.getInternal(), jsonLength);

        return OPAAddr.newAddr(valueAddr);
    }

    @Override
    public OPAAddr opaValueDump(OPAAddr valueAddr) {
        WasmFunctions.Function1<Integer, Integer> opa_value_dump = WasmFunctions.func(store, opaValueDumpFn, I32, I32);
        int strAddr  = opa_value_dump.call(valueAddr.getInternal());

        return OPAAddr.newAddr(strAddr);
    }

    @Override
    public OPAErrorCode opaValueAddPath(OPAAddr baseValueAddr, OPAAddr pathValueAddr, OPAAddr valueAddr) {
        WasmFunctions.Function3<Integer, Integer, Integer, Integer> opa_value_add_path = WasmFunctions.func(store, opaValueAddPathFn, I32, I32, I32, I32);
        int errorCode = opa_value_add_path.call(baseValueAddr.getInternal(), pathValueAddr.getInternal(), valueAddr.getInternal());

        return OPAErrorCode.fromValue(errorCode);
    }

    @Override
    public OPAErrorCode opaValueRemovePath(OPAAddr baseValueAddr, OPAAddr pathValueAddr) {
        WasmFunctions.Function2<Integer, Integer, Integer> opa_value_remove_path = WasmFunctions.func(store, opaValueRemovePathFn, I32, I32, I32);
        int errorCode = opa_value_remove_path.call(baseValueAddr.getInternal(), pathValueAddr.getInternal());

        return OPAErrorCode.fromValue(errorCode);
    }

    @Override
    public OPAAddr opaEval(OPAAddr reservedAddr, int entrypoint_id,  OPAAddr valueAddr, OPAAddr strAddr, int length, OPAAddr heapAddr, int format) {
        if(opaEvalFn == null) {
            throw new UnsupportedOperationException("opa_eval not supported, may be compiled using unsupported ABI(<1.2)");
        }

        WasmFunctions.Function7<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> opa_eval = WasmFunctions.func(store, opaEvalFn, I32, I32, I32, I32, I32, I32, I32, I32);

        int resultStrAddr = opa_eval.call(reservedAddr.getInternal(), entrypoint_id, valueAddr.getInternal(), strAddr.getInternal(), length, heapAddr.getInternal(), format);
        
        return OPAAddr.newAddr(resultStrAddr);
    }

    @Override
    public boolean isFastPathEvalSupported() {
        return (opaEvalFn != null);
    }

    @Override
    public Integer getAbiMajorVersion() { return abiMajorVersion; }

    @Override
    public Integer getAbiMinorVersion() { return abiMinorVersion; }
}
