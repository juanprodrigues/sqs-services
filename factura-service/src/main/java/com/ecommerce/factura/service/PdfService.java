package com.ecommerce.factura.service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class PdfService {

    public File generarFactura(
            Long pedidoId)
            throws Exception {

        String nombre =
                "factura-" + pedidoId + ".pdf";

        Document document =
                new Document();

        PdfWriter.getInstance(
                document,
                new FileOutputStream(nombre));

        document.open();

        document.add(
                new Paragraph(
                        "FACTURA"));

        document.add(
                new Paragraph(
                        "Pedido: "
                                + pedidoId));

        document.add(
                new Paragraph(
                        "Fecha: "
                                + LocalDate.now()));

        document.close();

        return new File(nombre);
    }
}