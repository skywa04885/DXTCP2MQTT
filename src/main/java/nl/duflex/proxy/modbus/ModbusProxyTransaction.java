package nl.duflex.proxy.modbus;

import java.util.concurrent.CompletableFuture;

public class ModbusProxyTransaction {
    private final ModbusProxyRequestMessage requestMessage;
    private final CompletableFuture<ModbusProxyResponseMessage> futureResponseMessage;

    public ModbusProxyTransaction(final ModbusProxyRequestMessage request) {
        this.requestMessage = request;
        this.futureResponseMessage = new CompletableFuture<>();
    }

    public final ModbusProxyRequestMessage getRequest() {
        return this.requestMessage;
    }

    public final CompletableFuture<ModbusProxyResponseMessage> getFutureResponseMessage() {
        return this.futureResponseMessage;
    }
}
