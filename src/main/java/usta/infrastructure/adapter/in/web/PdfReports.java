package usta.infrastructure.adapter.in.web;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import usta.domain.model.Customer;
import usta.domain.model.HealthBillingData;
import usta.domain.model.BillingDocument;
import usta.domain.model.Payment;
import usta.application.port.in.QueryIntegration;

import usta.domain.model.Item;
import java.util.function.Function;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

// Arma el documento PDF de la factura con OpenPDF
public final class PdfReports {

    // Datos del emisor que salen en el encabezado (vienen de application.properties)
    public record Issuer(String name, String nit, String address) {
    }

    private static final Locale CO = Locale.of("es", "CO");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color PRIMARY = new Color(13, 110, 253);   // azul de Bootstrap
    private static final Color LIGHT = new Color(233, 236, 239);

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PRIMARY);
    private static final Font ISSUER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY);
    private static final Font TEXT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font HEAD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font BIG = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PRIMARY);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

    private PdfReports() {
    }

    // Factura de un documento: encabezado, cliente, ítems, totales, pagos y datos de salud
    public static byte[] invoice(BillingDocument doc, Issuer issuer) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 48);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, output);
            document.open();

            document.add(header(doc, issuer));
            document.add(infoTable(doc));

            document.add(section("CLIENTE"));
            document.add(clientTable(doc.getCustomer()));

            document.add(section("DETALLE"));
            document.add(itemsTable(doc.getItems()));
            document.add(totalsTable(doc));

            if (!isEmpty(doc.getPayments())) {
                document.add(section("FORMAS DE PAGO"));
                document.add(paymentsTable(doc.getPayments()));
                Paragraph paid = new Paragraph("Total en formas de pago: " + money(sum(doc.getPayments(), Payment::getAmount)), BOLD);
                paid.setAlignment(Element.ALIGN_RIGHT);
                paid.setSpacingBefore(4);
                document.add(paid);
            }

            if (!isEmpty(doc.getHealthBillingData())) {
                document.add(section("DATOS DE SALUD"));
                document.add(healthTable(doc.getHealthBillingData()));
            }

            if (!blank(doc.getDescription()) || !blank(doc.getNotes())) {
                document.add(section("OBSERVACIONES"));
                if (!blank(doc.getDescription())) {
                    document.add(new Paragraph(doc.getDescription(), TEXT));
                }
                if (!blank(doc.getNotes())) {
                    document.add(new Paragraph(doc.getNotes(), TEXT));
                }
            }

            Paragraph note = new Paragraph("Documento generado el " + DATE_TIME.format(LocalDateTime.now())
                    + ". Representación gráfica del documento; no reemplaza la factura electrónica "
                    + "validada por la DIAN.", SMALL);
            note.setSpacingBefore(24);
            document.add(note);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF de la factura", e);
        } finally {
            document.close();
        }
        return output.toByteArray();
    }

    // ---------- Bloques del documento ----------

    // Encabezado: emisor a la izquierda, "FACTURA" y su número a la derecha
    private static PdfPTable header(BillingDocument doc, Issuer issuer) {
        PdfPTable table = table(60, 40);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.addElement(new Paragraph(issuer.name(), ISSUER));
        left.addElement(new Paragraph("NIT: " + issuer.nit(), TEXT));
        left.addElement(new Paragraph(issuer.address(), TEXT));
        table.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        Paragraph title = new Paragraph("FACTURA", TITLE);
        title.setAlignment(Element.ALIGN_RIGHT);
        Paragraph number = new Paragraph("No. " + invoiceNumber(doc), BOLD);
        number.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(title);
        right.addElement(number);
        table.addCell(right);
        return table;
    }

    private static PdfPTable infoTable(BillingDocument doc) {
        PdfPTable table = table(18, 32, 18, 32);
        table.setSpacingBefore(10);
        keyValue(table, "Fecha de emisión:", date(doc.getIssueDate()));
        keyValue(table, "Vencimiento:", date(doc.getDueDate()));
        keyValue(table, "Plazo:", doc.getPaymentTermDays() == null ? "" : doc.getPaymentTermDays() + " días");
        keyValue(table, "Periodo:", period(doc.getBillingPeriod()));
        keyValue(table, "Sede:", text(doc.getWarehouseCode()));
        keyValue(table, "Documento afectado:", text(doc.getAffectedDocument()));
        table.completeRow();
        return table;
    }

    private static PdfPTable clientTable(Customer client) {
        PdfPTable table = table(14, 36, 14, 36);
        keyValue(table, "Cliente:", clientName(client));
        keyValue(table, "NIT / CC:", text(client.getIdentificationNumber()) + (blank(client.getVerificationDigit()) ? "" : "-" + client.getVerificationDigit()));
        keyValue(table, "Dirección:", text(client.getAddress()));
        keyValue(table, "Teléfono:", text(client.getPhone()));
        keyValue(table, "Correo:", text(client.getEmail()));
        keyValue(table, "Ciudad:", text(client.getCityCode()));
        table.completeRow();
        return table;
    }

    private static PdfPTable itemsTable(List<Item> items) {
        PdfPTable table = table(16, 10, 20, 10, 20, 24);
        for (String h : new String[]{"Código", "Cant.", "Valor unit.", "% IVA", "IVA", "Total"}) {
            table.addCell(headCell(h));
        }
        table.setHeaderRows(1);
        if (items != null) {
            for (Item i : items) {
                table.addCell(bodyCell(code(i), Element.ALIGN_LEFT));
                table.addCell(bodyCell(quantity(i.getQuantity()), Element.ALIGN_RIGHT));
                table.addCell(bodyCell(money(i.getUnitAmount()), Element.ALIGN_RIGHT));
                table.addCell(bodyCell(percent(i.getVatRate()), Element.ALIGN_RIGHT));
                table.addCell(bodyCell(money(i.getVatAmount()), Element.ALIGN_RIGHT));
                table.addCell(bodyCell(money(i.getLineAmount()), Element.ALIGN_RIGHT));
            }
        }
        return table;
    }

    private static PdfPTable totalsTable(BillingDocument doc) {
        PdfPTable table = table(58, 42);
        table.setWidthPercentage(48);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setSpacingBefore(8);

        var items = doc.getItems();
        var charges = sum(doc.getAdjustments(), a -> a.signedAmount().max(BigDecimal.ZERO));
        var discounts = sum(doc.getAdjustments(), a -> a.signedAmount().negate().max(BigDecimal.ZERO));
        var consumptionTax = sum(items, Item::getConsumptionTaxAmount);
        var deductions = sum(doc.getHealthBillingData(), h -> h.getCopayment().add(h.getModeratingFee())
                .add(h.getSharedPayments()).add(h.getAdvances()));
        var net = doc.netAmount();

        totalRow(table, "Subtotal", sum(items, Item::getLineAmount), false);
        totalRow(table, "IVA", sum(items, Item::getVatAmount), false);
        if (positive(consumptionTax)) totalRow(table, "Impuesto al consumo", consumptionTax, false);
        if (positive(charges)) totalRow(table, "(+) Recargos", charges, false);
        if (positive(discounts)) totalRow(table, "(-) Descuentos", discounts, false);
        totalRow(table, "TOTAL", net, true);
        if (positive(deductions)) {
            totalRow(table, "(-) Copagos y anticipos", deductions, false);
            totalRow(table, "VALOR A PAGAR", net.subtract(deductions), true);
        }
        return table;
    }

    private static PdfPTable paymentsTable(List<Payment> payments) {
        PdfPTable table = table(40, 15, 20, 25);
        for (String h : new String[]{"Forma de pago", "Cuota", "Vence", "Valor"}) {
            table.addCell(headCell(h));
        }
        table.setHeaderRows(1);
        for (Payment p : payments) {
            table.addCell(bodyCell(text(p.getPaymentMethodCode()) + " - cuenta " + text(p.getLedgerAccount()), Element.ALIGN_LEFT));
            table.addCell(bodyCell(text(p.getInstallmentNumber()), Element.ALIGN_LEFT));
            table.addCell(bodyCell(date(p.getDueDate()), Element.ALIGN_LEFT));
            table.addCell(bodyCell(money(p.getAmount()), Element.ALIGN_RIGHT));
        }
        return table;
    }

    private static PdfPTable healthTable(List<HealthBillingData> list) {
        PdfPTable table = table(22, 28, 22, 28);
        for (HealthBillingData h : list) {
            keyValue(table, "Modalidad de pago:", text(h.getPaymentModality()));
            keyValue(table, "Cobertura:", text(h.getBenefitPlanCoverage()));
            keyValue(table, "Contrato:", text(h.getContractNumber()));
            keyValue(table, "Póliza:", text(h.getPolicyNumber()));
            keyValue(table, "Copago:", money(h.getCopayment()));
            keyValue(table, "Cuota moderadora:", money(h.getModeratingFee()));
            keyValue(table, "Pagos compartidos:", money(h.getSharedPayments()));
            keyValue(table, "Anticipos:", money(h.getAdvances()));
        }
        table.completeRow();
        return table;
    }

    // ---------- Helpers de OpenPDF ----------

    private static PdfPTable table(float... widths) {
        PdfPTable table = new PdfPTable(widths.length);
        table.setWidthPercentage(100);
        try {
            table.setWidths(widths);
        } catch (DocumentException e) {
            throw new IllegalStateException("Anchos de columna inválidos", e);
        }
        return table;
    }

    private static Paragraph section(String title) {
        Paragraph paragraph = new Paragraph(title, SECTION);
        paragraph.setSpacingBefore(12);
        paragraph.setSpacingAfter(4);
        return paragraph;
    }

    private static void keyValue(PdfPTable table, String label, String value) {
        table.addCell(cell(label, BOLD, Element.ALIGN_LEFT, Rectangle.NO_BORDER));
        table.addCell(cell(value, TEXT, Element.ALIGN_LEFT, Rectangle.NO_BORDER));
    }

    private static void totalRow(PdfPTable table, String label, BigDecimal value, boolean highlight) {
        Font font = highlight ? BIG : TEXT;
        PdfPCell left = cell(label, font, Element.ALIGN_LEFT, Rectangle.BOTTOM);
        PdfPCell right = cell(money(value), font, Element.ALIGN_RIGHT, Rectangle.BOTTOM);
        if (highlight) {
            left.setBackgroundColor(LIGHT);
            right.setBackgroundColor(LIGHT);
        }
        table.addCell(left);
        table.addCell(right);
    }

    private static PdfPCell headCell(String content) {
        PdfPCell cell = cell(content, HEAD, Element.ALIGN_CENTER, Rectangle.NO_BORDER);
        cell.setBackgroundColor(PRIMARY);
        return cell;
    }

    private static PdfPCell bodyCell(String content, int alignment) {
        return cell(content, TEXT, alignment, Rectangle.BOTTOM);
    }

    private static PdfPCell cell(String content, Font font, int alignment, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(border);
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setPadding(4);
        return cell;
    }

    // ---------- Formato de datos ----------

    private static String invoiceNumber(BillingDocument doc) {
        String number = !blank(doc.getDocumentNumber()) ? doc.getDocumentNumber() : doc.getExternalNumber();
        return text(doc.getDocumentType()) + (blank(number) ? "" : "-" + number);
    }

    private static String clientName(Customer c) {
        if (!blank(c.getDisplayName())) {
            return c.getDisplayName();
        }
        return String.join(" ", Stream.of(c.getFirstName(), c.getMiddleName(), c.getFirstSurname(), c.getSecondSurname())
                .filter(p -> !blank(p)).toList());
    }

    // El item trae "servicio" o "referencia" (uno u otro)
    private static String code(Item i) {
        return !blank(i.getServiceCode()) ? i.getServiceCode() : text(i.getItemReference());
    }

    // "202611" -> "11/2026"
    private static String period(String p) {
        if (blank(p) || p.length() != 6) {
            return text(p);
        }
        return p.substring(4) + "/" + p.substring(0, 4);
    }

    // Formato de dinero colombiano: $ 1.234.567,00
    private static String money(BigDecimal value) {
        return String.format(CO, "$ %,.2f", value == null ? BigDecimal.ZERO : value);
    }

    private static String quantity(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private static String percent(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString() + "%";
    }

    private static String date(LocalDate date) {
        return date == null ? "" : DATE.format(date);
    }

    private static String text(String s) {
        return s == null ? "" : s;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static <T> BigDecimal sum(List<T> rows, Function<T, BigDecimal> amount) {
        return rows.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static boolean positive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    private static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
    static byte[] processing(QueryIntegration queries) {
        return render("Historial de procesamiento", List.of("Solicitud","Documento","Fecha (UTC)","Resultado","Detalle"), table -> {
            for (var request : queries.requests()) {
                for (var event : queries.history(request.getId())) {
                    row(table,request.getId(),request.getSourceDocumentNumber(),event.getOccurredAt(),
                            event.getOperation()+" / "+event.getOutcome(),String.join(", ",event.getMessages()));
                }
            }
        });
    }
    private static byte[] render(String title,List<String> headers,java.util.function.Consumer<PdfPTable> fill) {
        var bytes = new ByteArrayOutputStream();
        var doc = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc,bytes);
        doc.open();
        try {
            doc.add(new Paragraph(title,FontFactory.getFont(FontFactory.HELVETICA_BOLD,17)));
            var table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);
            table.setSpacingBefore(12);
            headers.forEach(table::addCell);
            table.setHeaderRows(1);
            fill.accept(table);
            doc.add(table);
        } finally { doc.close(); }
        return bytes.toByteArray();
    }
    private static void row(PdfPTable table,Object... cells) {
        for (Object cell : cells) table.addCell(cell == null ? "" : cell.toString());
    }
}
