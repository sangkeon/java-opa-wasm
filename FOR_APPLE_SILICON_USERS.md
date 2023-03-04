# Warning
java-opa-wasm currently does not support Apple Silicon chips. This is because the wasmtime-java doesn't support Apple Silicon chips(https://github.com/kawamuray/wasmtime-java/issues/36).So if you are using Apple Silicon macs, building the wasmtime-java on your mac is required.

# Requirement
- Latest Rust Compiler (nightly channel)
- Java 8 or 11

# Instruction
```
% rustc --version
rustc 1.69.0-nightly (44cfafe2f 2023-03-03)
% javac --version
javac 11.0.18

# Get wasmtime-java source.
% git clone https://github.com/kawamuray/wasmtime-java

# Switch to the version required by java-opa-wasm.
% cd wasmtime-java
% git checkout v0.9.0 -b v0.9.0

# Build and install wasmtime-java to the Maven local repository.
% ./gradlew publishToMavenLocal

# Go to the location of java-opa-wasm directory.
% cd (...)/java-opa-wasm

# Check java-opa-wasm working.
% mvn test
```
