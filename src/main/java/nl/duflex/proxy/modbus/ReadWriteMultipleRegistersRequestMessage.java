package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.ReadWriteMultipleRegistersRequest;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

class ReadWriteMultipleRegistersRequestMessage extends RequestMessage<ReadWriteMultipleRegistersRequest> {
    static class Builder extends RequestMessage.Builder<com.digitalpetri.modbus.requests.ReadWriteMultipleRegistersRequest, ReadWriteMultipleRegistersRequestMessage> {
        private Integer readAddress = null;
        private Integer readQuantity = null;
        private Integer writeAddress = null;
        private Integer writeQuantity = null;
        private byte[] writeValues = null;

        @Override
        Builder copyFromModbusRequest(ReadWriteMultipleRegistersRequest request) {
            this.readAddress = request.getReadAddress();
            this.readQuantity = request.getReadQuantity();
            this.writeAddress = request.getWriteAddress();
            this.writeQuantity = request.getWriteQuantity();
            this.writeValues = io.netty.buffer.ByteBufUtil.getBytes(request.getValues());
            return this;
        }

        @Override
        Builder readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader)
                throws RuntimeException, IOException {
            final String line = inputStreamReader.readStringUntilNewLine();
            if (line == null) return null;

            final String[] lineSegments = line.split(" ");
            if (lineSegments.length != 5) throw new RuntimeException("Invalid number of segments in read write multiple " +
                    "registers request");

            final String readAddressLineSegment = lineSegments[0];
            final String readQuantityLineSegment = lineSegments[1];
            final String writeAddressLineSegment = lineSegments[2];
            final String writeQuantityLineSegment = lineSegments[3];
            final String noBytesForWritingLineSegment = lineSegments[4];

            int readAddress;
            int readQuantity;
            int writeAddress;
            int writeQuantity;
            int noBytesForWriting;

            try {
                readAddress = Integer.parseInt(readAddressLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid read address in read write multiple registers request");
            }

            try {
                readQuantity = Integer.parseInt(readQuantityLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid read quantity in read write multiple registers request");
            }

            try {
                writeAddress = Integer.parseInt(writeAddressLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid write address in read write multiple registers request");
            }

            try {
                writeQuantity = Integer.parseInt(writeQuantityLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid write quantity in read write multiple registers request");
            }

            try {
                noBytesForWriting = Integer.parseInt(noBytesForWritingLineSegment);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid no bytes for writing in read write multiple registers request");
            }

            final byte[] writeValues = inputStreamReader.readNBytes(noBytesForWriting);
            if (writeValues == null) return null;

            this.readAddress = readAddress;
            this.readQuantity = readQuantity;
            this.writeAddress = writeAddress;
            this.writeQuantity = writeQuantity;
            this.writeValues = writeValues;

            return this;
        }

        ReadWriteMultipleRegistersRequestMessage build() {
            assert this.readAddress != null;
            assert this.readQuantity != null;
            assert this.writeAddress != null;
            assert this.writeQuantity != null;
            assert this.writeValues != null;

            return new ReadWriteMultipleRegistersRequestMessage(this.readAddress, this.readQuantity,
                    this.writeAddress, this.writeQuantity, this.writeValues);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    private final int readAddress;
    private final int readQuantity;
    private final int writeAddress;
    private final int writeQuantity;
    private final byte[] writeValues;

    ReadWriteMultipleRegistersRequestMessage(final int readAddress, final int readQuantity,
                                             final int writeAddress, final int writeQuantity,
                                             final byte[] writeValues) {
        super(ModbusProxyRequestType.ReadWriteMultipleRegisters);

        this.readAddress = readAddress;
        this.readQuantity = readQuantity;
        this.writeAddress = writeAddress;
        this.writeQuantity = writeQuantity;
        this.writeValues = writeValues;
    }

    public int getWriteQuantity() {
        return writeQuantity;
    }

    public int getWriteAddress() {
        return writeAddress;
    }

    public byte[] getWriteValues() {
        return writeValues;
    }

    public int getReadAddress() {
        return readAddress;
    }

    public int getReadQuantity() {
        return readQuantity;
    }

    @Override
    void implWriteToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.readAddress + " " + this.readQuantity).flush();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " { read address: " + this.readAddress + ", read quantity: " + this.readQuantity
                + ", write address: " + this.writeAddress + ", write quantity: " + this.writeQuantity + " }";
    }
}
