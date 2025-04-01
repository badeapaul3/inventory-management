import org.mockito.Mockito;

import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author Paul Badea
 **/
public class SimpleExample {
    public static void main(String[] args) {

        // Mock a StringBuilder
        StringBuilder mockSb = Mockito.mock(StringBuilder.class);

        // Stub append
        when(mockSb.append("Hi")).thenReturn(mockSb);
        when(mockSb.toString()).thenReturn("Hi there");

        // Use it
        mockSb.append("Hi");
        System.out.println(mockSb.toString()); // Should print "Hi there"

        // Verify
        Mockito.verify(mockSb).append("Hi");

    }
}
