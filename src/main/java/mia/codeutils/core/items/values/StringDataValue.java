package mia.codeutils.core.items.values;

import java.util.Optional;

public class StringDataValue extends DataValue {
    public StringDataValue(Optional<String> value) {
        super(value);
    }

    public String getValue() {
        return (String) super.getValue();
    }
}