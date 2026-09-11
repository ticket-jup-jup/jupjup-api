package org.example.jubjubapi.seat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.jubjubapi.performance.entity.Performance;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    private Performance performance;

    private Long externalSeatId;

    @Column(nullable = false, length = 1)
    private String section;

    @Column(nullable = false, length = 2)
    private String seatRow;

    @Column(nullable = false)
    private Integer seatNumber;

    public Seat(Performance performance, Long externalSeatId, String section, String seatRow, Integer seatNumber) {
        this.performance = performance;
        this.externalSeatId = externalSeatId;
        this.section = section;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
    }

    public boolean isSameInfo(Performance performance, String section, String seatRow, Integer seatNumber) {
        return this.performance.equals(performance)
                && this.section.equals(section)
                && this.seatRow.equals(seatRow)
                && this.seatNumber.equals(seatNumber);
    }
}
