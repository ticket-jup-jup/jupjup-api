package org.example.jubjubapi.seat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long externalSeatId;

    @Column(nullable = false, length = 1)
    private String section;

    @Column(nullable = false, length = 2)
    private String seatRow;

    @Column(nullable = false)
    private Integer seatNumber;

    public Seat(Long externalSeatId, String section, String seatRow, Integer seatNumber) {
        this.externalSeatId = externalSeatId;
        this.section = section;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
    }
}
