package com.example.restea.share.repository;

import static com.example.restea.share.enums.ShareBoardMessage.SHARE_BOARD_INVALID_SORT;

import com.example.restea.share.entity.QShareBoard;
import com.example.restea.share.entity.ShareBoard;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShareBoardSearchRepository {

    private final JPAQueryFactory queryFactory;

    QShareBoard shareBoard = QShareBoard.shareBoard;

    public List<ShareBoard> findAllBySortAndKeyword(
            String sort, Integer page, Integer perPage, String searchBy, String keyword) {
        return queryFactory
                .selectFrom(shareBoard)
                .where(createWhereCondition(sort, searchBy, keyword))
                .orderBy(getOrderSpecifier(sort))
                .offset((long) (page - 1) * perPage)
                .limit(perPage)
                .fetch();
    }

    private Predicate createWhereCondition(String sort, String searchBy, String keyword) {

        BooleanExpression baseCondition = shareBoard.activated.isTrue();

        if ("urgent".equals(sort)) {
            baseCondition = baseCondition.and(shareBoard.endDate.gt(LocalDateTime.now()));
        }

        if (searchBy != null && keyword != null) {
            System.out.println("searchBy: " + searchBy);
            switch (searchBy) {
                case "title":
                    baseCondition = baseCondition.and(shareBoard.title.contains(keyword));
                    break;
                case "content":
                    baseCondition = baseCondition.and(shareBoard.content.contains(keyword));
                    break;
                case "writer":
                    baseCondition = baseCondition.and(shareBoard.user.nickname.contains(keyword));
                    break;
            }
        }
        return baseCondition;
    }

    private OrderSpecifier<?> getOrderSpecifier(String sort) {
        return switch (sort) {
            case "latest" -> shareBoard.createdDate.desc();
            case "urgent" -> shareBoard.endDate.asc();
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, SHARE_BOARD_INVALID_SORT.getMessage());
        };
    }

    public int countBySortAndKeyword(String sort, String searchBy, String keyword) {
        List<ShareBoard> results = queryFactory
                .selectFrom(shareBoard)
                .where(createWhereCondition(sort, searchBy, keyword))
                .fetch();
        return results.size();
    }
}
