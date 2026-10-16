package com.ironpath.advisor;

/** Retained route/effort metadata. These bands do not rank encounter equipment. */
public final class AcquisitionScope
{
 public enum Mode { READY, BOUNDED, REPEATABLE, LONG_TAIL, UNKNOWN }
 public enum Burden { LOW, MODERATE, HIGH, UNKNOWN }
 public enum Tail { ORDINARY, LONG, UNKNOWN }
 private AcquisitionScope() { }
}
