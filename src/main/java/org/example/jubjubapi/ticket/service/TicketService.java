package org.example.jubjubapi.ticket.service;

import lombok.RequiredArgsConstructor;
import org.example.jubjubapi.ticket.dto.TicketResponse;
import org.example.jubjubapi.ticket.entity.Ticket;
import org.example.jubjubapi.ticket.entity.TicketStatus;
import org.example.jubjubapi.ticket.exception.TicketErrorCode;
import org.example.jubjubapi.ticket.exception.TicketException;
import org.example.jubjubapi.ticket.repository.TicketRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;

    //티켓목록조회
    public List<TicketResponse> getTickets(Long performanceId, TicketStatus status,
                                           int page, int size) {
        if (performanceId != null) {
            requirePositiveId(performanceId);
        }
        return ticketRepository.search(performanceId, status, pageable(page, size))
                .stream().map(TicketResponse::from).toList();
    }
    //티켓 상세 조회
    public TicketResponse getTicket(Long ticketId) {
        requirePositiveId(ticketId);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketException(TicketErrorCode.TICKET_NOT_FOUND));
        return TicketResponse.from(ticket);
    }


    //티켓 데이터 삭제
    @Transactional
    public void deleteTicket(Long ticketId) {
        requirePositiveId(ticketId);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> new TicketException(TicketErrorCode.TICKET_NOT_FOUND) );
        if (ticketRepository.countReservationReferences(ticketId)>0){
            throw new TicketException(TicketErrorCode.TICKET_IN_USE);
        }

        if (ticketRepository.countRestrictiveReservationForeignKeys() == 0) {
            throw new TicketException(TicketErrorCode.TICKET_DELETE_NOT_READY);
        }
        try {
            ticketRepository.delete(ticket);
            ticketRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new TicketException(TicketErrorCode.TICKET_IN_USE_FK);
        }
    }
    //공통함수
    //ID 검증
    private void requirePositiveId(Long id) {
        if (id == null || id <= 0) {
            throw new TicketException(TicketErrorCode.INVALID_ID);
        }
    }
    //페이지 요청 객체 생성
    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
            throw new TicketException(TicketErrorCode.INVALID_PAGINATION);
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    }


}
