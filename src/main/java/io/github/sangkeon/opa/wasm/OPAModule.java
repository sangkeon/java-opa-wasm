package io.github.sangkeon.opa.wasm;

import static io.github.kawamuray.wasmtime.WasmValType.I32;

import java.nio.ByteBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import io.github.kawamuray.wasmtime.Linker;

import io.github.kawamuray.wasmtime.Engine;

import io.github.kawamuray.wasmtime.Extern;
import io.github.kawamuray.wasmtime.Func;
import io.github.kawamuray.wasmtime.Memory;
import io.github.kawamuray.wasmtime.Disposable;
import io.github.kawamuray.wasmtime.MemoryType;
import io.github.kawamuray.wasmtime.Module;
import io.github.kawamuray.wasmtime.Store;
import io.github.kawamuray.wasmtime.WasmFunctions;
import io.github.kawamuray.wasmtime.wasi.Wasi;
import io.github.kawamuray.wasmtime.wasi.WasiConfig;
import io.github.kawamuray.wasmtime.wasi.WasiConfig.PreopenDir;

import org.json.JSONObject;

public class OPAModule implements Disposable {
    private Map<Integer, String> builtinFunc = new HashMap<>();
    private Map<String, Integer> entrypoints = new HashMap<>();

    private Store store;
    private Engine engine;
    private Linker linker;

    private OPAExportsAPI exports;

    private Func abort;
    private Func println;
    private Func builtin0;
    private Func builtin1;
    private Func builtin2;
    private Func builtin3;
    private Func builtin4;
    private Wasi wasi;
    private Memory memory;
    private Module module;

    private OPAAddr _dataAddr;
    private OPAAddr _baseHeapPtr;
    private OPAAddr _dataHeapPtr;

    public OPAModule(String filename) {
        store = new Store();
        engine = store.engine();
        module = Module.fromFile(engine, filename);

        linker = new Linker(store);

        initImports();

        wasi = new Wasi(store, new WasiConfig(new String[0], new PreopenDir[0]));
        wasi.addToLinker(linker);
        
        String modulename = "policy";

        linker.module(modulename, module);

        exports = OPAExports.getOPAExports(linker, modulename);

        _baseHeapPtr = exports.opaHeapPtrGet();
        _dataHeapPtr = _baseHeapPtr;
        _dataAddr = loadJson("{}");

        loadBuiltins();
    }

    public OPAModule(Bundle bundle) {
        store = new Store();
        engine = store.engine();
        module = Module.fromBinary(engine, bundle.getPolicy());

        linker = new Linker(store);

        initImports();

        wasi = new Wasi(store, new WasiConfig(new String[0], new PreopenDir[0]));
        wasi.addToLinker(linker);
        
        String modulename = "policy";

        linker.module(modulename, module);

        exports = OPAExports.getOPAExports(linker, modulename);

        _dataAddr = loadJson(bundle.getData());
        _baseHeapPtr = exports.opaHeapPtrGet();
        _dataHeapPtr = _baseHeapPtr;

        loadBuiltins();

        loadEntrypoints();
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

        // Retrieve the result
        OPAAddr resultAddr = exports.opaEvalCtxGetResult(ctxAddr);
        return dumpJson(resultAddr);
    }

    public void setData(String json) {
        exports.opaHeapPtrSet(_baseHeapPtr);
        _dataAddr = loadJson(json);
        _dataHeapPtr = exports.opaHeapPtrGet();
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

        ByteBuffer buf = memory.buffer();

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

    private static String decodeNullTerminatedString(Memory memory, OPAAddr addr) {
        int internalAddr = addr.getInternal();
        int end = internalAddr;

        ByteBuffer buf = memory.buffer();

        while(buf.get(end) != 0) {
            end++;
        }

        int size = end - internalAddr;

        byte[] result = new byte[size];

        for(int i = 0; i < size; i++) {
            result[i] = buf.get(internalAddr + i);
        }

        return new String(result);
    }

    private void initImports() {
        memory = new Memory(store, new MemoryType(new MemoryType.Limit(2)));

        abort = WasmFunctions.wrap(store, I32, (addr) -> {
        });

        println = WasmFunctions.wrap(store, I32, (addr) -> {
        });

        builtin0 = WasmFunctions.wrap(store, I32, I32, I32, 
            (builtinId, opaCtxReserved) -> {
                return 0;
            }
        );

        builtin1 = WasmFunctions.wrap(store, I32, I32, I32, I32, 
            (builtinId, opaCtxReserved, addr1) -> {
                String funcName = getFuncName(builtinId);

                if(funcName == null) {
                    throw new UnsupportedOperationException("builtin function builtinId=" + builtinId + " not supported");
                }

                String arg1 = dumpJson(OPAAddr.newAddr(addr1));

                switch (funcName) {
                    case "urlquery.encode":
                        String unquoted = arg1.substring(1, arg1.length() - 1);
                        String result = arg1.charAt(0) + java.net.URLEncoder.encode(unquoted)
                         + arg1.charAt(arg1.length()-1);
    
                        return loadJson(result).getInternal();
                    default:
                        break;
                }

                return 0;
            }
        );

        builtin2 = WasmFunctions.wrap(store, I32, I32, I32, I32, I32, 
            (builtinId, opaCtxReserved, addr1, addr2) -> {
                return 0;
            }
        );

        builtin3 = WasmFunctions.wrap(store, I32, I32, I32, I32, I32, I32, 
            (builtinId, opaCtxReserved, addr1, addr2, addr3) -> {
                return 0;
            }
        );

        builtin4 = WasmFunctions.wrap(store, I32, I32, I32,  I32, I32, I32, I32,
            (builtinId, opaCtxReserved, addr1, addr2, addr3, addr4) -> {
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

        linker.define(OPAConstants.MODULE, OPAConstants.OPA_ABORT, opaabort);
        linker.define(OPAConstants.MODULE, OPAConstants.OPA_BUILTIN0, opabuiltin0);
        linker.define(OPAConstants.MODULE, OPAConstants.OPA_BUILTIN1, opabuiltin1);
        linker.define(OPAConstants.MODULE, OPAConstants.OPA_BUILTIN2, opabuiltin2);
        linker.define(OPAConstants.MODULE, OPAConstants.OPA_BUILTIN3, opabuiltin3);
        linker.define(OPAConstants.MODULE, OPAConstants.OPA_BUILTIN4, opabuiltin4);
        linker.define(OPAConstants.MODULE, OPAConstants.OPA_PRINTLN, opaprintln);
        linker.define(OPAConstants.MODULE, OPAConstants.MEMORY, opamemory);
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
    
            if(engine != null) {
                engine.dispose();
                engine = null;
            }

            if(store != null) {
                store.dispose();
                store = null;
            }

            disposeImports();
        }
    }

}
