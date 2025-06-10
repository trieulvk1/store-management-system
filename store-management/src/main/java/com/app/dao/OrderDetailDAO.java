package com.app.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.entity.OrderDetail;

@Repository
public interface OrderDetailDAO extends JpaRepository<OrderDetail, Long> {
//	List<OrderDetail> findAllByOrder(Order order);

  @Query("SELECT SUM(od.quantity) FROM OrderDetail od WHERE od.order.createDate = ?1")
  Integer totalProductSoldCurrentDay(Date date);

  @Query("SELECT p.name, SUM(od.quantity) AS totalQuantity " +
  "FROM OrderDetail od " +
  "JOIN od.product p " +
  "WHERE od.order.createDate BETWEEN ?1 AND ?2 " +
  "GROUP BY od.product.id, p.name " +
  "ORDER BY totalQuantity DESC")
  List<Object[]> getTop5SellingProductsForWeek(Date startOfWeek, Date endOfWeek);

  @Query("SELECT od FROM OrderDetail od WHERE od.order.id = ?1")
  List<OrderDetail> findByOrderId(Long orderId);
}
