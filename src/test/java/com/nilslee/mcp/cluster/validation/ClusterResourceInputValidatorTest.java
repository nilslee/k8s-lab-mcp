package com.nilslee.mcp.cluster.validation;

import com.nilslee.mcp.service.cluster.validation.ClusterResourceInputValidator;
import com.nilslee.mcp.service.cluster.validation.InvalidClusterInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClusterResourceInputValidatorTest {

    private final ClusterResourceInputValidator validator = new ClusterResourceInputValidator();

    // -------------------------------------------------------------------------
    // Namespace validation
    // -------------------------------------------------------------------------

    @Test
    void nullAndBlankNamespaceIsAccepted() {
        assertThatNoException().isThrownBy(() -> validator.validateNamespace(null));
        assertThatNoException().isThrownBy(() -> validator.validateNamespace(""));
        assertThatNoException().isThrownBy(() -> validator.validateNamespace("  "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"default", "kube-system", "my-ns", "ns1", "a"})
    void validNamespacesAreAccepted(String ns) {
        assertThatNoException().isThrownBy(() -> validator.validateNamespace(ns));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UPPERCASE",            // uppercase letters
            "-starts-with-dash",   // leading dash
            "ends-with-dash-",     // trailing dash
            "has.dot",             // dots not allowed in namespace
            "has spaces",          // spaces
            "toolongname012345678901234567890123456789012345678901234567890123"  // > 63 chars
    })
    void invalidNamespacesAreRejected(String ns) {
        assertThatThrownBy(() -> validator.validateNamespace(ns))
                .isInstanceOf(InvalidClusterInputException.class);
    }

    // -------------------------------------------------------------------------
    // Shell metacharacter rejection
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "ns; rm -rf /",
            "ns|cat /etc/passwd",
            "ns && echo pwned",
            "ns`whoami`",
            "ns$(id)",
            "ns${IFS}",
            "ns\ninjected",
            "ns\rinjected",
            "ns>output",
            "ns<input"
    })
    void shellMetacharactersAreRejected(String malicious) {
        assertThatThrownBy(() -> validator.validateNamespace(malicious))
                .isInstanceOf(InvalidClusterInputException.class)
                .hasMessageContaining("Illegal characters");
    }

    // -------------------------------------------------------------------------
    // Control character rejection
    // -------------------------------------------------------------------------

    @Test
    void controlCharactersAreRejected() {
        assertThatThrownBy(() -> validator.validateNamespace("ns\u0000null"))
                .isInstanceOf(InvalidClusterInputException.class);
        assertThatThrownBy(() -> validator.validateNamespace("ns\u001Bescape"))
                .isInstanceOf(InvalidClusterInputException.class);
    }

    // -------------------------------------------------------------------------
    // Resource name validation
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"nginx-deployment-7d6479d9f7-5kxqp", "my.pod", "pod1", "a"})
    void validResourceNamesAreAccepted(String name) {
        assertThatNoException().isThrownBy(() -> validator.validateResourceName(name, "podName"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-bad-start", "bad-end-", "UPPER"})
    void invalidResourceNamesAreRejected(String name) {
        assertThatThrownBy(() -> validator.validateResourceName(name, "podName"))
                .isInstanceOf(InvalidClusterInputException.class);
    }

    @Test
    void requiredResourceNameThrowsWhenBlank() {
        assertThatThrownBy(() -> validator.validateRequiredResourceName(null, "podName"))
                .isInstanceOf(InvalidClusterInputException.class)
                .hasMessageContaining("required");
        assertThatThrownBy(() -> validator.validateRequiredResourceName("", "podName"))
                .isInstanceOf(InvalidClusterInputException.class)
                .hasMessageContaining("required");
    }

    @Test
    void requiredNamespaceThrowsWhenBlank() {
        assertThatThrownBy(() -> validator.validateRequiredNamespace(null))
                .isInstanceOf(InvalidClusterInputException.class)
                .hasMessageContaining("required");
        assertThatThrownBy(() -> validator.validateRequiredNamespace("  "))
                .isInstanceOf(InvalidClusterInputException.class)
                .hasMessageContaining("required");
    }

    // -------------------------------------------------------------------------
    // Label selector validation
    // -------------------------------------------------------------------------

    @Test
    void nullAndBlankLabelSelectorIsAccepted() {
        assertThatNoException().isThrownBy(() -> validator.validateLabelSelector(null));
        assertThatNoException().isThrownBy(() -> validator.validateLabelSelector(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"app=nginx", "env=prod", "kubernetes.io/hostname=node1", "tier=frontend"})
    void validLabelSelectorsAreAccepted(String selector) {
        assertThatNoException().isThrownBy(() -> validator.validateLabelSelector(selector));
    }

    @Test
    void commaSeparatedSelectorIsRejected() {
        assertThatThrownBy(() -> validator.validateLabelSelector("app=nginx,env=prod"))
                .isInstanceOf(InvalidClusterInputException.class)
                .hasMessageContaining("comma");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "app=nginx; rm -rf /",
            "app|env",
            "app$(id)=value",
            "app=value\nnewline"
    })
    void maliciousLabelSelectorsAreRejected(String selector) {
        assertThatThrownBy(() -> validator.validateLabelSelector(selector))
                .isInstanceOf(InvalidClusterInputException.class);
    }

    @Test
    void labelSelectorWithoutEqualsIsRejected() {
        assertThatThrownBy(() -> validator.validateLabelSelector("justkey"))
                .isInstanceOf(InvalidClusterInputException.class);
    }
}
