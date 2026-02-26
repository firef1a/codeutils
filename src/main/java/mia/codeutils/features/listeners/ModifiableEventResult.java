package mia.codeutils.features.listeners;


public class ModifiableEventResult<T> {
    private final ModifiableEventResultType eventResultType;
    private final T modifiedData;

    public ModifiableEventResult(ModifiableEventResultType eventResultType, T modifiedData) {
        this.eventResultType = eventResultType;
        this.modifiedData = modifiedData;
    }

    public ModifiableEventData<T> eventResult(T base, T modifiedData) {
        if (eventResultType.equals(ModifiableEventResultType.PASS)) return new ModifiableEventData<>(base, modifiedData);
        else return new ModifiableEventData<>(base, this.modifiedData);
    }
}
