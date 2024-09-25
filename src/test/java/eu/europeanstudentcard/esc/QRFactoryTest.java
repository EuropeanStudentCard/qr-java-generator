package eu.europeanstudentcard.esc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URI;
import java.util.Objects;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QRFactoryTest {

    @InjectMocks
    @Spy
    QRFactory qrService;

    @BeforeEach()
    public void setUp() {
        Mockito.doReturn("https://s.esc-r.eu/").when(qrService).getVerifierUri();
    }

    @Test
    public void givenNoArguments_whenCreate_ThenResultIsEqual() {
        QRFactory qrFactory = QRFactory.create();
        Assertions.assertEquals("https://s.esc-r.eu/", qrFactory.getVerifierUri());
    }

    @Test
    public void givenHost_whenCreate_ThenResultIsEqual() {
        QRFactory qrFactory = QRFactory.create("host");
        Assertions.assertEquals("host", qrFactory.getVerifierUri());
    }

    @Test
    public void givenValidData_whenGenerateQR_thenNoExceptionIsThrown() {
        Assertions.assertDoesNotThrow(() -> qrService.areValidParameters( "vertical", "normal", "S"));
    }

    @Test
    public void givenInvalidOrientation_whenGenerateQR_thenExceptionIsThrown() {
        Assertions.assertThrows(QRFactoryException.class,
                () -> qrService.areValidParameters( "vertical1", "normal", "S"));
    }

    @Test
    public void givenInvalidColours_whenGenerateQR_thenExceptionIsThrown() {
        Assertions.assertThrows(QRFactoryException.class,
                () -> qrService.areValidParameters( "vertical", "normal1", "S"));
    }

    @Test
    public void givenInvalidSize_whenGenerateQR_thenExceptionIsThrown() {
        Assertions.assertThrows(QRFactoryException.class,
                () -> qrService.areValidParameters( "vertical", "normal", "S1"));
    }

    @Test
    public void givenNullCardNumber_whenGenerateQR_thenExceptionIsThrown() throws Exception {
        Mockito.doNothing().when(qrService).areValidParameters(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Assertions.assertThrows(QRFactoryException.class,
                () -> qrService.generateQR(null, "vertical", "normal", "S"));

        Mockito.verify(qrService, Mockito.times(1)).areValidParameters(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.verify(qrService, Mockito.times(0)).generateQRCodeSvg(ArgumentMatchers.any(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean());
        Mockito.verify(qrService, Mockito.times(0)).mergeSVG(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.any(), ArgumentMatchers.anyFloat());
    }

    @Test
    public void givenValidData_whenGenerateQR_thenResultIsNotNullAndMethodIsCalled() throws Exception {
        Mockito.doNothing().when(qrService).areValidParameters(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doReturn("test").when(qrService).generateQRCodeSvg(ArgumentMatchers.any(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean());
        Mockito.doReturn("test").when(qrService).mergeSVG(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.any(), ArgumentMatchers.anyFloat());

        String result = qrService.generateQR("cardNumber", "horizontal", "normal", "S");

        Assertions.assertNotNull(result);

        Mockito.verify(qrService, Mockito.times(1)).areValidParameters(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.verify(qrService, Mockito.times(1)).generateQRCodeSvg(ArgumentMatchers.any(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean());
        Mockito.verify(qrService, Mockito.times(1)).mergeSVG(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.any(), ArgumentMatchers.anyFloat());
    }

    @Test
    public void givenData_whenGenerateQRCodeSvg_thenResultIsNotNull() throws Exception {
        String result = qrService.generateQRCodeSvg("content",41, 41, true);

        Assertions.assertNotNull(result);
    }

    @Test
    public void givenHorizontalOrientation_whenMergeSVG_thenResultIsNotNullAndMethodIsCalled() throws Exception {
        Mockito.doReturn("test").when(qrService).serializeDocument(ArgumentMatchers.any());
        URI imageURI = Objects.requireNonNull(getClass().getClassLoader().getResource("logos/horizontal_normal.svg")).toURI();

        String result = qrService.mergeSVG(new File(imageURI), new File(imageURI), false, new ClassPathResource("logos/horizontal_normal.svg"), 1.0f);

        Assertions.assertNotNull(result);

        Mockito.verify(qrService, Mockito.times(1)).serializeDocument(ArgumentMatchers.any());
    }

    @Test
    public void givenVerticalOrientation_whenMergeSVG_thenResultIsNotNullAndMethodIsCalled() throws Exception {
        Mockito.doReturn("test").when(qrService).serializeDocument(ArgumentMatchers.any());
        String imageURI = Objects.requireNonNull(getClass().getClassLoader().getResource("logos/horizontal_normal.svg")).getFile();

        String result = qrService.mergeSVG(new File(imageURI), new File(imageURI), true, new ClassPathResource("logos/horizontal_normal.svg"), 1.0f);

        Assertions.assertNotNull(result);

        Mockito.verify(qrService, Mockito.times(1)).serializeDocument(ArgumentMatchers.any());
    }

    @Test
    public void givenDoc_whenSerializeDocument_thenResultIsNotNull() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document mergedDoc = docBuilder.newDocument();

        String result = qrService.serializeDocument(mergedDoc);

        Assertions.assertNotNull(result);;
    }

}