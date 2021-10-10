package eu.dlvm.domotics.base;

public interface IStateChangeRegistrar {
    void addStateChangedListener(IStateChangedListener updator);
    void removeStateChangedListener(IStateChangedListener updator);
}
