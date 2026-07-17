package fedor.dev.order.domain;

import fedor.dev.api.http.order.OrderStatus;
import fedor.dev.api.http.order.CreateOrderRequestDto;
import fedor.dev.api.http.order.OrderDto;
import fedor.dev.api.http.order.OrderItemEntityDto;
import fedor.dev.api.http.order.OrderItemRequestDto;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;

import fedor.dev.order.domain.db.OrderEntity;
import fedor.dev.order.domain.db.OrderEntityMapper;
import fedor.dev.order.domain.db.OrderItemEntity;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-12T00:01:50+0700",
    comments = "version: 1.6.0, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.jar, environment: Java 21.0.11 (Homebrew)"
)
@Component
public class OrderEntityMapperImpl implements OrderEntityMapper {

    @Override
    public OrderEntity toEntity(CreateOrderRequestDto requestDto) {
        if ( requestDto == null ) {
            return null;
        }

        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setCustomerId( requestDto.customerId() );
        orderEntity.setAddress( requestDto.address() );
        orderEntity.setItems( orderItemRequestDtoSetToOrderItemEntitySet( requestDto.items() ) );

        linkOrderItemEntities( orderEntity );

        return orderEntity;
    }

    @Override
    public OrderDto toOrderDto(OrderEntity orderEntity) {
        if ( orderEntity == null ) {
            return null;
        }

        Long id = null;
        Long customerId = null;
        String address = null;
        BigDecimal totalAmount = null;
        String courierName = null;
        Integer etaMinutes = null;
        OrderStatus orderStatus = null;
        Set<OrderItemEntityDto> items = null;

        id = orderEntity.getId();
        customerId = orderEntity.getCustomerId();
        address = orderEntity.getAddress();
        totalAmount = orderEntity.getTotalAmount();
        courierName = orderEntity.getCourierName();
        etaMinutes = orderEntity.getEtaMinutes();
        orderStatus = orderEntity.getOrderStatus();
        items = orderItemEntitySetToOrderItemEntityDtoSet( orderEntity.getItems() );

        OrderDto orderDto = new OrderDto( id, customerId, address, totalAmount, courierName, etaMinutes, orderStatus, items );

        return orderDto;
    }

    protected OrderItemEntity orderItemRequestDtoToOrderItemEntity(OrderItemRequestDto orderItemRequestDto) {
        if ( orderItemRequestDto == null ) {
            return null;
        }

        OrderItemEntity orderItemEntity = new OrderItemEntity();

        orderItemEntity.setItemId( orderItemRequestDto.itemId() );
        orderItemEntity.setName( orderItemRequestDto.name() );
        orderItemEntity.setQuantity( orderItemRequestDto.quantity() );

        return orderItemEntity;
    }

    protected Set<OrderItemEntity> orderItemRequestDtoSetToOrderItemEntitySet(Set<OrderItemRequestDto> set) {
        if ( set == null ) {
            return null;
        }

        Set<OrderItemEntity> set1 = new LinkedHashSet<OrderItemEntity>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( OrderItemRequestDto orderItemRequestDto : set ) {
            set1.add( orderItemRequestDtoToOrderItemEntity( orderItemRequestDto ) );
        }

        return set1;
    }

    protected OrderItemEntityDto orderItemEntityToOrderItemEntityDto(OrderItemEntity orderItemEntity) {
        if ( orderItemEntity == null ) {
            return null;
        }

        Long id = null;
        Long itemId = null;
        Integer quantity = null;
        BigDecimal priceAtPurchase = null;

        id = orderItemEntity.getId();
        itemId = orderItemEntity.getItemId();
        quantity = orderItemEntity.getQuantity();
        priceAtPurchase = orderItemEntity.getPriceAtPurchase();

        OrderItemEntityDto orderItemEntityDto = new OrderItemEntityDto( id, itemId, quantity, priceAtPurchase );

        return orderItemEntityDto;
    }

    protected Set<OrderItemEntityDto> orderItemEntitySetToOrderItemEntityDtoSet(Set<OrderItemEntity> set) {
        if ( set == null ) {
            return null;
        }

        Set<OrderItemEntityDto> set1 = new LinkedHashSet<OrderItemEntityDto>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( OrderItemEntity orderItemEntity : set ) {
            set1.add( orderItemEntityToOrderItemEntityDto( orderItemEntity ) );
        }

        return set1;
    }
}
