package nl.duflex;

import nl.duflex.proxy.*;
import nl.duflex.proxy.http.DXHttpConfig;
import nl.duflex.proxy.http.DXHttpRequestMethod;
import nl.duflex.proxy.http.DXHttpSwaggerParser;
import nl.duflex.proxy.http.HttpRestApiClientHandlerBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Logger;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class.getName());

    private static DXHttpConfig ReadHttpConfig() {
        try {
            final var path = Paths.get(System.getProperty("user.dir"), "http.xml").toString();
            final var file = new File(path);
            return DXHttpConfig.ReadFromFile(file);
        } catch (IOException e) {
            throw new Error("Failed to read config");
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        DXHttpSwaggerParser nigga = new DXHttpSwaggerParser();
//        nigga.parse(new String(new FileInputStream("mir100.yaml").readAllBytes()));

//        System.exit(-1);

        final var encodedLicenseKey = System.getenv("DX_PROTO_PROXY_LICENSE");
        if (encodedLicenseKey == null) {
            logger.warning("License key could not be found. Insert license in 'DX_PROTO_PROXY_LICENSE' " +
                    "environment variable.");
            return;
        }

        final var licenseKeyVerifierPublicKey = LicenseVerifierPublicKeyFactory.createFromResource("public.pem");
        final var licenseVerifier = new LicenseVerifier(licenseKeyVerifierPublicKey);

        License license;
        try {
            license = licenseVerifier.verify(encodedLicenseKey);
        } catch (final InvalidLicenseException invalidLicenseException) {
            logger.warning(invalidLicenseException.getMessage());
            return;
        }

        logger.info("License key verified!");

        final HashMap<ProxyProtocol, ProtocolClientHandlerBuilder> proxyProtocolProtocolClientHandlerBuilderMap = new HashMap<>();

        for (final var feature : license.getFeatures()) {
            final var protocol = ProxyProtocol.deserialize(feature);
            logger.info("Enabling handler for protocol: " + protocol);

            final var builder = ProtocolClientHandlerBuilderFactory.forProtocol(protocol);

            if (builder instanceof HttpRestApiClientHandlerBuilder httpRestApiClientHandlerBuilder) {
                httpRestApiClientHandlerBuilder.setConfig(ReadHttpConfig());
            }

            proxyProtocolProtocolClientHandlerBuilderMap.put(protocol, builder);
        }

        final var clientHandlerFactory = new ProxyProtocolClientHandlerFactory(proxyProtocolProtocolClientHandlerBuilderMap);

        final ServerSocket serverSocket = new ServerSocket((short) 5000, 4, InetAddress.getByName("0.0.0.0"));
        final TcpServer tcpServer = new TcpServer(serverSocket, clientHandlerFactory);
        tcpServer.run();
    }
}