package com.medchain.service;

import com.medchain.dao.AdminDao;
import com.medchain.dao.CourierDao;
import com.medchain.dao.CustomerDao;
import com.medchain.dao.PharmacyDao;
import com.medchain.exception.DuplicateUserException;
import com.medchain.exception.InvalidLoginException;
import com.medchain.exception.PharmacyNotApprovedException;
import com.medchain.model.*;
import com.medchain.util.PasswordUtil;

/**
 * Package: com.medchain.service
 * Purpose: Business logic service orchestrating registrations and logins for all system roles.
 */
public class UserService {
    private final CustomerDao customerDao = new CustomerDao();
    private final PharmacyDao pharmacyDao = new PharmacyDao();
    private final AdminDao adminDao = new AdminDao();
    private final CourierDao courierDao = new CourierDao();

    public Customer registerCustomer(String fullName, String email, String password, String phone, int homeAreaId, Integer preferredPharmacyId) 
            throws DuplicateUserException {
        String hashedPassword = PasswordUtil.hashPassword(password);
        Customer customer = new Customer(0, fullName, email, hashedPassword, phone, homeAreaId, preferredPharmacyId, null);
        return customerDao.insertCustomer(customer);
    }

    public PharmacyOwner registerPharmacy(String pharmacyName, String ownerName, String email, String password, int areaId, String address) 
            throws DuplicateUserException {
        String hashedPassword = PasswordUtil.hashPassword(password);
        PharmacyOwner owner = new PharmacyOwner(0, pharmacyName, ownerName, email, hashedPassword, areaId, address, "PENDING", null);
        return pharmacyDao.insertPharmacy(owner);
    }

    public Customer loginCustomer(String email, String password) throws InvalidLoginException {
        String hashedPassword = PasswordUtil.hashPassword(password);
        return customerDao.authenticate(email, hashedPassword);
    }

    public PharmacyOwner loginPharmacy(String email, String password) 
            throws InvalidLoginException, PharmacyNotApprovedException {
        String hashedPassword = PasswordUtil.hashPassword(password);
        return pharmacyDao.authenticate(email, hashedPassword);
    }

    public Admin loginAdmin(String email, String password) throws InvalidLoginException {
        String hashedPassword = PasswordUtil.hashPassword(password);
        return adminDao.authenticate(email, hashedPassword);
    }

    public Courier loginCourier(String email, String password) throws InvalidLoginException {
        String hashedPassword = PasswordUtil.hashPassword(password);
        return courierDao.authenticate(email, hashedPassword);
    }
}
