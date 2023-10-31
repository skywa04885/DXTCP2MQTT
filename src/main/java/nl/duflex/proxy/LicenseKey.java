package nl.duflex.proxy;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class LicenseKey {
    public LicenseKey() {

    }

    public static LicenseKey parse(final InputStream inputStream)
    {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.parse(inputStream);


        } catch (final ParserConfigurationException parserConfigurationException) {

        } catch (final IOException ioException) {

        } catch (final SAXException saxException) {

        } finally {

        }

        return null;
    }
}
