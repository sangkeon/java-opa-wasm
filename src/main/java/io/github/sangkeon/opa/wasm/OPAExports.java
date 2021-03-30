package io.github.sangkeon.opa.wasm;

import static io.github.kawamuray.wasmtime.WasmValType.I32;

import io.github.kawamuray.wasmtime.Linker;

import io.github.kawamuray.wasmtime.Func;
import io.github.kawamuray.wasmtime.Disposable;
import io.github.kawamuray.wasmtime.WasmFunctions;

public class OPAExports implements OPAExportsAPI, Disposable {
    private Linker linker;

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

    private OPAExports(Linker linker, String moduleName) {
        this.linker = linker;

        initFns(moduleName);
    }

    public static OPAExportsAPI getOPAExports(Linker linker, String moduleName) {
        return new OPAExports(linker, moduleName);
    }

    public void initFns(String moduleName) {
        opaMallocFn = linker.getOneByName(moduleName, OPAConstants.OPA_MALLOC).func();
        opaHeapPtrGetFn = linker.getOneByName(moduleName, OPAConstants.OPA_HEAP_PTR_GET).func();
        opaHeapPtrSetFn = linker.getOneByName(moduleName, OPAConstants.OPA_HEAP_PTR_SET).func();
        opaJsonDumpFn = linker.getOneByName(moduleName, OPAConstants.OPA_JSON_DUMP).func();
        opaJsonParseFn = linker.getOneByName(moduleName, OPAConstants.OPA_JSON_PARSE).func();
        opaEvalCtxNewFn = linker.getOneByName(moduleName, OPAConstants.OPA_EVAL_CTX_NEW).func();
        opaEvalCtxSetInputFn = linker.getOneByName(moduleName, OPAConstants.OPA_EVAL_CTX_SET_INPUT).func();
        opaEvalCtxSetDataFn = linker.getOneByName(moduleName, OPAConstants.OPA_EVAL_CTX_SET_DATA).func();
        opaEvalCtxGetResultFn = linker.getOneByName(moduleName, OPAConstants.OPA_EVAL_CTX_GET_RESULT).func();
        builtinsFn = linker.getOneByName(moduleName, OPAConstants.BUILTINS).func();
        evalFn = linker.getOneByName(moduleName, OPAConstants.EVAL).func();
        entrypointsFn = linker.getOneByName(moduleName, OPAConstants.ENTRYPOINTS).func();
        opaEvalCtxSetEntryPointFn = linker.getOneByName(moduleName, OPAConstants.OPA_EVAL_CTX_SET_ENTRYPOINT).func();
        opaFreeFn = linker.getOneByName(moduleName, OPAConstants.OPA_FREE).func();
        opaValueParseFn = linker.getOneByName(moduleName, OPAConstants.OPA_VALUE_PARSE).func();
        opaValueDumpFn = linker.getOneByName(moduleName, OPAConstants.OPA_VALUE_DUMP).func();
        opaValueAddPathFn = linker.getOneByName(moduleName, OPAConstants.OPA_VALUE_ADD_PATH).func();
        opaValueRemovePathFn = linker.getOneByName(moduleName, OPAConstants.OPA_VALUE_REMOVE_PATH).func();
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
    }

    public void dispose() {
        disposeFns();
    }

    @Override
    public OPAAddr opaMalloc(int bytes) {
        WasmFunctions.Function1<Integer, Integer> opa_malloc = WasmFunctions.func(opaMallocFn, I32, I32);
        int addr  = opa_malloc.call(bytes);

        return OPAAddr.newAddr(addr);
    }

    @Override
    public OPAAddr opaHeapPtrGet() {
        WasmFunctions.Function0<Integer> opa_heap_ptr_get = WasmFunctions.func(opaHeapPtrGetFn, I32);
        int addr = opa_heap_ptr_get.call();

        return  OPAAddr.newAddr(addr);
    }

    @Override
    public void opaHeapPtrSet(OPAAddr addr) {
        WasmFunctions.Consumer1<Integer> opa_heap_ptr_set = WasmFunctions.consumer(opaHeapPtrSetFn, I32);
        opa_heap_ptr_set.accept(addr.getInternal());
    }

    @Override
    public OPAAddr opaJsonDump(OPAAddr valueAddr) {
        WasmFunctions.Function1<Integer, Integer> opa_json_dump = WasmFunctions.func(opaJsonDumpFn, I32, I32);
        int strAddr  = opa_json_dump.call(valueAddr.getInternal());

        return OPAAddr.newAddr(strAddr);
    }

    @Override
    public OPAAddr opaJsonParse(OPAAddr addr, int jsonLength) {
        WasmFunctions.Function2<Integer, Integer, Integer> opa_json_parse = WasmFunctions.func(opaJsonParseFn, I32, I32, I32);
        int valueAddr = opa_json_parse.call(addr.getInternal(), jsonLength);

        return OPAAddr.newAddr(valueAddr);
    }

    @Override
    public OPAAddr opaEvalCtxNew() {
        WasmFunctions.Function0<Integer> opa_eval_ctx_new = WasmFunctions.func(opaEvalCtxNewFn, I32);
        int ctxAddr = opa_eval_ctx_new.call();

        return OPAAddr.newAddr(ctxAddr);
    }

    @Override
    public void opaEvalCtxSetInput(OPAAddr ctxAddr, OPAAddr inputAddr) {
        WasmFunctions.Consumer2<Integer, Integer> opa_eval_ctx_set_input = WasmFunctions.consumer(opaEvalCtxSetInputFn, I32, I32);
        opa_eval_ctx_set_input.accept(ctxAddr.getInternal(), inputAddr.getInternal());
    }

    @Override
    public void opaEvalCtxSetData(OPAAddr ctxAddr, OPAAddr dataAddr) {
        WasmFunctions.Consumer2<Integer, Integer> opa_eval_ctx_set_data = WasmFunctions.consumer(opaEvalCtxSetDataFn, I32, I32);
        opa_eval_ctx_set_data.accept(ctxAddr.getInternal(), dataAddr.getInternal());
    }

    @Override
    public OPAAddr opaEvalCtxGetResult(OPAAddr ctxAddr) {
        WasmFunctions.Function1<Integer, Integer> opa_eval_ctx_get_result = WasmFunctions.func(opaEvalCtxGetResultFn, I32, I32);
        int valueAddr =  opa_eval_ctx_get_result.call(ctxAddr.getInternal());

        return OPAAddr.newAddr(valueAddr);
    }

    @Override
    public OPAAddr builtins() {
        WasmFunctions.Function0<Integer> builtins = WasmFunctions.func(builtinsFn, I32);
        int valueAddr = builtins.call();

        return OPAAddr.newAddr(valueAddr);
    }

    @Override
    public OPAErrorCode eval(OPAAddr ctxAddr) {
        WasmFunctions.Function1<Integer, Integer> eval = WasmFunctions.func(evalFn, I32, I32);
        int errorCode = eval.call(ctxAddr.getInternal());

        return OPAErrorCode.fromValue(errorCode);
    }

    @Override
    public OPAAddr entrypoints() {
        WasmFunctions.Function0<Integer> entrypoints = WasmFunctions.func(entrypointsFn, I32);
        int valueAddr = entrypoints.call();

        return OPAAddr.newAddr(valueAddr);
    }

    @Override
    public void opaEvalCtxSetEntryPoint(OPAAddr ctxAddr, int entrypoint_id) {
        WasmFunctions.Consumer2<Integer, Integer> opa_eval_ctx_set_entrypoint = WasmFunctions.consumer(opaEvalCtxSetEntryPointFn, I32, I32);
        opa_eval_ctx_set_entrypoint.accept(ctxAddr.getInternal(), entrypoint_id);
    }

    @Override
    public void opaFree(OPAAddr addr) {
        WasmFunctions.Consumer1<Integer> opa_free = WasmFunctions.consumer(opaFreeFn, I32);
        opa_free.accept(addr.getInternal());
    }

    @Override
    public OPAAddr opaValueParse(OPAAddr addr, int jsonLength) {
        WasmFunctions.Function2<Integer, Integer, Integer> opa_value_parse = WasmFunctions.func(opaValueParseFn, I32, I32, I32);
        int valueAddr = opa_value_parse.call(addr.getInternal(), jsonLength);

        return OPAAddr.newAddr(valueAddr);
    }

    @Override
    public OPAAddr opaValueDump(OPAAddr valueAddr) {
        WasmFunctions.Function1<Integer, Integer> opa_value_dump = WasmFunctions.func(opaValueDumpFn, I32, I32);
        int strAddr  = opa_value_dump.call(valueAddr.getInternal());

        return OPAAddr.newAddr(strAddr);
    }

    @Override
    public OPAErrorCode opaValueAddPath(OPAAddr baseValueAddr, OPAAddr pathValueAddr, OPAAddr valueAddr) {
        WasmFunctions.Function3<Integer, Integer, Integer, Integer> opa_value_add_path = WasmFunctions.func(opaValueAddPathFn, I32, I32, I32, I32);
        int errorCode = opa_value_add_path.call(baseValueAddr.getInternal(), pathValueAddr.getInternal(), valueAddr.getInternal());

        return OPAErrorCode.fromValue(errorCode);
    }

    @Override
    public OPAErrorCode opaValueRemovePath(OPAAddr baseValueAddr, OPAAddr pathValueAddr) {
        WasmFunctions.Function2<Integer, Integer, Integer> opa_value_remove_path = WasmFunctions.func(opaValueRemovePathFn, I32, I32, I32);
        int errorCode = opa_value_remove_path.call(baseValueAddr.getInternal(), pathValueAddr.getInternal());

        return OPAErrorCode.fromValue(errorCode);
    }
}
