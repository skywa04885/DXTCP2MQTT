package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.*;

public class ModbusRequestFactory {
    public static ModbusRequest createFromRequestMessage(final RequestMessage<?> requestMessage) {
        if (requestMessage instanceof MaskWriteRegisterRequestMessage maskWriteRegisterRequestMessage) {
            final var address = maskWriteRegisterRequestMessage.getAddress();
            final var orMask = maskWriteRegisterRequestMessage.getOrMask();
            final var andMask = maskWriteRegisterRequestMessage.getAndMask();

            return new MaskWriteRegisterRequest(address, orMask, andMask);
        }

        if (requestMessage instanceof ReadCoilsRequestMessage readCoilsRequestMessage) {
            final var address = readCoilsRequestMessage.getAddress();
            final var quantity = readCoilsRequestMessage.getQuantity();

            return new ReadCoilsRequest(address, quantity);
        }

        if (requestMessage instanceof ReadDiscreteInputsRequestMessage readDiscreteInputsRequestMessage) {
            final var address = readDiscreteInputsRequestMessage.getAddress();
            final var quantity = readDiscreteInputsRequestMessage.getQuantity();

            return new ReadDiscreteInputsRequest(address, quantity);
        }

        if (requestMessage instanceof ReadHoldingRegistersRequestMessage readHoldingRegistersRequestMessage) {
            final var address = readHoldingRegistersRequestMessage.getAddress();
            final var quantity = readHoldingRegistersRequestMessage.getQuantity();

            return new ReadHoldingRegistersRequest(address, quantity);
        }

        if (requestMessage instanceof ReadInputRegistersRequestMessage readInputRegistersRequestMessage) {
            final var address = readInputRegistersRequestMessage.getAddress();
            final var quantity = readInputRegistersRequestMessage.getQuantity();

            return new ReadInputRegistersRequest(address, quantity);
        }

        if (requestMessage instanceof ReadWriteMultipleRegistersRequestMessage readWriteMultipleRegistersRequest) {
            final var readAddress = readWriteMultipleRegistersRequest.getReadAddress();
            final var readQuantity = readWriteMultipleRegistersRequest.getReadQuantity();
            final var writeAddress = readWriteMultipleRegistersRequest.getWriteAddress();
            final var writeQuantity = readWriteMultipleRegistersRequest.getWriteQuantity();
            final var writeValues = readWriteMultipleRegistersRequest.getWriteValues();

            return new ReadWriteMultipleRegistersRequest(readAddress, readQuantity, writeAddress, writeQuantity,
                    writeValues);
        }

        if (requestMessage instanceof WriteMultipleCoilsRequestMessage writeMultipleCoilsRequestMessage) {
            final var address = writeMultipleCoilsRequestMessage.getAddress();
            final var quantity = writeMultipleCoilsRequestMessage.getValues().size();
            final var values = ModbusProxyBitArrayEncoder.encodeToBinary(writeMultipleCoilsRequestMessage.getValues());

            return new WriteMultipleCoilsRequest(address, quantity, values);
        }

        if (requestMessage instanceof WriteMultipleRegistersRequestMessage writeMultipleRegistersRequestMessage) {
            final var address = writeMultipleRegistersRequestMessage.getAddress();
            final var quantity = writeMultipleRegistersRequestMessage.getQuantity();
            final var values = writeMultipleRegistersRequestMessage.getValues();

            return new WriteMultipleRegistersRequest(address, quantity, values);
        }

        if (requestMessage instanceof WriteSingleCoilRequestMessage writeSingleCoilRequestMessage) {
            final var address = writeSingleCoilRequestMessage.getAddress();
            final var value = writeSingleCoilRequestMessage.getValue();

            return new WriteSingleCoilRequest(address, value);
        }

        if (requestMessage instanceof WriteSingleRegisterRequestMessage writeSingleRegisterRequestMessage) {
            final var address = writeSingleRegisterRequestMessage.getAddress();
            final var value = writeSingleRegisterRequestMessage.getValue();

            return new WriteSingleRegisterRequest(address, value);
        }

        throw new Error("Unrecognized request message: " + requestMessage);
    }
}
