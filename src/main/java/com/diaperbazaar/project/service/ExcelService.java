package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.ProductCreateRequestDTO;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ExcelService {

    public List<ProductCreateRequestDTO> parseExcel(MultipartFile file) throws Exception {
        return null;
//        List<ProductCreateRequestDTO> products = new ArrayList<>();
//
//        Workbook workbook = WorkbookFactory.create(file.getInputStream());
//        Sheet sheet = workbook.getSheetAt(0);
//
//        boolean firstRow = true;
//
//        for (Row row : sheet) {
//
//            if (firstRow) {
//                firstRow = false;
//                continue; // skip header row
//            }
//
//            ProductCreateRequestDTO dto = new ProductCreateRequestDTO();
//
//            dto.setName(getString(row, 0));
//            dto.setSlug(getString(row, 1));
//            dto.setDescription(getString(row, 2));
//            String features = getString(row,3);
//            if(features !=null && !features.isEmpty()){
//                dto.setFeatures(Arrays.asList(features.split(",")));
//            }
//            dto.setPrice(getDouble(row, 4));
//            dto.setOriginalPrice(getDouble(row, 5));
//            dto.setImage(getString(row, 6));
//
//            // Convert comma-separated images → List<String>
//            String images = getString(row, 7);
//            if (images != null && !images.isEmpty()) {
//                dto.setImages(Arrays.asList(images.split(",")));
//            }
//
//            dto.setRating(getDouble(row, 8));
//            dto.setReviewCount((getInteger(row, 9)));
//            dto.setInStock(Boolean.parseBoolean(getString(row, 10)));
//            dto.setSku(getString(row, 11));
//            dto.setBrandId( getLong(row, 12));
//            dto.setCategoryId( getLong(row, 13));
//
//            products.add(dto);
//        }
//
//        workbook.close();
//        return products;
    }

    private String getString(Row row, int cellNum) {
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

    private Long getLong(Row row, int cellNum) {
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

    private Integer getInteger(Row row, int cellNum) {
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


    private Double getDouble(Row row, int cellNum) {
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
