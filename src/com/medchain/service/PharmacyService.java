package com.medchain.service;

import com.medchain.dao.PharmacyDao;
import com.medchain.model.PharmacyOwner;
import java.util.ArrayList;
import java.util.List;

/**
 * Package: com.medchain.service
 * Purpose: Business logic service managing pharmacy details, listings and admin approvals.
 */
public class PharmacyService {
    private final PharmacyDao pharmacyDao = new PharmacyDao();

    public List<PharmacyOwner> getAllPharmacies() {
        return pharmacyDao.getAllPharmacies();
    }

    public List<PharmacyOwner> getPendingPharmacies() {
        List<PharmacyOwner> all = pharmacyDao.getAllPharmacies();
        List<PharmacyOwner> pending = new ArrayList<>();
        for (PharmacyOwner p : all) {
            if ("PENDING".equals(p.getApprovalStatus())) {
                pending.add(p);
            }
        }
        return pending;
    }

    public boolean approvePharmacy(int pharmacyId) {
        return pharmacyDao.updateApprovalStatus(pharmacyId, "APPROVED");
    }

    public boolean rejectPharmacy(int pharmacyId) {
        return pharmacyDao.updateApprovalStatus(pharmacyId, "REJECTED");
    }
}
