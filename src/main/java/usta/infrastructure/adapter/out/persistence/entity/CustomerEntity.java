package usta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class CustomerEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "identification_number")
    public String identificationNumber;
    @Column(name = "identification_type")
    public String identificationType;
    @Column(name = "verification_digit")
    public String verificationDigit;
    @Column(name = "first_name")
    public String firstName;
    @Column(name = "middle_name")
    public String middleName;
    @Column(name = "first_surname")
    public String firstSurname;
    @Column(name = "second_surname")
    public String secondSurname;
    @Column(name = "display_name")
    public String displayName;
    @Column(name = "address")
    public String address;
    @Column(name = "phone")
    public String phone;
    @Column(name = "email")
    public String email;
    @Column(name = "city_code")
    public String cityCode;
}
