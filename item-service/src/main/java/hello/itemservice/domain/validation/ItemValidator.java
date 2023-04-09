package hello.itemservice.domain.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        Item item = (Item) target;

        // 검증 로직
//        if (!StringUtils.hasText(item.getItemName())) {
//            bindingResult.rejectValue("itemName", "required");
//        }
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "itemName", "required");
        if (item.getPrice() == null
                || item.getPrice() < 1_000 || item.getPrice() > 1_000_000) {
            errors.rejectValue("price", "range", new Object[]{1_000, 1_000_000}, null);
        }
        if (item.getQuantity() == null
                || item.getQuantity() < 0 || item.getQuantity() > 9_999) {
            errors.rejectValue("quantity", "max", new Object[]{9_999}, null);
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int totalPrice = item.getPrice() * item.getQuantity();
            if (totalPrice < 10_000) {
                errors.reject("totalPriceMin", new Object[]{10_000, totalPrice}, null);
            }
        }
    }
}
