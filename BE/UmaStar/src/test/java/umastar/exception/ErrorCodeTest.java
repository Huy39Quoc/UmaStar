package umastar.exception;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCodeTest {

    @Test
    void allNumericCodesAreUnique() {
        Set<Integer> seen = new HashSet<>();
        for (ErrorCode ec : ErrorCode.values()) {
            assertTrue(seen.add(ec.getCode()),
                    "Duplicate ErrorCode numeric code: " + ec.getCode() + " on " + ec.name());
        }
    }
}

