package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.slave.ModbusTcpSlave;
import com.digitalpetri.modbus.slave.ModbusTcpSlaveConfig;
import com.digitalpetri.modbus.slave.ServiceRequestHandler;
import io.netty.util.ReferenceCountUtil;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyProtocolClientHandler;
import nl.duflex.proxy.ProxyTcpClient;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ModbusSlaveProtocolClientHandler extends ProxyProtocolClientHandler implements ServiceRequestHandler {
    public static class FutureResponse<T extends ResponseMessage<?>> {
        private final ResponseMessage.Builder<T> builder;
        private final CompletableFuture<T> completableFutureMessage;

        public FutureResponse(final ResponseMessage.Builder<T> builder) {
            this.builder = builder;
            this.completableFutureMessage = new CompletableFuture<>();
        }

        public final CompletableFuture<T> getCompletableFutureMessage() {
            return this.completableFutureMessage;
        }

        public boolean complete(final ProxyInputStreamReader inputStreamReader) throws IOException {
            // Returns true if the connection was closde.
            if (this.builder.readFromInputStreamReader(inputStreamReader) == null) return true;

            // Completes the future message.
            this.completableFutureMessage.complete(this.builder.build());

            // Returns false since the message was not closed.
            return false;
        }
    }

    private void sendRequests() {
        try {
            final var outputStreamWriter = this.client.getOutputStreamWriter();

            while (true) {
                RequestMessage<?> message;

                // Gets the message that should be written from the queue.
                this.logger.info("Waiting for request message to send");

                synchronized (this.requestQueue) {
                    if (this.requestQueue.isEmpty()) this.requestQueue.wait();
                    message = this.requestQueue.removeFirst();
                    this.logger.info("Got request message from queue");
                }

                // Writes the message.
                this.logger.info("Writing request message");
                message.writeToOutputStreamWriter(outputStreamWriter);
            }
        } catch (final IOException exception) {
            this.logger.severe("An io exception occurred, message: " + exception.getMessage());
        } catch (final InterruptedException ignore) {
        } finally {
            if (running.compareAndSet(true, false)) {
                receiveResponsesThread.interrupt();
                shutdown();
            }
        }

    }

    private void receiveResponses()
    {
        try {
            var inputStreamReader = this.client.getInputStreamReader();

            while (true) {
                FutureResponse<?> futureResponse;

                this.logger.info("Waiting for future response message to read");

                synchronized (this.futureResponsesQueue) {
                    if (this.futureResponsesQueue.isEmpty()) this.futureResponsesQueue.wait();
                    futureResponse = this.futureResponsesQueue.removeFirst();
                }

                this.logger.info("Reading future response message");

                if (futureResponse.complete(inputStreamReader)) {
                    this.logger.info("Client closed connection");
                    break;
                }

                this.logger.info("Read future response message");

            }
        } catch (final IOException exception) {
            this.logger.severe("An io exception occurred, message: " + exception.getMessage());
        } catch (final InterruptedException ignore) {
        } finally {
            if (running.compareAndSet(true, false)) {
                sendRequestsThread.interrupt();
                shutdown();
            }
        }
    }

    private final LinkedList<FutureResponse<?>> futureResponsesQueue = new LinkedList<>();
    private final LinkedList<RequestMessage<?>> requestQueue = new LinkedList<>();
    private Thread sendRequestsThread = null;
    private Thread receiveResponsesThread = null;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private ModbusTcpSlave modbusTcpSlave = null;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ModbusSlaveProtocolClientHandler(final ProxyTcpClient client) {
        super(client);
    }

    private <TModbusRequest extends com.digitalpetri.modbus.requests.ModbusRequest,
            TModbusResponse extends com.digitalpetri.modbus.responses.ModbusResponse,
            TModbusProxyResponseMessage extends ResponseMessage<TModbusResponse>,
            TModbusProxyRequestMessage extends RequestMessage<TModbusRequest>>
    void onServiceRequest(final ServiceRequest<TModbusRequest, TModbusResponse> serviceRequest,
                          final ResponseMessage.Builder<TModbusProxyResponseMessage>
                                  modbusProxyResponseMessageBuilder,
                          final RequestMessage.Builder<TModbusRequest, TModbusProxyRequestMessage>
                                  modbusProxyRequestMessageBuilder) {
        final TModbusRequest request = serviceRequest.getRequest();

        final TModbusProxyRequestMessage proxyRequestMessage = modbusProxyRequestMessageBuilder
                .copyFromModbusRequest(request)
                .build();

        ReferenceCountUtil.release(request);

        this.logger.info("Handling proxy request message: " + proxyRequestMessage);

        final FutureResponse<TModbusProxyResponseMessage> futureResponse =
                new FutureResponse<>(modbusProxyResponseMessageBuilder);

        futureResponse.getCompletableFutureMessage().thenAccept(proxyResponseMessage -> {
            serviceRequest.sendResponse(proxyResponseMessage.toModbusResponse());
            this.logger.info("Handled proxy request message: " + proxyRequestMessage);
        });

        synchronized (this.requestQueue) {
            this.requestQueue.addLast(proxyRequestMessage);
            this.requestQueue.notifyAll();
        }

        synchronized (this.futureResponsesQueue) {
            this.futureResponsesQueue.addLast(futureResponse);
            this.futureResponsesQueue.notifyAll();
        }
    }

    private void shutdown() {
        if (this.modbusTcpSlave != null) {
            this.modbusTcpSlave.shutdown();
            this.logger.info("Modbus slave shut down");
        }

        try {
            this.client.close();
            this.logger.info("Closed client");
        } catch (final IOException ioException) {
            this.logger.warning("Failed to close client socket, message: " + ioException.getMessage());
        }
    }

    @Override
    public void onMaskWriteRegister(final com.digitalpetri.modbus.slave.ServiceRequestHandler.ServiceRequest<com.digitalpetri.modbus.requests.MaskWriteRegisterRequest, com.digitalpetri.modbus.responses.MaskWriteRegisterResponse> service) {
        onServiceRequest(service, MaskWriteRegisterResponseMessage.newBuilder(),
                MaskWriteRegisterRequestMessage.newBuilder());

    }

    @Override
    public void onReadInputRegisters(final com.digitalpetri.modbus.slave.ServiceRequestHandler.ServiceRequest<com.digitalpetri.modbus.requests.ReadInputRegistersRequest, com.digitalpetri.modbus.responses.ReadInputRegistersResponse> service) {
        onServiceRequest(service, ReadInputRegistersResponseMessage.newBuilder(),
                ReadInputRegistersRequestMessage.newBuilder());

    }

    @Override
    public void onReadDiscreteInputs(final com.digitalpetri.modbus.slave.ServiceRequestHandler.ServiceRequest<com.digitalpetri.modbus.requests.ReadDiscreteInputsRequest, com.digitalpetri.modbus.responses.ReadDiscreteInputsResponse> service) {
        onServiceRequest(service, ReadDiscreteInputsResponseMessage.newBuilder(),
                ReadDiscreteInputsRequestMessage.newBuilder());
    }

    @Override
    public void onWriteMultipleRegisters(final com.digitalpetri.modbus.slave.ServiceRequestHandler.ServiceRequest<com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest, com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse> service) {
        onServiceRequest(service, WriteMultipleRegistersResponseMessage.newBuilder(),
                WriteMultipleRegistersRequestMessage.newBuilder());
    }

    @Override
    public void onReadCoils(final com.digitalpetri.modbus.slave.ServiceRequestHandler.ServiceRequest<com.digitalpetri.modbus.requests.ReadCoilsRequest, com.digitalpetri.modbus.responses.ReadCoilsResponse> service) {
        onServiceRequest(service, ReadCoilsResponseMessage.newBuilder(),
                ReadCoilsRequestMessage.newBuilder());
    }

    @Override
    public void onWriteMultipleCoils(final com.digitalpetri.modbus.slave.ServiceRequestHandler.ServiceRequest<com.digitalpetri.modbus.requests.WriteMultipleCoilsRequest, com.digitalpetri.modbus.responses.WriteMultipleCoilsResponse> service) {
        onServiceRequest(service, WriteMultipleCoilsResponseMessage.newBuilder(),
                WriteMultipleCoilsRequestMessage.newBuilder());
    }

    @Override
    public void onWriteSingleCoil(com.digitalpetri.modbus.slave.ServiceRequestHandler.ServiceRequest<com.digitalpetri.modbus.requests.WriteSingleCoilRequest, com.digitalpetri.modbus.responses.WriteSingleCoilResponse> service) {
        onServiceRequest(service, WriteSingleCoilResponseMessage.newBuilder(),
                WriteSingleCoilRequestMessage.newBuilder());
    }

    @Override
    public void onReadHoldingRegisters(final com.digitalpetri.modbus.slave.ServiceRequestHandler.ServiceRequest<com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest, com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse> service) {
        onServiceRequest(service, ReadHoldingRegistersResponseMessage.newBuilder(),
                ReadHoldingRegistersRequestMessage.newBuilder());
    }

    @Override
    public void onReadWriteMultipleRegisters(final com.digitalpetri.modbus.slave.ServiceRequestHandler.ServiceRequest<com.digitalpetri.modbus.requests.ReadWriteMultipleRegistersRequest, com.digitalpetri.modbus.responses.ReadWriteMultipleRegistersResponse> service) {
        onServiceRequest(service, ReadWriteMultipleRegistersResponseMessage.newBuilder(),
                ReadWriteMultipleRegistersRequestMessage.newBuilder());
    }

    @Override
    public void onWriteSingleRegister(com.digitalpetri.modbus.slave.ServiceRequestHandler.ServiceRequest<com.digitalpetri.modbus.requests.WriteSingleRegisterRequest, com.digitalpetri.modbus.responses.WriteSingleRegisterResponse> service) {
        onServiceRequest(service, WriteSingleRegisterResponseMessage.newBuilder(),
                WriteSingleRegisterRequestMessage.newBuilder());
    }

    private boolean bind() throws IOException, RuntimeException, ExecutionException, InterruptedException {
        final ProxyInputStreamReader inputStreamReader = this.client.getInputStreamReader();

        final ModbusSlaveOptions.Builder modbusOptionsBuilder = ModbusSlaveOptions.newBuilder().readFromInputStreamReader(inputStreamReader);
        if (modbusOptionsBuilder == null) return false;
        final ModbusSlaveOptions modbusOptions = modbusOptionsBuilder.build();

        final ModbusTcpSlaveConfig.Builder modbusTcpSlaveConfigBuilder = new ModbusTcpSlaveConfig.Builder();
        final ModbusTcpSlaveConfig modbusTcpSlaveConfig = modbusTcpSlaveConfigBuilder.build();

        final ModbusTcpSlave modbusTcpSlave = new ModbusTcpSlave(modbusTcpSlaveConfig);
        modbusTcpSlave.setRequestHandler(this);
        modbusTcpSlave.bind(modbusOptions.getAddress().getHostAddress(), modbusOptions.getPort()).get();

        this.modbusTcpSlave = modbusTcpSlave;

        return true;
    }

    @Override
    public void run() {
        try {
            if (!bind()) {
                return;
            }

            this.sendRequestsThread = new Thread(this::sendRequests);
            this.sendRequestsThread.start();

            this.receiveResponsesThread = new Thread(this::receiveResponses);
            this.receiveResponsesThread.start();

            this.running.set(true);
        } catch (final IOException exception) {
            this.logger.severe("An IO exception occurred, message: " + exception.getMessage());
        } catch (final RuntimeException exception) {
            this.logger.severe("A runtime exception occurred, message: " + exception.getMessage());
        } catch (final InterruptedException exception) {
            this.logger.severe("An interrupted exception occurred, message: " + exception.getMessage());
        } catch (final ExecutionException exception) {
            this.logger.severe("An execution exception occurred, message: " + exception.getMessage());
        }
    }
}
