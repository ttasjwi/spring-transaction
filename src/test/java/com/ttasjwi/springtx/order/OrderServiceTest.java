package com.ttasjwi.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void complete() throws NotEnoughMoneyException {
        //given
        Order order = new Order();
        order.setUsername("정상");
        orderService.order(order);

        // when
        Order findOrder = orderRepository.findById(order.getId()).get();

        // then
        assertThat(findOrder.getPayStatus()).isEqualTo("완료");
    }

    @Test
    public void runtimeException() {
        //given
        Order order = new Order();
        order.setUsername("예외");

        // when & then
        assertThatThrownBy(() -> orderService.order(order))
                .isInstanceOf(RuntimeException.class);

        // then : 롤백되었으므로 데이터가 없어야한다.
        assertThat(orderRepository.findById(order.getId()).isEmpty()).isTrue();
    }

    @Test
    public void bizException() {
        //given
        Order order = new Order();
        order.setUsername("잔고부족");

        // when
        try {
            orderService.order(order);
        } catch (NotEnoughMoneyException e) {
            log.info("고객에게 잔고부족을 알리고 별도의 계좌로 입금하도록 안내");
        }

        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("대기");
    }
}
