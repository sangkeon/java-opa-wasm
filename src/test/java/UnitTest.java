import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import io.github.sangkeon.opa.wasm.*;

public class UnitTest {
    @Test
    public void bundleTest1() throws Exception {
        try {
            Bundle bundle = BundleUtil.extractBundle("./sample-policy/bundle/bundle.tar.gz");

            try (
                OPAModule om = new OPAModule(bundle);
            ) {
                String input = "{\"user\": \"alice\"}";
                String output = om.evaluate(input, "opa/wasm/test/allowed");

                assertEquals("[{\"result\":true}]", output);
           }

        } catch(Exception e) {
            fail();
        }
    }

    @Test
    public void bundleTest2() throws Exception {
        try {
            Bundle bundle = BundleUtil.extractBundle("./sample-policy/bundle/bundle.tar.gz");

            try (
                OPAModule om = new OPAModule(bundle);
            ) {
                String input = "{\"user\": \"bob\"}";
                String output = om.evaluate(input, "opa/wasm/test/allowed");

                assertEquals("[{\"result\":false}]", output);
           }

        } catch(Exception e) {
            fail();
        }
    }

    @Test
    public void wasmTest1() throws Exception {
        try (
            OPAModule om = new OPAModule("./sample-policy/wasm/policy.wasm");
        ) {
            String input = "{\"user\": \"john\"}";
            String data = "{\"role\":{\"john\":\"admin\"}}";

            om.setData(data);
            String output = om.evaluate(input);

            assertEquals("[{\"result\":true}]", output);
        }
    }

    @Test
    public void wasmTest2() throws Exception {
        try (
            OPAModule om = new OPAModule("./sample-policy/wasm/policy.wasm");
        ) {
            String input = "{\"user\": \"john\"}";
            String data = "{\"role\":{\"john\":\"user\"}}";

            om.setData(data);
            String output = om.evaluate(input);

            assertEquals("[{\"result\":false}]", output);
        }
    }
}