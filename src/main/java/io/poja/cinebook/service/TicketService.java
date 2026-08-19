package io.poja.cinebook.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.poja.cinebook.entity.Reservation;
import io.poja.cinebook.file.bucket.BucketComponent;
import io.poja.cinebook.repository.model.JMovie;
import io.poja.cinebook.repository.model.JProjection;
import io.poja.cinebook.repository.model.JRoom;
import io.poja.cinebook.repository.model.JSeat;
import io.poja.cinebook.repository.model.JUser;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TicketService {

  private static final int QR_CODE_SIZE = 200;
  private static final float TICKET_WIDTH = 400;
  private static final float TICKET_HEIGHT = 550;
  private static final float MARGIN = 30;
  private static final float LINE_HEIGHT = 22;
  private static final float TITLE_SIZE = 18;
  private static final float BODY_SIZE = 12;

  private final BucketComponent bucketComponent;

  public String generateAndUpload(
      Reservation reservation,
      JProjection projection,
      JSeat primarySeat,
      JRoom room,
      JUser user,
      JMovie movie) {
    try {
      byte[] pdfBytes = generateTicketPdf(reservation, projection, primarySeat, room, user, movie);
      File tempFile = File.createTempFile("ticket-" + reservation.id(), ".pdf");
      tempFile.deleteOnExit();
      java.nio.file.Files.write(tempFile.toPath(), pdfBytes);

      String bucketKey = "tickets/" + reservation.id() + ".pdf";
      bucketComponent.upload(tempFile, bucketKey);

      return bucketComponent.presign(bucketKey, java.time.Duration.ofDays(365)).toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate ticket: " + e.getMessage(), e);
    }
  }

  private byte[] generateTicketPdf(
      Reservation reservation,
      JProjection projection,
      JSeat primarySeat,
      JRoom room,
      JUser user,
      JMovie movie)
      throws IOException, WriterException {

    PDDocument document = new PDDocument();
    PDPage page = new PDPage(new PDRectangle(TICKET_WIDTH, TICKET_HEIGHT));
    document.addPage(page);

    PDType1Font helvetica = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    PDType1Font helveticaBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    PDType1Font helveticaOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    try (PDPageContentStream content = new PDPageContentStream(document, page)) {
      float y = TICKET_HEIGHT - MARGIN;

      content.setNonStrokingColor(0.1f, 0.1f, 0.15f);
      content.addRect(0, y - 60, TICKET_WIDTH, 60);
      content.fill();

      content.setNonStrokingColor(1, 1, 1);
      content.beginText();
      content.setFont(helveticaBold, TITLE_SIZE);
      content.newLineAtOffset(MARGIN, y - 22);
      content.showText("CINEBOOK");
      content.endText();

      content.beginText();
      content.setFont(helvetica, BODY_SIZE);
      content.newLineAtOffset(MARGIN, y - 42);
      content.showText("Your Movie Ticket");
      content.endText();

      y -= 80;

      content.setNonStrokingColor(0, 0, 0);
      content.beginText();
      content.setFont(helveticaBold, 16);
      content.newLineAtOffset(MARGIN, y);
      content.showText(movie != null ? movie.getTitle() : "Unknown");
      content.endText();
      y -= LINE_HEIGHT + 8;

      drawLine(content, MARGIN, y, TICKET_WIDTH - MARGIN, y);
      y -= 15;

      String dateStr =
          projection.getDatetime() != null
              ? projection
                  .getDatetime()
                  .atZone(ZoneId.systemDefault())
                  .format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm"))
              : "N/A";

      content.setFont(helvetica, BODY_SIZE);
      writeField(content, helvetica, helveticaBold, MARGIN, y, "Date", dateStr);
      y -= LINE_HEIGHT + 4;
      writeField(
          content,
          helvetica,
          helveticaBold,
          MARGIN,
          y,
          "Room",
          room != null ? room.getNumber() : "N/A");
      y -= LINE_HEIGHT + 4;
      writeField(
          content,
          helvetica,
          helveticaBold,
          MARGIN,
          y,
          "Seat",
          primarySeat != null ? primarySeat.getNumber() : "N/A");
      y -= LINE_HEIGHT + 4;
      writeField(
          content,
          helvetica,
          helveticaBold,
          MARGIN,
          y,
          "Price",
          projection.getSeatPrice() != null
              ? projection.getSeatPrice().toString() + " EUR"
              : "N/A");
      y -= LINE_HEIGHT + 4;
      writeField(
          content,
          helvetica,
          helveticaBold,
          MARGIN,
          y,
          "Customer",
          user != null ? user.getFirstName() + " " + user.getLastName() : "N/A");
      y -= LINE_HEIGHT + 4;
      writeField(
          content,
          helvetica,
          helveticaBold,
          MARGIN,
          y,
          "Reservation",
          reservation.id().toString().substring(0, 8));
      y -= 25;

      drawLine(content, MARGIN, y, TICKET_WIDTH - MARGIN, y);
      y -= 15;

      String qrData = reservation.id().toString();
      BufferedImage qrImage = generateQRCode(qrData);
      PDImageXObject pdImage =
          PDImageXObject.createFromByteArray(document, bufferedImageToBytes(qrImage, "png"), "qr");

      float qrX = (TICKET_WIDTH - QR_CODE_SIZE) / 2;
      content.drawImage(pdImage, qrX, y - QR_CODE_SIZE, QR_CODE_SIZE, QR_CODE_SIZE);

      content.setNonStrokingColor(0.5f, 0.5f, 0.5f);
      content.beginText();
      content.setFont(helvetica, 8);
      content.newLineAtOffset(MARGIN, y - QR_CODE_SIZE - 12);
      content.showText("Scan at entrance");
      content.endText();

      y -= QR_CODE_SIZE + 30;
      content.setNonStrokingColor(0, 0, 0);
      content.beginText();
      content.setFont(helveticaOblique, 8);
      content.newLineAtOffset(MARGIN, y);
      content.showText("This ticket must be presented for entry. One ticket per person.");
      content.endText();
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    document.save(out);
    document.close();
    return out.toByteArray();
  }

  private void writeField(
      PDPageContentStream content,
      PDType1Font normal,
      PDType1Font bold,
      float x,
      float y,
      String label,
      String value)
      throws IOException {
    content.setNonStrokingColor(0.5f, 0.5f, 0.5f);
    content.beginText();
    content.setFont(normal, 10);
    content.newLineAtOffset(x, y);
    content.showText(label);
    content.endText();

    content.setNonStrokingColor(0, 0, 0);
    content.beginText();
    content.setFont(bold, 10);
    content.newLineAtOffset(x + 80, y);
    content.showText(value);
    content.endText();
  }

  private void drawLine(PDPageContentStream content, float x1, float y1, float x2, float y2)
      throws IOException {
    content.setNonStrokingColor(0.85f, 0.85f, 0.85f);
    content.addRect(x1, y1 - 0.5f, x2 - x1, 1);
    content.fill();
  }

  private BufferedImage generateQRCode(String data) throws WriterException {
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix =
        qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
    return MatrixToImageWriter.toBufferedImage(bitMatrix);
  }

  private byte[] bufferedImageToBytes(BufferedImage image, String format) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    javax.imageio.ImageIO.write(image, format, baos);
    return baos.toByteArray();
  }
}
