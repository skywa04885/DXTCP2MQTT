package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.*;
import com.digitalpetri.modbus.responses.*;

public class ResponseMessageFactory {
    /**
     * Creates a new response message instance based on the given modbus response and modbus request.
     *  This is needed because some modbus responses require information from the request to be useful.
     * @param modbusResponse The modbus response.
     * @param modbusRequest The modbus request associated with the response.
     * @return The created response message.
     */
    public static ResponseMessage<?> createFromModbusResponse(final ModbusResponse modbusResponse, final ModbusRequest modbusRequest) {
        // Read coils response.
        if (modbusResponse instanceof ReadCoilsResponse readCoilsResponse &&
                modbusRequest instanceof ReadCoilsRequest readCoilsRequest) {
            final var valueBytes = io.netty.buffer.ByteBufUtil.getBytes(readCoilsResponse.getCoilStatus());
            final var values = ModbusProxyBitArrayDecoder.decodeFromBinary(valueBytes, readCoilsRequest.getQuantity());

            return new ReadCoilsResponseMessage(values);
        }

        // Write single coil response.
        if (modbusResponse instanceof WriteSingleCoilResponse writeSingleCoilResponse &&
                modbusRequest instanceof WriteSingleCoilRequest) {
            final var address = writeSingleCoilResponse.getAddress();
            final var value = writeSingleCoilResponse.getValue() == 0xFF00;

            return new WriteSingleCoilResponseMessage(address, value);
        }

        // Write multiple coils response.
        if (modbusResponse instanceof WriteMultipleCoilsResponse writeMultipleCoilsResponse &&
                modbusRequest instanceof WriteMultipleCoilsRequest) {
            final var address = writeMultipleCoilsResponse.getAddress();
            final var quantity = writeMultipleCoilsResponse.getQuantity();

            return new WriteMultipleCoilsResponseMessage(address, quantity);
        }

        // Mask write response.
        if (modbusResponse instanceof MaskWriteRegisterResponse maskWriteRegisterResponse
            && modbusRequest instanceof MaskWriteRegisterRequest) {
            final var address = maskWriteRegisterResponse.getAddress();
            final var orMask = maskWriteRegisterResponse.getOrMask();
            final var andMask = maskWriteRegisterResponse.getAndMask();

            return new MaskWriteRegisterResponseMessage(address, orMask, andMask);
        }

        // Discrete input response.
        if (modbusResponse instanceof ReadDiscreteInputsResponse readDiscreteInputsResponse &&
                modbusRequest instanceof ReadDiscreteInputsRequestMessage readDiscreteInputsRequest) {
            final var valueBytes = io.netty.buffer.ByteBufUtil.getBytes(readDiscreteInputsResponse.getInputStatus());
            final var values = ModbusProxyBitArrayDecoder.decodeFromBinary(valueBytes,
                    readDiscreteInputsRequest.getQuantity());

            return new ReadDiscreteInputsResponseMessage(values);
        }

        // Read holding registers response.
        if (modbusResponse instanceof ReadHoldingRegistersResponse readHoldingRegistersResponse &&
                modbusRequest instanceof ReadHoldingRegistersRequest) {
            final var registers = io.netty.buffer.ByteBufUtil.getBytes(readHoldingRegistersResponse.getRegisters());

            return new ReadHoldingRegistersResponseMessage(registers);
        }

        // Read input registers response.
        if (modbusResponse instanceof ReadInputRegistersResponse readInputRegistersResponse &&
                modbusRequest instanceof ReadInputRegistersRequest) {
            final var registers = io.netty.buffer.ByteBufUtil.getBytes(readInputRegistersResponse.getRegisters());

            return new ReadInputRegistersResponseMessage(registers);
        }

        // Read write multiple registers response.
        if (modbusResponse instanceof ReadWriteMultipleRegistersResponse readWriteMultipleRegistersResponse &&
                modbusRequest instanceof ReadWriteMultipleRegistersRequest) {
            final var registers = io.netty.buffer.ByteBufUtil.getBytes(readWriteMultipleRegistersResponse.getRegisters());

            return new ReadWriteMultipleRegistersResponseMessage(registers);
        }

        // Write multiple registers response.
        if (modbusResponse instanceof WriteMultipleRegistersResponse writeMultipleRegistersResponse &&
                modbusRequest instanceof WriteMultipleRegistersRequest) {
            final var address = writeMultipleRegistersResponse.getAddress();
            final var quantity = writeMultipleRegistersResponse.getQuantity();

            return new WriteMultipleRegistersResponseMessage(address, quantity);
        }

        // Write single register response.
        if (modbusResponse instanceof WriteSingleRegisterResponse writeSingleRegisterResponse &&
            modbusRequest instanceof WriteSingleRegisterRequest) {
            final var address = writeSingleRegisterResponse.getAddress();
            final var value = writeSingleRegisterResponse.getValue();

            return new WriteSingleRegisterResponseMessage(address, value);
        }

        // Throws an error since we do not know what to do.
        throw new Error("Unrecognized pair of modbus request/response, namely " + modbusRequest + " and "
                + modbusResponse);
    }
}
