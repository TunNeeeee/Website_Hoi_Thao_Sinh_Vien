package com.hutech.hoithao.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "Sport")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Tên môn thể thao
    @Column(name = "sport_name", nullable = false)
    @NotBlank(message = "Tên môn thể thao không được để trống.")
    private String sportName;

    // Thời gian bắt đầu thi đấu
    @Column(name = "time_start", nullable = false)
    @NotNull(message = "Ngày bắt đầu không được để trống.")
    @Future(message = "Ngày bắt đầu phải là một ngày trong tương lai.")
    private LocalDate startDate;

    // Thời gian kết thúc thi đấu
    @Column(name = "time_end", nullable = false)
    @NotNull(message = "Ngày kết thúc không được để trống.")
    @Future(message = "Ngày kết thúc phải là một ngày trong tương lai.")
    private LocalDate endDate;

    // Số lượng đội tối đa
    @Column(name = "number_team", nullable = false)
    @NotNull(message = "Số lượng đội tối đa không được để trống.")
    @Min(value = 1, message = "Số lượng đội tối đa phải lớn hơn hoặc bằng 1.")
    private Integer numberTeamMax;

    // Số lượng thành viên mỗi đội
    @Column(name = "number_member", nullable = false)
    @NotNull(message = "Số lượng thành viên không được để trống.")
    @Min(value = 1, message = "Số lượng thành viên mỗi đội phải lớn hơn hoặc bằng 1.")
    private Integer numberMember;

    // Phí ký quỹ
    @Column(name = "phi_ky_quy", nullable = false)
    @NotNull(message = "Phí ký quỹ không được để trống.")
    @Min(value = 0, message = "Phí ký quỹ phải lớn hơn hoặc bằng 0.")
    private Integer phiKyQuy;

    // Phí tham gia
    @Column(name = "phi_tham_gia", nullable = false)
    @NotNull(message = "Phí tham gia không được để trống.")
    @Min(value = 0, message = "Phí tham gia phải lớn hơn hoặc bằng 0.")
    private Integer phiThamGia;

    // Trạng thái của môn thể thao: -1: Mở đăng ký, 0: Da xoa, 1: Đã kết thúc
    @Column(nullable = false)
    @NotNull(message = "Trạng thái không được để trống.")
    @Min(value = -1, message = "Trạng thái phải là -1, 0 hoặc 1.")
    @Max(value = 1, message = "Trạng thái phải là -1, 0 hoặc 1.")
    private Integer status;

    // Quan hệ với sự kiện
    @ManyToOne
    @JoinColumn(name = "id_event", nullable = false)
    @NotNull(message = "Hội thao không được để trống.")
    private Event event;

    // Danh sách đội
    @OneToMany(mappedBy = "sport")
    private Set<Team> listTeam;

    // Quan hệ với định dạng thi đấu
    @ManyToOne
    @JoinColumn(name = "id_format")
    private Format format;
}
