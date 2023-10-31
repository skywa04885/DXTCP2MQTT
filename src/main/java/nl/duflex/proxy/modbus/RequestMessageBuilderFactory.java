package nl.duflex.proxy.modbus;

public class RequestMessageBuilderFactory {
    public static RequestMessage.Builder<?, ?> createForRequestType(final ModbusProxyRequestType requestType)  {
        return switch (requestType) {
            case WriteSingleCoil -> WriteSingleCoilRequestMessage.newBuilder();
            case ReadCoils -> ReadCoilsRequestMessage.newBuilder();
            default -> throw new RuntimeException("Unrecognized request type: " + requestType);
        };
    }
}
