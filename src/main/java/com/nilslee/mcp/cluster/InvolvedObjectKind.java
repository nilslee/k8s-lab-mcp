package com.nilslee.mcp.cluster;

/**
 * Typed enum for event involved-object kind to prevent prompt-injection via free-text kind strings.
 */
public enum InvolvedObjectKind {
    Pod,
    Deployment,
    ReplicaSet,
    StatefulSet,
    DaemonSet,
    Job,
    CronJob,
    Service,
    Node,
    PersistentVolumeClaim,
    ConfigMap,
    Secret
}
