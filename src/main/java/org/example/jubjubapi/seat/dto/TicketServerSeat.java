package org.example.jubjubapi.seat.dto;

import lombok.Getter;

@Getter
public class TicketServerSeat {

    private final Long id;
    private final Long performanceId;
    private final String section;
    private final String seatRow;
    private final Integer seatNumber;

    public TicketServerSeat(Long id, Long performanceId, String section, String seatRow, Integer seatNumber) {
        this.id = id;
        this.performanceId = performanceId;
        this.section = section;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
    }
}