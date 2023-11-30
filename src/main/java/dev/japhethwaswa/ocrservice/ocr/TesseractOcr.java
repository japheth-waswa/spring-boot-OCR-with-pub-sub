package dev.japhethwaswa.ocrservice.ocr;

import dev.japhethwaswa.ocrservice.OcrService;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;


public class TesseractOcr {

    private boolean deleteImage, deletePdf;

    public TesseractOcr() {
        this(false, false);
    }

    public TesseractOcr(boolean deleteImage, boolean deletePdf) {
        this.deleteImage = deleteImage;
        this.deletePdf = deletePdf;
    }

    public String extractTextFromImage(String imagePath, String dirName) {
        String uri = imagePath;
        //parse dirName & create if it does not exist
        Path dirPath = Paths.get(OcrService.dotenv != null ? OcrService.dotenv.get("FILES_STORE") : "./assets/", dirName);
        //create the dir
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return null;
            }
        }
        String dirLocation = dirPath.toString();

        //check if is image,if so,convert it to png
        boolean isImg = isImage(uri);
        if (isImg) {
            String imageURI = convertImageToPng(uri, dirLocation);

            //if image conversion was not successful
            if (imageURI == null) return null;
            uri = imageURI;

        }

        //tesseract extract content
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("./tessdata");
        String result = null;
        try {
            result = tesseract.doOCR(new File(uri));
        } catch (TesseractException e) {
            System.out.println(e.getMessage());
        }

        if (deleteImage) {
            //delete the image
            deleteFile(imagePath);

            // delete the dirPath for imagePath
            String imagePathDirFromAssets = getDirPathFromAssets(imagePath);
            if (imagePathDirFromAssets != null) deleteDir(imagePathDirFromAssets);
        }

        //delete the converted image & its dir
        String uriDirFromAssets = getDirPathFromAssets(uri);
        if (uriDirFromAssets != null) deleteDir(uriDirFromAssets);

        return result;
    }


    private boolean isImage(String uri) {
        try {
            return ImageIO.read(new File(uri)) != null;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private String convertImageToPng(String inputImagePath, String dirPath) {
        try {
            String outputImagePath = Paths.get(dirPath, new Random().nextInt(10_000_000, 100_000_000) + ".png").toString();

            // Read the JPEG image
            BufferedImage originalImage = ImageIO.read(new File(inputImagePath));
            // Create a new BufferedImage with transparency (for PNG)
            BufferedImage convertedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            // Draw the original image onto the new image with transparency
            convertedImage.getGraphics().drawImage(originalImage, 0, 0, null);
            // Write the new image to the specified file in PNG format
            ImageIO.write(convertedImage, "png", new File(outputImagePath));
            return outputImagePath;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Map<Integer, String> extractContentFromPdf(String inputPdfPath, String dirName) {

        Map<Integer, String> pdfContent = new HashMap<>();

        try {

            //parse dirName & create if it does not exist
            Path dirPath = Paths.get(OcrService.dotenv != null ? OcrService.dotenv.get("FILES_STORE") : "./assets/", dirName);
            //create the dir
            if (!Files.exists(dirPath)) Files.createDirectories(dirPath);
            String dirLocation = dirPath.toString();

            // Load the PDF document
            PDDocument document = Loader.loadPDF(new File(inputPdfPath));

            //extract pdf text
            pdfContent = extractPdfText(document);

            //extract pdf images
            extractPdfImages(document, pdfContent, dirLocation);

            // Close the PDF document
            document.close();

            //delete pdf if allowed
            if (deletePdf) {
                //delete pdf
                deleteFile(inputPdfPath);
                String pdfFromAssets = getDirPathFromAssets(inputPdfPath);
                if (pdfFromAssets != null) deleteDir(pdfFromAssets);

                //delete images from disk
                String dirFromAssets = getDirPathFromAssets(dirLocation);
                if (dirFromAssets != null) deleteDir(dirFromAssets);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return pdfContent;
    }

    private Map<Integer, String> extractPdfText(PDDocument document) {
        Map<Integer, String> pdfPages = new HashMap<>();
        try {
            PDFTextStripper textStripper = new PDFTextStripper();
            for (PDPage page : document.getPages()) {
                int pageNum = document.getPages().indexOf(page) + 1;
                textStripper.setStartPage(pageNum);
                textStripper.setEndPage(pageNum);
                String text = textStripper.getText(document);

                //add in map
                pdfPages.put(pageNum, text != null ? text : "");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return pdfPages;
    }

    private void extractPdfImages(PDDocument document, Map<Integer, String> pdfPages, String dirPath) {
        Random random = new Random();
        PDPageTree pages = document.getPages();
        String imageDirName = "" + new Random().nextInt(500_000_00, 1_000_000_000);

        //Iterate through each page
        for (PDPage page : pages) {
            int pageNum = document.getPages().indexOf(page) + 1;
            //get page resources including images
            PDResources resources = page.getResources();

            //extract image from the resources
            for (COSName cosName : resources.getXObjectNames()) {
                if (resources.isImageXObject(cosName)) {
                    try {
                        PDImageXObject image = (PDImageXObject) resources.getXObject(cosName);
                        //save image to a file
                        String imagePath = Paths.get(dirPath, pageNum + "-" + random.nextInt(100_000_000, 500_000_000) + ".png").toString();
                        ImageIO.write(image.getImage(), "png", new File(imagePath));
                        String imageText = extractTextFromImage(imagePath, imageDirName);
                        String pageText = pdfPages.get(pageNum);
                        //update pdf with image text
                        if (imageText != null)
                            pdfPages.put(pageNum, pageText != null ? pageText + " " + imageText : imageText);

                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }

    }

    private String getDirPathFromAssets(String dirLocation) {
        Path dirPath = Paths.get(dirLocation);
        Iterator<Path> pathIterator = dirPath.iterator();
        List<String> dirs = new ArrayList<>();
        boolean assetsDirFound = false;

        //go all the way until you achieve the next directory after assets directory
        while (pathIterator.hasNext()) {
            String dir = pathIterator.next().toString();
            dirs.add(dir);

            //if assets dir found break out.
            if (assetsDirFound) break;

            if (dir.equalsIgnoreCase("assets")) {
                assetsDirFound = true;
            }
        }

        if (dirs.isEmpty()) return null;

        //ensure the last dir is not assets since deleting assets dir will clear the entire dir structure of unintended dirs
        if (dirs.get(dirs.size() - 1).equalsIgnoreCase("assets")) return null;

        StringBuilder stringBuilder = new StringBuilder();
        for (String item : dirs) {
            if (!stringBuilder.isEmpty()) {
                stringBuilder.append("/");
            }
            stringBuilder.append(item);
        }
        return stringBuilder.toString();
    }

    private boolean deleteFile(String filePath) {
        Path path = Paths.get(filePath);
        try {
            Files.delete(path);
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private boolean deleteDir(String dirPath) {
        Path path = Paths.get(dirPath);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

}
