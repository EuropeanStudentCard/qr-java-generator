package eu.europeanstudentcard.esc;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import eu.europeanstudentcard.esc.constants.QRConstants;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class is used to generate QRs for the european student cards
 * <p>
 * verifierUri: the uri pointing to the ESC Verifier, this uri will be concatenated with the card number
 * <p>
 * qrSize: the minimum size of the QR 41x41px
 * <p>
 * qrMargin: the minimum margin of the QR 4 modules/px
 * <p>
 * qrVersion: the version of the QR: Version 4, contains 33 modules of information
 * <p>
 * qrErrorCorrectionLevel: the error correction level: M
 */
public class QRFactory {

    private final String verifierUri;
    private final int qrSize;
    private final int qrMargin;
    private final int qrVersion;
    private final String qrErrorCorrectionLevel;

    public QRFactory() {
        this.qrErrorCorrectionLevel = "M";
        this.qrVersion = 4;
        this.qrMargin = 4;
        this.qrSize = 41;
        this.verifierUri = "https://s.esc-r.eu/";
    }

    public QRFactory(String host) {
        this.verifierUri = host;
        this.qrErrorCorrectionLevel = "M";
        this.qrVersion = 4;
        this.qrMargin = 4;
        this.qrSize = 41;
    }

    public static QRFactory create() {
        return new QRFactory();
    }

    public static QRFactory create(String host) {
        return new QRFactory(host);
    }

    /**
     * Generates the QR with the ESC Logo
     *
     * @param cardNumber:  the number of the card, will be concatenated with the uri
     * @param orientation: the orientation of the QR, vertical/horizontal
     * @param colours:     the colours of the QR, normal/inverted
     * @param size:        the size of the QR, XS (41x41px) + logo, S (61.5x61.5px) + logo, M (164x164px) + logo
     * @throws QRFactoryException if an error occurs while generating the QR
     * @return: the QR pointing to the ESC Verifier
     */
    public String generateQR(String cardNumber, String orientation, String colours, String size) throws QRFactoryException {
        //Check the parameters
        this.areValidParameters(orientation, colours, size);

        String result = "";
        // Get the QR properties and the scale factor
        boolean isVertical = QRConstants.VERTICAL_ORIENTATION.equalsIgnoreCase(orientation);
        boolean isPositive = QRConstants.NORMAL_COLOUR.equalsIgnoreCase(colours);
        boolean isExtraSmallSize = QRConstants.EXTRA_SMALL_SIZE.equalsIgnoreCase(size);
        boolean isSmallSize = QRConstants.SMALL_SIZE.equalsIgnoreCase(size);

        float scaleFactor = isExtraSmallSize ? 1.0f : isSmallSize ? 1.5f : 4f;

        // Generate the path of the logo based on the QR properties
        String documentPath = "logos/";
        String path = documentPath
                .concat(isVertical ? QRConstants.VERTICAL_ORIENTATION : QRConstants.HORIZONTAL_ORIENTATION)
                .concat("_")
                .concat(isPositive ? QRConstants.NORMAL_COLOUR : QRConstants.INVERTED_COLOUR)
                .concat(QRConstants.SVG_EXTENSION);

        try {
            // Concatenate the uri with the card number and retrieve the logo with the generated path
            String uri = this.getVerifierUri().concat(cardNumber);
            String file = Objects.requireNonNull(getClass().getClassLoader().getResource(path)).getFile();
            ClassPathResource cpr = new ClassPathResource(path);

            // Generate QR SVG string
            String qrSVG = this.generateQRCodeSvg(uri, this.qrSize, this.qrSize, isPositive);

            // Create a temporary file for the QR
            File qrFile = File.createTempFile("temp", QRConstants.SVG_EXTENSION);
            FileUtils.writeStringToFile(qrFile, qrSVG);

            try {
                // Merge the QR SVG and the Logo SVG in one SVG
                result = this.mergeSVG(qrFile, new File(file), isVertical, cpr, scaleFactor);
            } finally {
                qrFile.delete();
            }

        } catch (Exception e) {
            throw new QRFactoryException(e.getMessage());
        }

        return result;
    }

    /**
     * Validates the input parameters
     *
     * @param orientation: the orientation of the QR, vertical/horizontal
     * @param colours:     the colours of the QR, normal/inverted
     * @param size:        the size of the QR, XS (41x41px) + logo, S (61.5x61.5px) + logo, M (164x164px) + logo
     * @throws QRFactoryException if the input parameters are invalid
     * @return: the QR pointing to the ESC Verifier
     */
    protected void areValidParameters(String orientation, String colours, String size) throws QRFactoryException {
        if (!orientation.equalsIgnoreCase(QRConstants.VERTICAL_ORIENTATION) && !
                orientation.equalsIgnoreCase(QRConstants.HORIZONTAL_ORIENTATION)) {
            throw new QRFactoryException("Error validating parameters, invalid orientation");
        }

        if (!colours.equalsIgnoreCase(QRConstants.NORMAL_COLOUR) && !colours.equalsIgnoreCase(QRConstants.INVERTED_COLOUR)) {
            throw new QRFactoryException("Error validating parameters, invalid colour");
        }

        if (!size.equalsIgnoreCase(QRConstants.EXTRA_SMALL_SIZE) &&
                !size.equalsIgnoreCase(QRConstants.SMALL_SIZE) &&
                !size.equalsIgnoreCase(QRConstants.MEDIUM_SIZE)) {
            throw new QRFactoryException("Error validating parameters, invalid size");
        }
    }

    /**
     * Generates the QR code in SVG format
     *
     * @param content    the uri that will be pointed in the QR
     * @param width      the width of the QR
     * @param height     the height of the QR
     * @param isPositive the colours of the QR, if positive normals colours will be used, else, inverted colours
     * @return the QR code in SVG format
     * @throws WriterException if an error occurs while generating the QR
     */

    protected String generateQRCodeSvg(String content, int width, int height, boolean isPositive) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        // Set the parameters of the QR
        ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.forBits(QRConstants.ErrorLevel.getBitsFromName(this.qrErrorCorrectionLevel));
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        hints.put(EncodeHintType.QR_VERSION, this.qrVersion);
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);        // Start building the SVG string
        // Generate the SVG String
        StringBuilder svgBuilder = new StringBuilder();
        svgBuilder.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ");
        svgBuilder.append("width=\"").append(width).append("\" ");
        svgBuilder.append("height=\"").append(height).append("\" ");
        svgBuilder.append("viewBox=\"0 0 ").append(width).append(" ").append(height).append("\">");
        svgBuilder.append("<rect width=\"100%\" height=\"100%\" fill=\"transparent\"/>");
        String pixelColor = isPositive ? "black" : "white";
        // Iterate over the BitMatrix to generate <rect> for each black pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitMatrix.get(x, y)) {
                    svgBuilder.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"1\" height=\"1\" fill=\"" + pixelColor + "\"/>");
                }
            }
        }
        svgBuilder.append("</svg>");
        return svgBuilder.toString();
    }

    /**
     * Merge the SVG files in one
     *
     * @param qrSVG       The QR SVG File
     * @param logoSVG     The logo SVG File
     * @param isVertical  the orientation of the QR, if isVertical, the QR orientation is vertical, else, is horizontal
     * @param cpr         the resource pointing to the SVG File
     * @param scaleFactor the width and the height of the QR and the logo will be scaled with by factor
     * @return the SVG file merged
     * @throws Exception if an error occurs while merging the files
     */

    protected String mergeSVG(File qrSVG, File logoSVG, boolean isVertical, ClassPathResource cpr, float scaleFactor) throws Exception {
        // Load both SVG files
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
        Document qrDoc = factory.createDocument(qrSVG.toURI().toString());
        Document logoDoc = factory.createDocument(logoSVG.toURI().toString(), cpr.getInputStream());

        // Get width and height of both images
        Element qrElement = qrDoc.getDocumentElement();
        float qrWidth = Float.parseFloat(qrElement.getAttribute("width"));
        float qrHeight = Float.parseFloat(qrElement.getAttribute("height"));

        Element logoElement = logoDoc.getDocumentElement();
        float logoWidth = Float.parseFloat(logoElement.getAttribute("width"));
        float logoHeight = Float.parseFloat(logoElement.getAttribute("height"));

        // Scale width and height of both images
        qrWidth = qrWidth * scaleFactor;
        qrHeight = qrHeight * scaleFactor;
        logoWidth = logoWidth * scaleFactor;
        logoHeight = logoHeight * scaleFactor;
        float margin = this.qrMargin * scaleFactor;

        logoElement.setAttribute("width", String.valueOf(logoWidth));
        logoElement.setAttribute("height", String.valueOf(logoHeight));
        qrElement.setAttribute("width", String.valueOf(qrWidth));
        qrElement.setAttribute("height", String.valueOf(qrHeight));

        // Calculate the extra margin for the merged image
        float extraMargin = ((qrWidth - (margin * 2)) * 0.2f) - margin;

        // Calculate new width and height for merged image
        float mergedWidth = (isVertical ? qrWidth : (qrWidth + logoWidth)) + (extraMargin * 2);
        float mergedHeight = (isVertical ? (qrHeight + logoHeight) : qrHeight) + (extraMargin * 2);

        // Create a new empty SVG document for the merged result
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document mergedDoc = docBuilder.newDocument();

        // Create the root <svg> element
        Element rootElement = mergedDoc.createElement("svg");
        rootElement.setAttribute("width", String.valueOf(mergedWidth));
        rootElement.setAttribute("height", String.valueOf(mergedHeight));
        rootElement.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        mergedDoc.appendChild(rootElement);

        // Import the SVGs into the merged SVG
        if (isVertical) {
            Element firstImage = (Element) mergedDoc.importNode(logoElement, true);
            firstImage.setAttribute("x", String.valueOf(extraMargin));
            firstImage.setAttribute("y", String.valueOf(extraMargin));
            rootElement.appendChild(firstImage);

            Element secondImage = (Element) mergedDoc.importNode(qrElement, true);
            secondImage.setAttribute("x", String.valueOf(extraMargin));
            secondImage.setAttribute("y", String.valueOf(logoHeight + extraMargin));
            rootElement.appendChild(secondImage);
        } else {
            Element firstImage = (Element) mergedDoc.importNode(qrElement, true);
            firstImage.setAttribute("x", String.valueOf(extraMargin));
            firstImage.setAttribute("y", String.valueOf(extraMargin));
            rootElement.appendChild(firstImage);

            Element secondImage = (Element) mergedDoc.importNode(logoElement, true);
            secondImage.setAttribute("x", String.valueOf(qrWidth + extraMargin));
            secondImage.setAttribute("y", String.valueOf(extraMargin));
            rootElement.appendChild(secondImage);
        }

        return serializeDocument(mergedDoc);
    }

    /**
     * Function to serialize the SVG Document to string
     *
     * @param doc the document to be serialized
     * @return the serialized document in string format
     * @throws Exception if an error occurs while serializing the document
     */
    protected String serializeDocument(Document doc) throws Exception {
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.transform.Transformer t = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
        t.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
        t.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        t.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");
        t.transform(new javax.xml.transform.dom.DOMSource(doc), new javax.xml.transform.stream.StreamResult(sw));
        return sw.toString();
    }

    public String getVerifierUri() {
        return verifierUri;
    }

}