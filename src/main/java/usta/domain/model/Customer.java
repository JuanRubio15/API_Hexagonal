package usta.domain.model;

import lombok.Builder;
import lombok.Getter;

/**Clase que representa a un clienta, para el sistema de facturación o a un paciente para el sistema externo*/
@Getter
public final class Customer {
    private final Long id;
    private final String identificationNumber;
    private final String identificationType;
    private final String verificationDigit;
    private final String firstName;
    private final String middleName;
    private final String firstSurname;
    private final String secondSurname;
    private final String displayName;
    private final String address;
    private final String phone;
    private final String email;
    private final String cityCode;

    @Builder
    private Customer(
            Long id,
            String identificationNumber,
            String identificationType,
            String verificationDigit,
            String firstName,
            String middleName,
            String firstSurname,
            String secondSurname,
            String displayName,
            String address,
            String phone,
            String email,
            String cityCode) {
        if (identificationNumber == null || identificationNumber.isBlank() || identificationType == null ||
                identificationType.isBlank() || firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("Se requieren identificación, tipo y primer nombre o razón social.");
        }

        this.id = id;
        this.identificationNumber = identificationNumber;
        this.identificationType = identificationType;
        this.verificationDigit = verificationDigit;
        this.firstName = firstName;
        this.middleName = middleName;
        this.firstSurname = firstSurname;
        this.secondSurname = secondSurname;
        this.displayName = displayName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.cityCode = cityCode;
    }
}
