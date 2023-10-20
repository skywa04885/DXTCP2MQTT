package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.requests.WriteSingleRegisterRequest;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.responses.WriteSingleRegisterResponse;
import com.digitalpetri.modbus.slave.ModbusTcpSlave;
import com.digitalpetri.modbus.slave.ModbusTcpSlaveConfig;
import com.digitalpetri.modbus.slave.ServiceRequestHandler;
import io.netty.util.ReferenceCountUtil;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;
import nl.duflex.proxy.ProxyProtocolClientHandler;
import nl.duflex.proxy.ProxyTcpClient;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class ModbusSlaveProtocolClientHandler extends ProxyProtocolClientHandler implements ServiceRequestHandler {
    /**
     * This runnable is responsible for sending the requests.
     */
    private static class RequestSendingRunnable implements Runnable {
        private final ProxyOutputStreamWriter outputStreamWriter;
        private final LinkedList<ModbusProxyRequestMessage> queue = new LinkedList<>();
        private final Logger logger = Logger.getLogger(this.getClass().getName());

        /**
         * Creates a new request sending runnable.
         *
         * @param outputStreamWriter the output stream writer.
         */
        public RequestSendingRunnable(final ProxyOutputStreamWriter outputStreamWriter) {
            this.outputStreamWriter = outputStreamWriter;
        }

        /**
         * Enqueues the given message to be written.
         *
         * @param message the message to be written.
         */
        public void enqueue(final ModbusProxyRequestMessage message) {
            synchronized (this.queue) {
                this.queue.addLast(message);
                this.queue.notify();
            }
        }

        /**
         * Writes all the messages.
         */
        @Override
        public void run() {
            try {
                while (true) {
                    ModbusProxyRequestMessage message;

                    // Gets the message that should be written from the queue.
                    synchronized (this.queue) {
                        if (this.queue.isEmpty()) this.queue.wait();
                        message = this.queue.removeFirst();
                    }

                    // Writes the message.
                    message.writeToOutputStreamWriter(this.outputStreamWriter);
                }
            } catch (final IOException exception) {
                this.logger.severe("An io exception occurred, message: " + exception.getMessage());
            } catch (final InterruptedException exception) {
                // TODO: Do something with this?
                this.logger.severe("An interrupted exception occurred, message: " + exception.getMessage());
            }
        }
    }

    /**
     * This class keeps track of a future response, it contains the builder that will be used to
     * build the future message for the completion.
     */
    public static class FutureResponse {
        private final ModbusProxyResponseMessage.Builder builder;
        private final CompletableFuture<ModbusProxyResponseMessage> completableFutureMessage;

        /**
         * Constructs a new future response.
         * @param builder the builder for the future response message.
         */
        public FutureResponse(final ModbusProxyResponseMessage.Builder builder) {
            this.builder = builder;
            this.completableFutureMessage = new CompletableFuture<>();
        }

        /**
         * Gets the builder for the response message.
         * @return the builder for the response message.
         */
        public final ModbusProxyResponseMessage.Builder getBuilder() {
            return this.builder;
        }

        /**
         * Gets the completable future response message.
         * @return the completable future response message.
         */
        public final CompletableFuture<ModbusProxyResponseMessage> getCompletableFutureMessage() {
            return this.completableFutureMessage;
        }
    }

    /**
     * This runnable is responsible for receiving the responses.
     */
    private static class ResponseReceivingRunnable implements Runnable {
        private final ProxyInputStreamReader inputStreamReader;
        private final LinkedList<FutureResponse> queue = new LinkedList<>();
        private final Logger logger = Logger.getLogger(this.getClass().getName());

        /**
         * Constructs a new response receiving runnable.
         *
         * @param inputStreamReader the reader to read the responses from.
         */
        public ResponseReceivingRunnable(final ProxyInputStreamReader inputStreamReader) {
            this.inputStreamReader = inputStreamReader;
        }

        /**
         * Enqueues the given future response builder to the queue.
         *
         * @param futureResponse the future response to enqueue.
         */
        public void enqueue(final FutureResponse futureResponse) {
            synchronized (this.queue) {
                this.queue.addLast(futureResponse);
                this.queue.notify();
            }
        }

        /**
         * Receives all the responses.
         */
        @Override
        public void run() {
            try {
                while (true) {
                    FutureResponse futureResponse;

                    // Dequeues the future response.
                    synchronized (this.queue) {
                        if (this.queue.isEmpty()) this.queue.wait();
                        futureResponse = this.queue.removeFirst();
                    }

                    // Builds the response message from the input stream.
                    final ModbusProxyResponseMessage responseMessage = futureResponse.getBuilder()
                            .readFromInputStreamReader(inputStreamReader)
                            .build();

                    // Completes the future with the built response message.
                    futureResponse.getCompletableFutureMessage().complete(responseMessage);
                }
            } catch (final IOException exception) {
                this.logger.severe("An io exception occurred, message: " + exception.getMessage());
            } catch (final InterruptedException exception) {
                // TODO: Do something with this?
                this.logger.severe("An interrupted exception occurred, message: " + exception.getMessage());
            }
        }
    }

    private final RequestSendingRunnable requestSendingRunnable;
    private final ResponseReceivingRunnable responseReceivingRunnable;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private ModbusTcpSlave modbusTcpSlave = null;

    public ModbusSlaveProtocolClientHandler(final ProxyTcpClient client) {
        super(client);

        this.requestSendingRunnable = new RequestSendingRunnable(client.getOutputStreamWriter());
        this.responseReceivingRunnable = new ResponseReceivingRunnable(client.getInputStreamReader());
    }

    @Override
    public void onReadHoldingRegisters(ServiceRequest<ReadHoldingRegistersRequest, ReadHoldingRegistersResponse> service) {
    }

    /**
     * Gets called on a single register write request.
     *
     * @param service the service.
     */
    @Override
    public void onWriteSingleRegister(ServiceRequest<WriteSingleRegisterRequest, WriteSingleRegisterResponse> service) {
        final WriteSingleRegisterRequest request = service.getRequest();

        // Constructs the future response.
        final FutureResponse futureResponse = new FutureResponse(ModbusProxyWriteSingleRegisterResponseMessage
                .newBuilder());

        // Listens for the future response message.
        futureResponse.getCompletableFutureMessage().thenAccept(responseMessage -> {
            // Makes sure that the response message is of the correct type, if not, raise an error.
            if (responseMessage instanceof ModbusProxyWriteSingleRegisterResponseMessage singleRegisterResponseMessage) {
                // Sends the response to the service.
                service.sendResponse(singleRegisterResponseMessage.toResponse());

                // Releases the request.
                ReferenceCountUtil.release(request);
            } else {
                throw new Error("Response message is not of the correct type");
            }
        });

        // Builds the request message.
        final ModbusProxyWriteSingleRegisterRequestMessage requestMessage =
                ModbusProxyWriteSingleRegisterRequestMessage
                        .newBuilder()
                        .copyFrom(request)
                        .build();

        // Enqueues the future response and the request message.
        this.responseReceivingRunnable.enqueue(futureResponse);
        this.requestSendingRunnable.enqueue(requestMessage);
    }

    /**
     * Binds the modbus slave.
     *
     * @return false if the end of the stream was reached.
     * @throws IOException          gets thrown when the reading of the config line fails.
     * @throws RuntimeException     gets thrown when the config line has invalid values.
     * @throws ExecutionException   gets thrown when the async stuff goes wrong.
     * @throws InterruptedException gets thrown when the async stuff goes wrong.
     */
    private boolean bind() throws IOException, RuntimeException, ExecutionException, InterruptedException {
        final ProxyInputStreamReader inputStreamReader = this.client.getInputStreamReader();

        // Builds the modbus slave options.
        final ModbusOptions.Builder modbusOptionsBuilder = ModbusOptions.newBuilder().readFromInputStreamReader(inputStreamReader);
        if (modbusOptionsBuilder == null) return false;
        final ModbusOptions modbusOptions = modbusOptionsBuilder.build();

        // Builds the slave config.
        final ModbusTcpSlaveConfig.Builder modbusTcpSlaveConfigBuilder = new ModbusTcpSlaveConfig.Builder();
        final ModbusTcpSlaveConfig modbusTcpSlaveConfig = modbusTcpSlaveConfigBuilder.build();

        // Creates the slave.
        final ModbusTcpSlave modbusTcpSlave = new ModbusTcpSlave(modbusTcpSlaveConfig);

        // Sets the request handler.
        modbusTcpSlave.setRequestHandler(this);

        // Binds the slave.
        modbusTcpSlave.bind(modbusOptions.getAddress().toString(), modbusOptions.getPort()).get();

        // Puts the slave in the instance variable.
        this.modbusTcpSlave = modbusTcpSlave;

        return true;
    }

    @Override
    public void run() {
        try {
            if (bind()) {
                // TODO: something.
                return;
            }

            // Starts the request sending thread.
            this.logger.info("Starting request sending thread");
            final Thread requestSendingThread = new Thread(this.requestSendingRunnable);
            requestSendingThread.start();

            // Starts the response receiving thread.
            this.logger.info("Starting response receiving thread");
            final Thread responseReceivingThread = new Thread(this.responseReceivingRunnable);
            responseReceivingThread.start();
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
