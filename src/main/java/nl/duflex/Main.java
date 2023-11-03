package nl.duflex;

import nl.duflex.proxy.*;
import nl.duflex.proxy.http.DXHttpConfig;
import nl.duflex.proxy.http.DXHttpRequestData;
import nl.duflex.proxy.http.DXHttpRequestFactory;
import nl.duflex.proxy.http.DXHttpRequestMethod;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Logger;

public class Main {
    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        final var config = DXHttpConfig.ReadFromFile(new File(Paths.get(System.getProperty("user.dir"), "http.xml").toString()));

        HttpClient httpClient = HttpClient.newHttpClient();

        var res = DXHttpRequestFactory.FromConfig(config, new DXHttpRequestData(
                "MIR",
                "map",
                "primary",
                DXHttpRequestMethod.Post,
                new HashMap<>() {{
                    put("guid", "error");
                }},
                new HashMap<>() {{
                    put("hello", "world");
                }},
                new HashMap<>() {{
                    put("CI", "Test");
                }},
                new HashMap<>() {{
                    put("Test", "Hello!");
                }}
        )).Perform(httpClient);

        System.out.println(res.Code);
        System.out.println(res.Headers.toString());
        System.out.println(res.Body.toString());

        System.exit(0);

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
            proxyProtocolProtocolClientHandlerBuilderMap.put(protocol,
                    ProtocolClientHandlerBuilderFactory.forProtocol(protocol));
        }

        final var clientHandlerFactory = new ProxyProtocolClientHandlerFactory(proxyProtocolProtocolClientHandlerBuilderMap);

        final ServerSocket serverSocket = new ServerSocket((short) 5000, 4, InetAddress.getByName("0.0.0.0"));
        final TcpServer tcpServer = new TcpServer(serverSocket, clientHandlerFactory);
        tcpServer.run();
    }
}