package mia.codeutils.features.listeners;

/*
    this is so very specific listeners can build modified results (chat messages)
 */
public record ModifiableEventData<T>(T base, T modified) {
    public ModifiableEventResult<T> pass() { return  new ModifiableEventResult<>(ModifiableEventResultType.PASS, null); }
    public ModifiableEventResult<T> modified(T modifiedData) { return new ModifiableEventResult<>(ModifiableEventResultType.MODIFY, modifiedData); }

}
