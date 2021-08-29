package io.github.sangkeon.opa.wasm;

public interface OPAExportsAPI {
    public OPAErrorCode eval(OPAAddr ctxAddr);

    public OPAAddr builtins();

    public OPAAddr entrypoints();

    public OPAAddr opaEvalCtxNew();

    public void opaEvalCtxSetInput(OPAAddr ctxAddr, OPAAddr inputAddr);

    public void opaEvalCtxSetData(OPAAddr ctxAddr, OPAAddr dataAddr);

    public void opaEvalCtxSetEntryPoint(OPAAddr ctxAddr, int entrypoint_id);

    public OPAAddr opaEvalCtxGetResult(OPAAddr ctxAddr);

    public OPAAddr opaMalloc(int bytes);

    public void opaFree(OPAAddr addr);

    public OPAAddr opaJsonParse(OPAAddr addr, int jsonLength);

    public OPAAddr opaValueParse(OPAAddr addr, int jsonLength);

    public OPAAddr opaJsonDump(OPAAddr valueAddr);

    public OPAAddr opaValueDump(OPAAddr valueAddr);

    public void opaHeapPtrSet(OPAAddr addr);

    public OPAAddr opaHeapPtrGet();

    public OPAErrorCode opaValueAddPath(OPAAddr baseValueAddr, OPAAddr pathValueAddr, OPAAddr valueAddr);

    public OPAErrorCode opaValueRemovePath(OPAAddr baseValueAddr, OPAAddr pathValueAddr);

    public OPAAddr opaEval(OPAAddr reservedAddr, int entrypoint_id,  OPAAddr valueAddr, OPAAddr strAddr, int length, OPAAddr heapAddr, int format);

    public boolean isFastPathEvalSupported();
}