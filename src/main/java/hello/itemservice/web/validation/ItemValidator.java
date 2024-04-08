package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
        // 위 구문을 이렇게도 볼 수 있다. item == clazz
        // 위 구문을 이렇게도 볼 수 있다. item == subItem (Item의 자식클래스여도 통과)
    }

    @Override
    public void validate(Object target, Errors errors) { // errors는 bindingResult의 부모클래스이다. 따라서 bindingResult를 넣어줄 수 있다. (다형성) => 부모는 자식을 담을 수 있다. 자식은 부모를 담지 못한다.
        Item item = (Item) target; // 인터페이스 자체에서 target 타입을 Object로 명시했기에, 캐스팅을 해서 사용한다.

        // 특정 필드 검증
        if (!StringUtils.hasText(item.getItemName())) {
            errors.rejectValue("itemName", "required");
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            errors.rejectValue("price", "range",
                    new Object[]{1000, 1000000}, null);
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            errors.rejectValue("quantity", "max",
                    new Object[]{9999}, null);
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                errors.reject(
                        "totalPriceMin",
                        new Object[]{10000, resultPrice},
                        null);
            }
        }
    }
}
