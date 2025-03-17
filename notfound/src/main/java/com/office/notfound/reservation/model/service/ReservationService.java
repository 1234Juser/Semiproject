package com.office.notfound.reservation.model.service;

import com.office.notfound.reservation.model.dao.ReservationMapper;
import com.office.notfound.reservation.model.dto.ReservationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    private final ReservationMapper reservationMapper;

    @Autowired
    public ReservationService(ReservationMapper reservationMapper) {
        this.reservationMapper = reservationMapper;
    }

    /**
     * 🔹 로그인한 사용자의 예약 전체 조회 (일반 사용자: 본인 예약만 / 관리자: 전체 조회)
     */
    public List<ReservationDTO> findAllReservations(Integer memberCode, boolean isAdmin) {
        if (isAdmin) {
            return reservationMapper.adminFindAllReservation();
        }
        return reservationMapper.findAllReservationByMember(memberCode);
    }

    /**
     * 🔹 로그인한 사용자의 특정 조건으로 예약 검색 (일반 사용자: 본인 예약만 / 관리자: 전체 조회)
     */
    public List<ReservationDTO> searchReservations(Integer memberCode, boolean isAdmin,
                                                   String reservationCode, String reservationDate,
                                                   String startDatetime, String endDatetime) {
        Integer reservationCodeInt = null;

        // 예약번호가 입력된 경우 Integer 변환
        if (reservationCode != null && !reservationCode.isEmpty()) {
            try {
                reservationCodeInt = Integer.parseInt(reservationCode);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("예약번호는 숫자로 입력해야 합니다.");
            }
        }

        if (isAdmin) {
            return reservationMapper.searchAdminReservation(reservationCodeInt, memberCode, reservationDate, startDatetime, endDatetime);
        }
        return reservationMapper.searchReservationByMember(memberCode, reservationCodeInt, reservationDate, startDatetime, endDatetime);
    }

    /**
     * 🔹 로그인한 사용자의 예약 취소 (본인 예약만 가능)
     */
    public void cancelMemberReservations(Integer memberCode, List<Integer> reservationCodes) {
        if (reservationCodes != null && !reservationCodes.isEmpty()) {
            reservationMapper.cancelReservationsByMember(memberCode, reservationCodes);
        }
    }

    /**
     * 🔹 관리자가 특정 예약을 삭제 (예약 취소된 건만 삭제 가능)
     */
    public void deleteReservations(List<Integer> reservationCodes) {
        if (reservationCodes != null && !reservationCodes.isEmpty()) {
            reservationMapper.deleteReservations(reservationCodes);
        }
    }

    /**
     * 🔹 일정 기간 지난 '예약취소' 상태의 예약 자동 삭제
     */
    public int deleteOldCanceledReservations() {
        return reservationMapper.deleteOldCanceledReservations();
    }


    /**
     * 🔹 예약 가능한 시간대 조회
     */
    public List<String> getAvailableTimeSlots(int officeCode, String date) {
        // 모든 시간대 생성 (0시부터 22시까지, 2시간 간격)
        List<String> allTimeSlots = new ArrayList<>();
        for (int i = 0; i < 24; i += 2) {
            allTimeSlots.add(String.format("%02d:00", i));
        }

        // 이미 예약된 시간대 조회
        List<String> bookedTimes = reservationMapper.findBookedTimeSlots(officeCode, date);
        
        // 예약 가능한 시간대만 필터링
        allTimeSlots.removeAll(bookedTimes);
        
        return allTimeSlots;
    }

    /**
     * 🔹 예약 등록
     */
    public void registerReservation(ReservationDTO reservation) {
        // 예약 시간 중복 체크
        boolean isTimeSlotAvailable = reservationMapper.checkTimeSlotAvailability(
            reservation.getOfficeCode(),
            reservation.getStartDatetime(),
            reservation.getEndDatetime()
        );

        if (!isTimeSlotAvailable) {
            throw new RuntimeException("이미 예약된 시간대입니다.");
        }

        // 예약 정보 저장
        reservation.setReservationStatus("예약대기");
        reservationMapper.insertReservation(reservation);
    }
}
