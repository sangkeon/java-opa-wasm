# OPA(Open Policy Agent) WebAssembly SDK for Java

## About
OPA(www.openpolicyagent.org) ia a awesome policy engine for Cloud Native Application and Micro Services.

This project is a very sample-grade sdk to illustrate how to use wasm compiled rego policy for Java application.

This project uses https://github.com/kawamuray/wasmtime-java for WASM runtime.

Inspired by and, borrowed many ideas+codes from following projects. 
- golang-opa-wasm(https://github.com/open-policy-agent/golang-opa-wasm)
- npm-opa-wasm(https://github.com/open-policy-agent/npm-opa-wasm)
- dotnet-opa-wasm(https://github.com/christophwille/dotnet-opa-wasm)

Tested under OPA version 0.26.0

Warning: Very early stage project and OPA's env module requirements not implemented(just place holder).

## Usage

### Maven dependency ###
```
    <dependency>
        <groupId>io.github.sangkeon</groupId>
        <artifactId>java-opa-wasm</artifactId>
        <version>0.2.1</version>
    </dependency>
```

### To load and evaluate for OPA wasm file 
```
try (
    OPAModule om = new OPAModule("./sample-policy/wasm/policy.wasm");
) {
    String input = "{\"user\": \"john\"}";
    String data = "{\"role\":{\"john\":\"admin\"}}";

    om.setData(data);

    String result = om.evaluate(input, "opa/wasm/test/allowed");

    System.out.println("result=" + result);
}
```

### To load and evaluate for OPA bundle 
When using bundle, policy.wasm and data.json inside bundle will be loaded.

```
try {
    Bundle bundle = BundleUtil.extractBundle("./sample-policy/bundle/bundle.tar.gz");

    try (
        OPAModule om = new OPAModule(bundle);
    ) {
        String input = "{\"user\": \"alice\"}";
        String result = om.evaluate(input, "opa/wasm/test/allowed");

        System.out.println("result=" + result);
    }
} catch(Exception e) {
}

```
