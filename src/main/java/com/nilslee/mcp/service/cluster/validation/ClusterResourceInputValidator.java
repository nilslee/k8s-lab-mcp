package com.nilslee.mcp.service.cluster.validation;

import com.nilslee.mcp.exception.InvalidClusterInputException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validates all model-supplied inputs before they reach the Kubernetes API.
 *
 * Defense strategy:
 * - Shell metacharacters are rejected even though we never shell out (defense-in-depth).
 * - Kubernetes naming rules (RFC 1123) are enforced so invalid names are caught early.
 * - Label selectors only accept a single {@code key=value} token; commas and complex
 *   expressions are rejected to minimise the injection surface.
 */
@Component
public class ClusterResourceInputValidator {

    /**
     * Shell metacharacters and control characters that must never reach any API call,
     * even indirectly. Includes the full C0 control range, typical shell operators,
     * and command-substitution patterns.
     */
    private static final Pattern SHELL_METACHAR = Pattern.compile(
            "[\u0000-\u001F\u007F;|&`><!\n\r]|\\$\\(|\\$\\{");

    /**
     * RFC 1123 DNS label – used for namespace names.
     * Max 63 characters, lowercase, digits, hyphens; must start and end with alphanumeric.
     */
    private static final Pattern DNS_LABEL = Pattern.compile(
            "^[a-z0-9]([a-z0-9\\-]{0,61}[a-z0-9])?$");

    /**
     * RFC 1123 DNS subdomain – used for pod, deployment, service names.
     * Max 253 characters, lowercase, digits, hyphens and dots; start/end alphanumeric.
     */
    private static final Pattern DNS_SUBDOMAIN = Pattern.compile(
            "^[a-z0-9]([a-z0-9\\-\\.]{0,251}[a-z0-9])?$");

    /**
     * Single key=value label selector. Commas (multi-selector), spaces, and
     * other complex expressions are rejected.
     */
    private static final Pattern LABEL_SELECTOR = Pattern.compile(
            "^[a-zA-Z0-9][a-zA-Z0-9/_\\-\\.]*=[a-zA-Z0-9_\\-\\./]*$");

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Validates an optional namespace parameter.
     * Null or blank is accepted (means "all namespaces").
     *
     * @param namespace candidate namespace or {@code null}/blank for all
     */
    public void validateNamespace(String namespace) {
        if (isBlankOrNull(namespace)) {
            return;
        }
        rejectShellMetachars(namespace, "namespace");
        if (!DNS_LABEL.matcher(namespace).matches()) {
            throw new InvalidClusterInputException(
                    "Invalid namespace name '" + namespace + "': must be a DNS label (lowercase alphanumeric and hyphens, max 63 chars).");
        }
    }

    /**
     * Validates a required namespace parameter.
     *
     * @param namespace must be non-blank and a valid DNS label
     */
    public void validateRequiredNamespace(String namespace) {
        if (isBlankOrNull(namespace)) {
            throw new InvalidClusterInputException("namespace is required.");
        }
        validateNamespace(namespace);
    }

    /**
     * Validates an optional resource name (pod, deployment, service, …).
     *
     * @param name      candidate name or {@code null}/blank to skip validation
     * @param fieldName API field name for error messages
     */
    public void validateResourceName(String name, String fieldName) {
        if (isBlankOrNull(name)) {
            return;
        }
        rejectShellMetachars(name, fieldName);
        if (!DNS_SUBDOMAIN.matcher(name).matches()) {
            throw new InvalidClusterInputException(
                    "Invalid " + fieldName + " '" + name + "': must be a DNS subdomain (lowercase alphanumeric, hyphens and dots, max 253 chars).");
        }
    }

    /**
     * Validates a required resource name.
     *
     * @param name      must be non-blank and a valid DNS subdomain
     * @param fieldName API field name for error messages
     */
    public void validateRequiredResourceName(String name, String fieldName) {
        if (isBlankOrNull(name)) {
            throw new InvalidClusterInputException(fieldName + " is required.");
        }
        validateResourceName(name, fieldName);
    }

    /**
     * Validates an optional single-token label selector ({@code key=value}).
     * Commas and any complex expression are rejected.
     *
     * @param selector {@code null}, blank, or exactly one {@code key=value} token
     */
    public void validateLabelSelector(String selector) {
        if (isBlankOrNull(selector)) {
            return;
        }
        rejectShellMetachars(selector, "labelSelector");
        if (selector.contains(",")) {
            throw new InvalidClusterInputException(
                    "labelSelector must be a single key=value pair; comma-separated multi-selectors are not supported.");
        }
        if (!LABEL_SELECTOR.matcher(selector).matches()) {
            throw new InvalidClusterInputException(
                    "Invalid labelSelector '" + selector + "': use key=value format with alphanumeric, hyphens, underscores, dots, and slashes.");
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void rejectShellMetachars(String value, String fieldName) {
        if (SHELL_METACHAR.matcher(value).find()) {
            throw new InvalidClusterInputException(
                    "Illegal characters detected in " + fieldName + ": shell metacharacters and control characters are not allowed.");
        }
    }

    private boolean isBlankOrNull(String value) {
        return value == null || value.isBlank();
    }
}
