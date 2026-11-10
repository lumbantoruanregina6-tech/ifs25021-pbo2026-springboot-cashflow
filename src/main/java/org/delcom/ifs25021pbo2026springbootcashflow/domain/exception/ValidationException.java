package org.delcom.ifs25021pbo2026springbootcashflow.domain.exception;

/** Dilempar saat validasi aturan bisnis atau input domain gagal. */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}
