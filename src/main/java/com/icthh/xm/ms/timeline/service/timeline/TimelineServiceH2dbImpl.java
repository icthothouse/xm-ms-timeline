package com.icthh.xm.ms.timeline.service.timeline;

import com.icthh.xm.ms.timeline.domain.XmTimeline;
import com.icthh.xm.ms.timeline.repository.jpa.TimelineJpaRepository;
import com.icthh.xm.ms.timeline.web.rest.vm.TimelinePageVM;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specifications;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class TimelineServiceH2dbImpl implements TimelineService {

    private TimelineJpaRepository timelineRepository;

    private static <T> Specification<XmTimeline> equalSpecification(T filterValue, String propertyName) {
        return (root, query, builder) -> builder.equal(root.get(propertyName), filterValue);
    }

    private static Specification<XmTimeline> lessThanOrEqualToSpecification(Instant filterValue, String propertyName) {
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get(propertyName), filterValue);
    }

    private static Specification<XmTimeline> greaterThanOrEqualToSpecification(Instant filterValue, String propertyName) {
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get(propertyName), filterValue);
    }

    private Specifications combineEqualSpecifications(Specifications prevSpec, String filterValue, String propertyName) {
        Specifications specifications = Specifications.where(equalSpecification(filterValue, propertyName));
        return prevSpec != null ? prevSpec.and(specifications) : specifications;
    }

    private Specifications combineLessThanOrEqualToSpecifications(Specifications prevSpec, Instant filterValue, String propertyName) {
        Specifications specifications = Specifications.where(lessThanOrEqualToSpecification(filterValue, propertyName));
        return prevSpec != null ? prevSpec.and(specifications) : specifications;
    }

    private Specifications combineGreaterThanOrEqualToSpecifications(Specifications prevSpec, Instant filterValue, String propertyName) {
        Specifications specifications = Specifications.where(greaterThanOrEqualToSpecification(filterValue, propertyName));
        return prevSpec != null ? prevSpec.and(specifications) : specifications;
    }

    @Override
    public void insertTimelines(XmTimeline timeline) {
        timelineRepository.save(timeline);
    }

    @Override
    public TimelinePageVM getTimelines(String msName, String userKey, String idOrKey, Instant dateFrom, Instant dateTo, String operation, String next, int limit) {

        Specifications<XmTimeline> specificationsForFiltering = null;

        if (StringUtils.isNotBlank(msName)) {
            specificationsForFiltering = combineEqualSpecifications(specificationsForFiltering, msName, "msName");
        }
        if (StringUtils.isNotBlank(userKey)) {
            specificationsForFiltering = combineEqualSpecifications(specificationsForFiltering, userKey, "userKey");
        }
        if (StringUtils.isNotBlank(operation)) {
            specificationsForFiltering = combineEqualSpecifications(specificationsForFiltering, operation, "operation");
        }
        if (Objects.nonNull(dateFrom)) {
            specificationsForFiltering = combineLessThanOrEqualToSpecifications(specificationsForFiltering, dateFrom, "dateFrom");
        }
        if (Objects.nonNull(dateTo)) {
            specificationsForFiltering = combineGreaterThanOrEqualToSpecifications(specificationsForFiltering, dateTo, "dateTo");
        }

        // TODO (method is not finished yet)

        List timelines = specificationsForFiltering != null ? timelineRepository.findAll(specificationsForFiltering) : timelineRepository.findAll();

        return new TimelinePageVM(timelines, null);
    }
}
