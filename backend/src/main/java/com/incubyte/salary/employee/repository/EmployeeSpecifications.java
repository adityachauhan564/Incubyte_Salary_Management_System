package com.incubyte.salary.employee.repository;

import com.incubyte.salary.employee.dto.EmployeeSearchCriteria;
import com.incubyte.salary.employee.entity.Employee;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> matching(EmployeeSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(criteria.search())) {
                String pattern = "%" + criteria.search().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern)
                ));
            }
            if (StringUtils.hasText(criteria.country())) {
                predicates.add(cb.equal(root.get("country"), criteria.country()));
            }
            if (StringUtils.hasText(criteria.department())) {
                predicates.add(cb.equal(root.get("department"), criteria.department()));
            }
            if (StringUtils.hasText(criteria.jobTitle())) {
                predicates.add(cb.equal(root.get("jobTitle"), criteria.jobTitle()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
