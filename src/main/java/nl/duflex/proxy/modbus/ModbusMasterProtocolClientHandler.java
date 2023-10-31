package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.digitalpetri.modbus.slave.ServiceRequestHandler;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyProtocolClientHandler;
import nl.duflex.proxy.ProxyTcpClient;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class ModbusMasterProtocolClientHandler extends ProxyProtocolClientHandler implements ServiceRequestHandler {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final LinkedList<ResponseMessage<?>> responseMessages = new LinkedList<>();

    private ModbusTcpMaster modbusTcpMaster = null;
    private boolean running = false;
    private Thread requestSendingThread = null;
    private Thread requestReceivingThread = null;

    public ModbusMasterProtocolClientHandler(final ProxyTcpClient client) {
        super(client);
    }

    /**
     * Connects to the modbus slave.
     *
     * @return True if we're connected.
     * @throws IOException          Gets thrown when an IOException occurred.
     * @throws RuntimeException     Gets most likely thrown by the options-builder.
     * @throws ExecutionException   Gets called when something went wrong with the modbus master.
     * @throws InterruptedException Gets called when something went wrong with the modbus master.
     */
    private boolean connect() throws IOException, RuntimeException, ExecutionException, InterruptedException {
        final ProxyInputStreamReader inputStreamReader = this.client.getInputStreamReader();

        final ModbusMasterOptions.Builder modbusMasterOptionsBuilder = ModbusMasterOptions
                .newBuilder()
                .readFromInputStreamReader(inputStreamReader);

        if (modbusMasterOptionsBuilder == null) return false;

        final ModbusMasterOptions modbusMasterOptions = modbusMasterOptionsBuilder.build();

        final ModbusTcpMasterConfig modbusTcpMasterConfig = new ModbusTcpMasterConfig.Builder(modbusMasterOptions.getAddress().getHostAddress())
                .setPort(modbusMasterOptions.getPort())
                .build();

        this.modbusTcpMaster = new ModbusTcpMaster(modbusTcpMasterConfig);

        this.modbusTcpMaster.connect();

        return true;
    }

    private void shutdown() {
        // Disconnect the master.
        if (this.modbusTcpMaster != null) {
            try {
                this.modbusTcpMaster.disconnect().get();
                this.logger.info("Disconnected modbus master");
            } catch (final ExecutionException | InterruptedException exception) {
                exception.printStackTrace();
                this.logger.severe("Failed to disconnect modbus master, message: " + exception.getMessage());
            }
        }

        // Close the client.
        try {
            this.client.close();
        } catch (final IOException ioException) {
            this.logger.warning("Failed to close client socket, message: " + ioException.getMessage());
        }
    }

    private void sendRequests() {
        try {
            var outputStreamWriter = this.client.getOutputStreamWriter();

            while (true) {
                // Retrieves the message that we should send.
                ResponseMessage<?> responseMessage;
                synchronized (this.responseMessages) {
                    if (this.responseMessages.isEmpty()) this.responseMessages.wait();
                    responseMessage = this.responseMessages.removeFirst();
                }

                // Writes the response message to the output stream.
                outputStreamWriter.write(responseMessage);
            }
        } catch (final InterruptedException ignored) {
        } catch (final IOException ioException) {
            this.logger.warning("Got IO exception while sending requests, message: " + ioException.getMessage());
        } finally {
            // Makes sure that the shutdown only gets called once.
            synchronized (this) {
                if (!this.running) return;
                this.running = false;
            }

            // Interrupts the other thread.
            this.requestReceivingThread.interrupt();

            // Shuts down.
            shutdown();
        }

        this.logger.info("Stopping with sending requests");
    }

    private void receiveRequests() {
        try {
            var inputStreamReader = this.client.getInputStreamReader();

            while (true) {
                // Reads the request type, and breaks when we've reached the end of the stream.
                final var requestType = ModbusProxyRequestType.readFromInputStreamReader(inputStreamReader);
                if (requestType == null) break;

                // Constructs the request message builder based on the request type.
                final var requestMessageBuilder = RequestMessageBuilderFactory.createForRequestType(requestType);

                // Reads the data from the input stream into the builder and breaks when we've reached
                // the end of the stream.
                if (requestMessageBuilder.readFromInputStreamReader(inputStreamReader) == null) break;

                // Builds the request message.
                final var requestMessage = requestMessageBuilder.build();

                // Creates the modbus request.
                final var modbusRequest = ModbusRequestFactory.createFromRequestMessage(requestMessage);

                // Sends the request, and handles the response.
                this.modbusTcpMaster
                        .sendRequest(modbusRequest, 0)
                        .thenAccept(modbusResponse -> {
                            // Creates the response message based on the modbus response and request.
                            final ResponseMessage<?> responseMessage = ResponseMessageFactory
                                    .createFromModbusResponse(modbusResponse, modbusRequest);

                            // Enqueues the response message to be written.
                            synchronized (this.responseMessages) {
                                this.responseMessages.addLast(responseMessage);
                                this.responseMessages.notifyAll();
                            }

                            // releases the request and the response.
                            io.netty.util.ReferenceCountUtil.release(modbusResponse);
                            io.netty.util.ReferenceCountUtil.release(modbusRequest);
                        });

            }
        } catch (final IOException exception) {
            this.logger.severe("An io exception occurred, message: " + exception.getMessage());
        } finally {
            // Makes sure that the shutdown only gets called once.
            synchronized (this) {
                if (!this.running) return;
                this.running = false;
            }

            // Interrupts the other thread.
            this.requestSendingThread.interrupt();

            // Shuts down.
            shutdown();
        }

        this.logger.info("Stopping with receiving requests");
    }

    @Override
    public void run() {
        try {
            if (!connect()) {
                return;
            }

            this.requestSendingThread = new Thread(this::sendRequests);
            this.requestSendingThread.start();

            this.requestReceivingThread = new Thread(this::receiveRequests);
            this.requestReceivingThread.start();

            this.running = true;
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
