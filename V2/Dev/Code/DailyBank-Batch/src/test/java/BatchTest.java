import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BatchTest {

    @Test
    void monTest() {
        // Arrange
        int expected = 2;

        // Act
        int actual = 1 + 1;

        // Assert
        assertEquals(expected, actual, "1 + 1 devrait être égal à 2");
    }
}
