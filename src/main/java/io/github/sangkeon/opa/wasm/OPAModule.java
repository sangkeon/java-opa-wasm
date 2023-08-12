package io.github.sangkeon.opa.wasm;

import static io.github.kawamuray.wasmtime.WasmValType.I32;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import io.github.kawamuray.wasmtime.Linker;

import io.github.kawamuray.wasmtime.Extern;
import io.github.kawamuray.wasmtime.Func;
import io.github.kawamuray.wasmtime.Memory;
import io.github.kawamuray.wasmtime.Disposable;
import io.github.kawamuray.wasmtime.MemoryType;
import io.github.kawamuray.wasmtime.Module;
import io.github.kawamuray.wasmtime.Store;
import io.github.kawamuray.wasmtime.WasmFunctions;
import io.github.kawamuray.wasmtime.wasi.WasiCtx;
import io.github.kawamuray.wasmtime.wasi.WasiCtxBuilder;

import org.json.JSONObject;

public class OPAModule implements Disposable {
    public static final String EMPTY_JSON = "{}";
    public static final String MODULE_NAME = "policy";
    private Map<Integer, String> builtinFunc = new HashMap<>();
    private Map<String, Integer> entrypoints = new HashMap<>();

    private Store<Void> store;
    private Linker linker;
    private WasiCtx wasi;

    private OPAExportsAPI exports;

    private Func abort;
    private Func println;
    private Func builtin0;
    private Func builtin1;
    private Func builtin2;
    private Func builtin3;
    private Func builtin4;
    private Memory memory;
    private Module module;

    private OPAAddr _dataAddr;
    private OPAAddr _baseHeapPtr;
    private OPAAddr _dataHeapPtr;
    private OPAAddr _heapPtr;

    public OPAModule(String filename) {
        this(filename, EMPTY_JSON);
    }

    public OPAModule(String filename, String json) {
        wasi = new WasiCtxBuilder().inheritStdout().inheritStderr().build();
        store = Store.withoutData();
        linker = new Linker(store.engine());
        module = Module.fromFile(store.engine(), filename);

        initImports();

        WasiCtx.addToLinker(linker);
        
        linker.module(store, MODULE_NAME, module);

        exports = OPAExports.getOPAExports(linker, MODULE_NAME, store);

        loadBuiltins();
        loadEntrypoints();

        _dataAddr = loadJson(json);
        _baseHeapPtr = exports.opaHeapPtrGet();
        _dataHeapPtr = _baseHeapPtr.copy();
        _heapPtr = _baseHeapPtr.copy();
    }

    public OPAModule(Bundle bundle) {
        wasi = new WasiCtxBuilder().inheritStdout().inheritStderr().build();
        store = Store.withoutData(wasi);
        linker = new Linker(store.engine());
        module = Module.fromBinary(store.engine(), bundle.getPolicy());

        initImports();

        WasiCtx.addToLinker(linker);
        
        linker.module(store, MODULE_NAME, module);

        exports = OPAExports.getOPAExports(linker, MODULE_NAME, store);

        loadBuiltins();
        loadEntrypoints();

        _dataAddr = loadJson(bundle.getData());
        _baseHeapPtr = exports.opaHeapPtrGet();
        _dataHeapPtr = _baseHeapPtr.copy();
        _heapPtr = _baseHeapPtr.copy();
    }

    public Map<String, Integer> getEntrypoints() {
        return entrypoints;
    }

    public void loadEntrypoints() {
        entrypoints.clear();

        OPAAddr entrypointsAddr = exports.entrypoints();

        if(!entrypointsAddr.isNull()) {
            String jsonString = dumpJson(entrypointsAddr);

            JSONObject jObject = new JSONObject(jsonString);

            Iterator<String> iter = jObject.keys();

            while(iter.hasNext()) {
                String key = iter.next();
                int val = jObject.getInt(key);

                entrypoints.put(key, val);
            }
        }
    }

    public void loadBuiltins() {
        builtinFunc.clear();

        OPAAddr builtinaddr = exports.builtins();

        if(!builtinaddr.isNull()) {
            String jsonString = dumpJson(builtinaddr);

            JSONObject jObject = new JSONObject(jsonString);

            Iterator<String> iter = jObject.keys();

            while(iter.hasNext()) {
                String key = iter.next();
                int val = jObject.getInt(key);

                builtinFunc.put(val, key);
            }
        }
    }

    public String getFuncName(int id) {
        return builtinFunc.get(id);
    }

    public void dispose() {
        dispose(true);
    }

    public String readStringFromOPAMemory(OPAAddr addr) {
        return decodeNullTerminatedString(memory, addr);
    }

    public String evaluate(String json) {
        // Evaluate with default entrypoint
        return evaluate(json, 0);
    }

    public String evaluate(String json, String entrypoint) {
        if(entrypoints.containsKey(entrypoint)) {
            return evaluate(json, entrypoints.get(entrypoint));
        }

        throw new RuntimeException(String.format("entrypoint %s is not valid", entrypoint));
    }
    
    public String evaluate(String json, int entrypoint) {
        _heapPtr = _dataHeapPtr.copy();

        if(exports.isFastPathEvalSupported()) {
            return evaluateFastPath(json, entrypoint);
        } else {
            return evaluateNormalPath(json, entrypoint);
        }
    }

    private String evaluateNormalPath(String json, int entrypoint) {
        // Reset the heap pointer before each evaluation
        exports.opaHeapPtrSet(_dataHeapPtr);

        // Load the input data
        OPAAddr inputAddr = loadJson(json);

        // Setup the evaluation context
        OPAAddr ctxAddr = exports.opaEvalCtxNew();
        exports.opaEvalCtxSetInput(ctxAddr, inputAddr);
        exports.opaEvalCtxSetData(ctxAddr, _dataAddr);
        exports.opaEvalCtxSetEntryPoint(ctxAddr, entrypoint);

        // Actually evaluate the policy
        OPAErrorCode err = exports.eval(ctxAddr);

        if(err != OPAErrorCode.OPA_ERR_OK) {
            throw new RuntimeException(String.format("evaluate error: %s", err.message())); 
        }

        // Retrieve the result
        OPAAddr resultAddr = exports.opaEvalCtxGetResult(ctxAddr);
        return dumpJson(resultAddr);
    }

    private String evaluateFastPath(String json, int entrypoint) {
        byte[] jsonBytes = json.getBytes();
        int size = jsonBytes.length;

        ByteBuffer buf = memory.buffer(store);
        OPAAddr inputAddr = _heapPtr.copy();
        for(int i = 0; i < size; i++) {
            buf.put(inputAddr.getInternal() + i, jsonBytes[i]);
        }

        this._heapPtr = OPAAddr.newAddr(inputAddr.getInternal() + size);

        OPAAddr resultAddr = exports.opaEval(OPAAddr.newAddr(0), entrypoint, this._dataAddr,
                inputAddr, jsonBytes.length, _heapPtr ,0);

        return decodeNullTerminatedString(memory, resultAddr);
    }

    public void setData(String json) {
        exports.opaHeapPtrSet(_baseHeapPtr);
        _dataAddr = loadJson(json);
        _dataHeapPtr = exports.opaHeapPtrGet();
        _heapPtr = _dataHeapPtr.copy();
    }

    public OPAAddr loadJson(String json) {
        OPAAddr addr = writeString(memory, json);

        OPAAddr parseAddr = exports.opaJsonParse(addr, json.length());

        if (parseAddr.isNull()) {
            throw new NullPointerException("Parsing failed");
        }

        return parseAddr;
    }

    private OPAAddr writeString(Memory memory, String string) {
        byte[] stringBytes = string.getBytes();

        OPAAddr addr = exports.opaMalloc(stringBytes.length);

        ByteBuffer buf = memory.buffer(store);

        int internalAddr = addr.getInternal();

        for(int i = 0; i < stringBytes.length; i++ ) {
            buf.put(internalAddr + i, stringBytes[i]);
        }

        return addr;
    }

    private String dumpJson(OPAAddr addrResult) {
        OPAAddr addr = exports.opaJsonDump(addrResult);
        return decodeNullTerminatedString(memory, addr);
    }

    private String decodeNullTerminatedString(Memory memory, OPAAddr addr) {
        int internalAddr = addr.getInternal();
        int end = internalAddr;

        ByteBuffer buf = memory.buffer(store);

        while(buf.get(end) != 0) {
            end++;
        }

        int size = end - internalAddr;

        if (size == 0) {
            return "";
        }

        byte[] result = new byte[size];

        for(int i = 0; i < size; i++) {
            result[i] = buf.get(internalAddr + i);
        }

        return new String(result);
    }

    private void initImports() {
        memory = new Memory(store, new MemoryType(5L, false));

        abort = WasmFunctions.wrap(store, I32, (addr) -> {
            throw new RuntimeException(readStringFromOPAMemory(OPAAddr.newAddr(addr)));
        });

        println = WasmFunctions.wrap(store, I32, (addr) -> {
            System.out.println(readStringFromOPAMemory(OPAAddr.newAddr(addr)));
        });

        builtin0 = WasmFunctions.wrap(store, I32, I32, I32, 
            (builtinId, opaCtxReserved) -> {
                String funcName = getFuncName(builtinId);

                checkBuiltinFunctionExists(builtinId, funcName);

                unsupportedFunction(builtinId, funcName);

                return 0;
            }
        );

        builtin1 = WasmFunctions.wrap(store, I32, I32, I32, I32, 
            (builtinId, opaCtxReserved, addr1) -> {
                String funcName = getFuncName(builtinId);

                checkBuiltinFunctionExists(builtinId, funcName);

                String arg1 = dumpJson(OPAAddr.newAddr(addr1));

                switch (funcName) {
                    case "urlquery.encode":
                        String unquoted = arg1.substring(1, arg1.length() - 1);
                        try {
                            String result = arg1.charAt(0) + java.net.URLEncoder.encode(unquoted, "UTF-8")
                                + arg1.charAt(arg1.length()-1);
    
                            return loadJson(result).getInternal();
                        } catch(UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    default:
                        unsupportedFunction(builtinId, funcName);
                }
                return 0;
            }
        );

        builtin2 = WasmFunctions.wrap(store, I32, I32, I32, I32, I32, 
            (builtinId, opaCtxReserved, addr1, addr2) -> {
                String funcName = getFuncName(builtinId);

                checkBuiltinFunctionExists(builtinId, funcName);

                unsupportedFunction(builtinId, funcName);
                return 0;
            }
        );

        builtin3 = WasmFunctions.wrap(store, I32, I32, I32, I32, I32, I32, 
            (builtinId, opaCtxReserved, addr1, addr2, addr3) -> {
                String funcName = getFuncName(builtinId);

                checkBuiltinFunctionExists(builtinId, funcName);

                unsupportedFunction(builtinId, funcName);

                return 0;
            }
        );

        builtin4 = WasmFunctions.wrap(store, I32, I32, I32, I32, I32, I32, I32,
            (builtinId, opaCtxReserved, addr1, addr2, addr3, addr4) -> {
                String funcName = getFuncName(builtinId);

                checkBuiltinFunctionExists(builtinId, funcName);

                unsupportedFunction(builtinId, funcName);

                return 0;
            }
        );

        Extern opaabort = Extern.fromFunc(abort);
        Extern opabuiltin0 = Extern.fromFunc(builtin0);
        Extern opabuiltin1 = Extern.fromFunc(builtin1);
        Extern opabuiltin2 = Extern.fromFunc(builtin2);
        Extern opabuiltin3 = Extern.fromFunc(builtin3);
        Extern opabuiltin4 = Extern.fromFunc(builtin4);
        Extern opaprintln = Extern.fromFunc(println);
        Extern opamemory = Extern.fromMemory(memory);

        linker.define(store, OPAConstants.MODULE, OPAConstants.OPA_ABORT, opaabort);
        linker.define(store, OPAConstants.MODULE, OPAConstants.OPA_BUILTIN0, opabuiltin0);
        linker.define(store, OPAConstants.MODULE, OPAConstants.OPA_BUILTIN1, opabuiltin1);
        linker.define(store, OPAConstants.MODULE, OPAConstants.OPA_BUILTIN2, opabuiltin2);
        linker.define(store, OPAConstants.MODULE, OPAConstants.OPA_BUILTIN3, opabuiltin3);
        linker.define(store, OPAConstants.MODULE, OPAConstants.OPA_BUILTIN4, opabuiltin4);
        linker.define(store, OPAConstants.MODULE, OPAConstants.OPA_PRINTLN, opaprintln);
        linker.define(store, OPAConstants.MODULE, OPAConstants.MEMORY, opamemory);
    }

    private static void checkBuiltinFunctionExists(Integer builtinId, String funcName) {
        if(funcName == null) {
            throw new UnsupportedOperationException("builtin function builtinId=" + builtinId + " not supported");
        }
    }

    private static void unsupportedFunction(Integer builtinId, String funcName) {
        throw new UnsupportedOperationException("builtin function '" + funcName + "', builtinId="
                + builtinId + " not supported");
    }

    private void disposeImports() {
        if(memory != null) {
            memory.dispose();
            memory = null;
        }

        if(builtin0 != null) {
            builtin0.dispose();
            builtin0 = null;
        }

        if(builtin1 != null) {
            builtin1.dispose();
            builtin1 = null;
        }

        if(builtin2 != null) {
            builtin2.dispose();
            builtin2 = null;
        }

        if(builtin3 != null) {
            builtin3.dispose();
            builtin3 = null;
        }

        if(builtin4 != null) {
            builtin4.dispose();
            builtin4 = null;
        }
        
        if(println != null) {
            println.dispose();
            println = null;
        }

        if(abort != null) {
            abort.dispose();
            abort = null;
        }
    }

    protected void dispose(boolean disposing) {
        if (disposing) {
            if(module != null) {
                module.dispose();
                module = null;
            }

            if(wasi != null) {
                wasi.dispose();
                wasi = null;
            }

            if(linker != null) {
                linker.dispose();
                linker = null;
            }

            if(store != null) {
                store.dispose();
                store = null;
            }

            disposeImports();
        }
    }

    public Integer getAbiMajorVersion() {
        return exports.getAbiMajorVersion();
    }

    public Integer getAbiMinorVersion() {
        return exports.getAbiMinorVersion();
    }
}
