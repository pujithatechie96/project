package com.diaperbazaar.project.util;

import com.diaperbazaar.project.entity.Brand;
import com.diaperbazaar.project.entity.Category;
import com.diaperbazaar.project.entity.Product;
import com.diaperbazaar.project.entity.ProductSize;
import com.diaperbazaar.project.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class ExcelHelper {


    public static ProductRepository productRepository;

    ExcelHelper(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public static boolean hasExcelFormat(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                || contentType.equals("application/vnd.ms-excel"));
    }

    public static List<Brand> parseBrands(InputStream is) throws Exception {
        List<Brand> list = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(2);
        Iterator<Row> rows = sheet.iterator();
        if (rows.hasNext()) rows.next(); // skip header
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            Brand b = new Brand();
            b.setName(getString(currentRow, 0));
//            b.setLogo(getString(currentRow, 1));
            b.setDescription(getString(currentRow, 1));
            b.setSlug(getString(currentRow, 2));
            list.add(b);
        }
        workbook.close();
        return list;
    }

    public static List<Category> parseCategories(InputStream is) throws Exception {
        List<Category> list = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(1);
        Iterator<Row> rows = sheet.iterator();
        if (rows.hasNext()) rows.next(); // skip header
        while (rows.hasNext()) {
            Row row = rows.next();
            Category c = new Category();
            c.setName(getString(row, 0));
            c.setSlug(getString(row, 1));
            c.setDescription(getString(row, 2));
//            c.setImage(getString(row, 3));
            list.add(c);
        }
        workbook.close();
        return list;
    }

//    public static List<Product> parseProducts(InputStream is) throws Exception {
//        List<Product> list = new ArrayList<>();
//        Workbook workbook = new XSSFWorkbook(is);
//        Sheet sheet = workbook.getSheet("products");
//        if (sheet == null) sheet = workbook.getSheetAt(0);
//        Iterator<Row> rows = sheet.iterator();
//        if (rows.hasNext()) rows.next(); // skip header
//        while (rows.hasNext()) {
//            Row r = rows.next();
//            Product p = new Product();
//            p.setName(getString(r, 0));
//            p.setSlug(getString(r, 1));
//            p.setDescription(getString(r, 2));
//            p.setBrand(getLong(r, 3));
//            p.setCategory(getLong(r, 4));
//            p.setPrice(getDouble(r, 5));
//            p.setOriginalPrice(getDouble(r, 6));
//            p.setSku(getString(r, 7));
//            p.setRating(getDouble(r, 8));
//            p.setImage(getString(r, 9));
//            list.add(p);
//        }
//        workbook.close();
//        return list;
//    }

    public static List<ProductSize> parseProductSizes(InputStream is) throws Exception {
        List<ProductSize> list = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(3);
//        if (sheet == null) {
//            if (workbook.getNumberOfSheets() > 1)
//                sheet = workbook.getSheetAt(1);
//            else
//                sheet = workbook.getSheetAt(0);
//        }
        Iterator<Row> rows = sheet.iterator();
        if (rows.hasNext()) rows.next(); // skip header
        while (rows.hasNext()) {
            Row r = rows.next();
            ProductSize ps = new ProductSize();
            Long productId = getLong(r,0);
            Product product = productRepository.findById(productId).get();
            ps.setProduct(product);
            ps.setSize(getString(r, 1));
            ps.setStock((int) getDouble(r, 2).doubleValue());
            ps.setSku(getString(r, 4));
            list.add(ps);
        }
        workbook.close();
        return list;
    }

    // helpers
    public static String getString(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue()).trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    public static Long getLong(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                String v = cell.getStringCellValue().trim();
                if (v.isEmpty()) return null;
                return Long.parseLong(v);
            case NUMERIC:
                return (long) cell.getNumericCellValue();
            default:
                return null;
        }
    }

    public Integer getInteger(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                String v = cell.getStringCellValue().trim();
                if (v.isEmpty()) return null;
                return Integer.parseInt(v);
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            default:
                return null;
        }
    }


    public static Double getDouble(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                String v = cell.getStringCellValue().trim();
                if (v.isEmpty()) return null;
                return Double.parseDouble(v);
            case NUMERIC:
                return cell.getNumericCellValue();
            default:
                return null;
        }
    }
}

