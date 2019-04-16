package hb.kg.common.util.office;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 按照类型获取office文档的值
 */
public class OfficeUtil {
    /**
     * 按照excel的格子数据类型返回对应的值
     */
    public static String getCellValue(Cell cell) {
        Object result = "";
        if (cell != null) {
            switch (cell.getCellTypeEnum()) {
            case STRING:
                result = cell.getStringCellValue();
                break;
            case NUMERIC:
                result = cell.getNumericCellValue();
                break;
            case BOOLEAN:
                result = cell.getBooleanCellValue();
                break;
            case FORMULA:
                result = cell.getCellFormula();
                break;
            case ERROR:
                result = cell.getErrorCellValue();
                break;
            case BLANK:
            default:
                break;
            }
        }
        return result.toString();
    }

    /**
     * 根据文档类型选择Workbook
     * @throws IOException
     */
    public static Workbook getWorkbookByFilename(String filename,
                                                 InputStream stream)
            throws IOException {
        Workbook rsObj = null;
        if (filename == null || filename.length() < 1) {
            return null;
        }
        String[] fileParts = filename.split("\\.");
        if (fileParts.length < 2) {
            return null;
        }
        String fileType = fileParts[fileParts.length - 1].trim();
        switch (fileType) {
        case "doc":
        case "xls":
        case "ppt":
            rsObj = new HSSFWorkbook(stream);
            break;
        case "docx":
        case "xlsx":
        case "pptx":
            rsObj = new XSSFWorkbook(stream);
            break;
        default:
            break;
        }
        return rsObj;
    }
}
