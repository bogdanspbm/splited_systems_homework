package objects;

import java.io.Serializable;

public record MessageSerializable(Object content) implements Serializable {
}
