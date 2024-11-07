package com.ssafy.enjoyTrip.domain.member.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "MEMBER")
@Getter
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long no;

    @Column(name = "member_key", unique = true, updatable = false, nullable = false)
    private String memberKey;

    @Column(name = "member_id", unique = true, nullable = false)
    private String memberId;

    @Column(nullable = false)
    private String password;

    @Column(name = "member_name", nullable = false)
    private String memberName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Builder
    public Member(String memberId, String password, String memberName,
                  String email, String phone, LocalDate birthDate, Gender gender) {
        this.memberKey = UUID.randomUUID().toString();
        this.memberId = memberId;
        this.password = password;
        this.memberName = memberName;
        this.email = email;
        this.phone = phone;
        this.joinDate = LocalDateTime.now();
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public boolean matchPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.password);
    }
}