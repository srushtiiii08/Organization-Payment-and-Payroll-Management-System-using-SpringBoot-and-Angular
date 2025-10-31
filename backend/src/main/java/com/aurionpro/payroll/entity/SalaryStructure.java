package com.aurionpro.payroll.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "salary_structures",		//The uniqueness should be on employee_id + isActive (only one active salary per employee)
uniqueConstraints = @UniqueConstraint(
    columnNames = {"employee_id", "is_active"},
    name = "UK_employee_active_salary"
)
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryStructure {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

//	@OneToOne
//	@JoinColumn(name = "employee_id", nullable = false, unique = true)
//	private Employee employee;
	
	@ManyToOne(fetch = FetchType.LAZY)  //one emp should have multiple sal structure but only one active sal structure
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal basicSalary;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal hra;                   // House Rent Allowance

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal dearnessAllowance;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal providentFund;

	@Column(precision = 10, scale = 2)
	private BigDecimal otherAllowances;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal grossSalary;           // Total before deductions

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal netSalary;                 // Take home salary

	private LocalDate effectiveFrom;

	@Column(nullable = false)
	private Boolean isActive = true;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	// Method to calculate salaries
	@PrePersist
	@PreUpdate
	private void calculateSalaries() {
		this.grossSalary = basicSalary.add(hra).add(dearnessAllowance)
				.add(otherAllowances != null ? otherAllowances : BigDecimal.ZERO);

		this.netSalary = grossSalary.subtract(providentFund);
	}
}
