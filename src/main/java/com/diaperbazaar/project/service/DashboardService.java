package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.LowStockVariantDTO;
import com.diaperbazaar.project.repository.DashboardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public List<Object> getLowStockVariants() {
        int LOW_STOCK_THRESHOLD = 2;
        return dashboardRepository.findLowStockVariants(LOW_STOCK_THRESHOLD);
    }
}
