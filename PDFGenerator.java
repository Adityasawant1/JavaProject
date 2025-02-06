/*
    javac -cp ".;c:\*" PDFDemo.java
    java -cp ".;c:\*" PDFDemo
    
    export CLASSPATH=/root/*:/root:.
*/
import java.io.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.Desktop;

class PDFGenerator
{    
    public static void main(String[] args)
    { 
        try
        {
            String path="Test.pdf";
            OutputStream file = new FileOutputStream(new File(path));
            Document document = new Document();
            PdfWriter.getInstance(document, file);
            document.open();
            
            // PDF File Properties
            document.addTitle("I am in Title");
            document.addSubject("I am in Subject");
            document.addKeywords("I am in Keyword");
            document.addAuthor("I am Author");
            document.addCreator("I am Creator");
            
            // Write in a PDF
            document.add(new Paragraph("AAAAAaaaaaaaaaaaaa"));
            document.add(new Paragraph("Bbbb \t\t Bbbbb \t Bbbb"));
            document.add(new Paragraph("\n\n\n CCCCC \n\n "));
            
            // Table Creation
            PdfPTable table = new PdfPTable(3);
            PdfPCell c1;
            
            table.addCell(new Phrase("Table Header 1"));
            
            c1 = new PdfPCell(new Phrase("Table Header 2"));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            //            c1.setFixedHeight(55);
            table.addCell(c1);
            
            c1 = new PdfPCell(new Phrase("Table Header 3"));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);
            
            table.setHeaderRows(1);
            
            table.addCell("1.0");
            table.addCell("1.1");
            table.addCell("1.2");
            
            table.addCell("2.0");
            table.addCell("2.1");
            table.addCell("2.2");
            
            for(int i=0;i<30;i++)
                 table.addCell(""+new java.util.Random().nextInt(100));
            
            document.add(table);
            document.add(new Paragraph(" "));
            
            /* ****************************************** */
                table = new PdfPTable(3);
                
                c1 = new PdfPCell(new Phrase("Cell with colspan 3"));
                c1.setColspan(3);
                table.addCell(c1);
                
                c1 = new PdfPCell(new Phrase("Cell with rowspan 2"));
                c1.setRowspan(2);
                table.addCell(c1);
                
                table.addCell("row 1; cell 1");
                table.addCell("row 1; cell 2");
                table.addCell("row 2; cell 1");
                table.addCell("row 2; cell 2");
                
                document.add(table);
                document.add(new Paragraph(" "));

            /* ****************************************** */
            
                // Add List
                // List list = new List(false);
                 List list = new List(true);
                list.add(new ListItem("First point"));
                list.add(new ListItem("Second point"));
                list.add(new ListItem("Third point"));
                document.add(list);
                    
            document.close();
            file.close();
            
            // To open the pdf file in only windows
            // Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + path);
            
            //To open the pdf file in linux & windows
            Desktop desktop = Desktop.getDesktop();
            desktop.open(new java.io.File(path));			
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}        
