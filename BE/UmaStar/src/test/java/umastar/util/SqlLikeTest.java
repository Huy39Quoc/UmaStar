package umastar.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlLikeTest {

    @Test
    void containsWrapsWithPercentAndEscapesWildcardsAndBackslash() {
        assertEquals("%abc%", SqlLike.contains("AbC"));
        assertEquals("%100\\%\\_a\\\\b%", SqlLike.contains("100%_A\\b"));
    }
}

