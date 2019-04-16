package hb.kg.common.util.office;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import hb.kg.common.util.file.FileUtils;

public class OfficeToHtmlUtil {
    /**
     * PPT 转 HTML
     * @param path
     * @param filename
     * @return
     */
    public static void pptToHtml(String sourcePath,
                                 String id,
                                 String targetDir,
                                 String webpath) {
        File pptFile = new File(sourcePath);
        if (pptFile.exists()) {
            try {
                String type = FileUtils.getFileExtension(sourcePath);
                if ("ppt".equals(type)) {
                    String htmlStr = toImage2003(sourcePath, targetDir, id, webpath);
                    FileUtils.writeFileFromString(targetDir + "\\" + id + ".html", htmlStr, false);
                } else if ("pptx".equals(type)) {
                    String htmlStr = toImage2007(sourcePath, targetDir, id, webpath);
                    FileUtils.writeFileFromString(targetDir + "\\" + id + ".html", htmlStr, false);
                } else {
                    System.out.println("the file is not a ppt");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("file does not exist!");
        }
    }

    public static String toImage2007(String sourcePath,
                                     String targetDir,
                                     String pptFileName,
                                     String webpath)
            throws Exception {
        String htmlStr = "";
        FileInputStream is = new FileInputStream(sourcePath);
        XMLSlideShow ppt = new XMLSlideShow(is);
        is.close();
        FileUtils.createOrExistsDir(targetDir);// create html dir
        Dimension kgsize = ppt.getPageSize();
        System.out.println(kgsize.width + "--" + kgsize.height);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ppt.getSlides().size(); i++) {
            try {
                // 防止中文乱码
                for (XSLFShape shape : ppt.getSlides().get(i).getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape tsh = (XSLFTextShape) shape;
                        for (XSLFTextParagraph p : tsh) {
                            for (XSLFTextRun r : p) {
                                r.setFontFamily("宋体");
                            }
                        }
                    }
                }
                BufferedImage img = new BufferedImage(kgsize.width,
                                                      kgsize.height,
                                                      BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = img.createGraphics();
                // clear the drawing area
                graphics.setPaint(Color.white);
                graphics.fill(new Rectangle2D.Float(0, 0, kgsize.width, kgsize.height));
                // render
                ppt.getSlides().get(i).draw(graphics);
                // save the output
                String imageDir = targetDir + pptFileName + "\\";
                FileUtils.createOrExistsDir(imageDir);// create image dir
                String imagePath = imageDir + pptFileName + "-" + (i + 1) + ".png";
                String imagewebpath = pptFileName + "\\" + pptFileName + "-" + (i + 1) + ".png";
                sb.append("<html>");
                sb.append("<body>");
                sb.append("<p style=text-align:center;>");
                sb.append("<img style='display:inline-block;width:100%;' src=" + "\"" + imagewebpath
                        + "\"" + "/>");
                sb.append("</p>");
                sb.append("<br />");
                sb.append("</html>");
                sb.append("</body>");
                FileOutputStream out = new FileOutputStream(imagePath);
                javax.imageio.ImageIO.write(img, "png", out);
                out.close();
            } catch (Exception e) {
                System.out.println("第" + i + "张ppt转换出错");
            }
        }
        ppt.close();
        System.out.println("success");
        htmlStr = sb.toString();
        return htmlStr;
    }

    public static String toImage2003(String sourcePath,
                                     String targetDir,
                                     String pptFileName,
                                     String webpath) {
        String htmlStr = "";
        HSLFSlideShow ppt = null;
        try {
            ppt = new HSLFSlideShow(new HSLFSlideShowImpl(sourcePath));
            FileUtils.createOrExistsDir(targetDir);// create html dir
            Dimension kgsize = ppt.getPageSize();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < ppt.getSlides().size(); i++) {
                // 防止中文乱码
                for (HSLFShape shape : ppt.getSlides().get(i).getShapes()) {
                    if (shape instanceof HSLFTextShape) {
                        HSLFTextShape tsh = (HSLFTextShape) shape;
                        for (HSLFTextParagraph p : tsh) {
                            for (HSLFTextRun r : p) {
                                r.setFontFamily("宋体");
                            }
                        }
                    }
                }
                BufferedImage img = new BufferedImage(kgsize.width,
                                                      kgsize.height,
                                                      BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = img.createGraphics();
                // clear the drawing area
                graphics.setPaint(Color.white);
                graphics.fill(new Rectangle2D.Float(0, 0, kgsize.width, kgsize.height));
                // render
                ppt.getSlides().get(i).draw(graphics);
                String imageDir = targetDir + pptFileName + "\\";
                FileUtils.createOrExistsDir(imageDir);// create image dir
                String imagePath = imageDir + pptFileName + "-" + (i + 1) + ".png";
                String imagewebpath = pptFileName + "\\" + pptFileName + "-" + (i + 1) + ".png";
                sb.append("<html>");
                sb.append("<body>");
                sb.append("<p style=text-align:center;>");
                sb.append("<img style='display:inline-block;width:100%;' src=" + "\"" + imagewebpath
                        + "\"" + "/>");
                sb.append("</p>");
                sb.append("<br />");
                sb.append("</html>");
                sb.append("</body>");
                FileOutputStream out = new FileOutputStream(imagePath);
                javax.imageio.ImageIO.write(img, "png", out);
                out.close();
            }
            System.out.println("success");
            htmlStr = sb.toString();
        } catch (Exception e) {
        } finally {
            if (ppt != null) {
                try {
                    ppt.close();
                } catch (Exception e2) {
                }
            }
        }
        return htmlStr;
    }

    public static void main(String[] args) {
        String ctxPath = "G:\\ahbtest\\pptToHtml\\";
        String filelocalname = "高新技术企业认定专题.ppt";
        String zid = "08801";
        String webpath = "G:\\ahbtest\\pptToHtml\\webpath\\";
        OfficeToHtmlUtil.pptToHtml(ctxPath + filelocalname, zid, ctxPath, webpath);
    }
}