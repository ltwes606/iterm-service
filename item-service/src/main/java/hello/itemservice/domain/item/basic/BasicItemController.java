package hello.itemservice.domain.item.basic;

import hello.itemservice.domain.item.DeliveryCode;
import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.ItemType;
import hello.itemservice.domain.validation.ItemValidator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/basic/items")
@RequiredArgsConstructor
@Slf4j
public class BasicItemController {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    @InitBinder
    public void init(WebDataBinder dataBinder) {
        log.info("init binder {}", dataBinder);
        dataBinder.addValidators(itemValidator);
    }

    @ModelAttribute("regions")
    public Map<String, String> regions() {
        Map<String, String> regions = new LinkedHashMap<>();
        regions.put("SEOUL", "서울");
        regions.put("BUSAN", "부산");
        regions.put("JEJU", "제주");
        return regions;
    }

    @ModelAttribute("itemTypes")
    public ItemType[] itemTypes() {
        return ItemType.values();
    }

    @ModelAttribute("deliveryCodes")
    public List<DeliveryCode> deliveryCodes() {
        List<DeliveryCode> deliveryCodes = new ArrayList<>();
        deliveryCodes.add(new DeliveryCode("FAST", "빠른 배송"));
        deliveryCodes.add(new DeliveryCode("NORMAL", "일반 배송"));
        deliveryCodes.add(new DeliveryCode("SLOW", "느린 배송"));
        return deliveryCodes;
    }


    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "basic/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "basic/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        // th:object를 사용하기 위해서
        model.addAttribute("item", new Item());
        return "basic/addForm";
    }

    /**
     * BindingResult가 타입 오류시 오류를 담아 오류 페이지로 넘어가지 않게 함
     */
//    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Model model) {

        // 검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(
                    new FieldError("item", "itemName", "상품 이름은 필수입니다.")
            );
        }
        if (item.getPrice() == null
                || item.getPrice() < 1_000 || item.getPrice() > 1_000_000) {
            bindingResult.addError(
                    new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다.")
            );
        }
        if (item.getQuantity() == null
                || item.getQuantity() < 0 || item.getQuantity() > 9_999) {
            bindingResult.addError(
                    new FieldError("item", "quantity", "수량은 최소 0 부터 최대 9,999 까지 허용합니다.")
            );
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int totalPrice = item.getPrice() * item.getQuantity();
            if (totalPrice < 10_000) {
                bindingResult.addError(new ObjectError("item",
                        "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + totalPrice)
                );
            }
        }

        // 검증에 실패시 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "/basic/addForm";
        }

        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/basic/items/{itemId}";
    }

    /**
     * 필드 오류시 필드 값이 초기화 되지 않게 함
     */
//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // 검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(
                    new FieldError("item", "itemName", item.getItemName(), false, null, null,
                            "상품 이름은 필수입니다.")
            );
        }
        if (item.getPrice() == null
                || item.getPrice() < 1_000 || item.getPrice() > 1_000_000) {
            bindingResult.addError(
                    new FieldError("item", "price", item.getPrice(), false, null, null,
                            "가격은 1,000 ~ 1,000,000 까지 허용합니다.")
            );
        }
        if (item.getQuantity() == null
                || item.getQuantity() < 0 || item.getQuantity() > 9_999) {
            bindingResult.addError(
                    new FieldError("item", "quantity", item.getQuantity(), false, null, null,
                            "수량은 최소 0 부터 최대 9,999 까지 허용합니다.")
            );
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int totalPrice = item.getPrice() * item.getQuantity();
            if (totalPrice < 10_000) {
                bindingResult.addError(new ObjectError("item", null, null,
                        "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + totalPrice)
                );
            }
        }

        // 검증에 실패시 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "/basic/addForm";
        }

        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/basic/items/{itemId}";
    }

    /**
     * 오류 메시지를 따로 처리
     */
//    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // 검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(
                    new FieldError("item", "itemName", item.getItemName(), false,
                            new String[]{"required.item.itemName"}, null, null)
            );
        }
        if (item.getPrice() == null
                || item.getPrice() < 1_000 || item.getPrice() > 1_000_000) {
            bindingResult.addError(
                    new FieldError("item", "price", item.getPrice(), false,
                            new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null)
            );
        }
        if (item.getQuantity() == null
                || item.getQuantity() < 0 || item.getQuantity() > 9_999) {
            bindingResult.addError(
                    new FieldError("item", "quantity", item.getQuantity(), false,
                            new String[]{"max.item.quantity"}, new Object[]{0, 9999}, null)
            );
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int totalPrice = item.getPrice() * item.getQuantity();
            if (totalPrice < 10_000) {
                bindingResult.addError(new ObjectError("item",
                        new String[]{"totalPriceMin"}, new Object[]{10000, totalPrice}, null)
                );
            }
        }

        // 검증에 실패시 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);

            return "/basic/addForm";
        }

        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/basic/items/{itemId}";
    }

    /**
     * 코드 단순화
     */
//    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        log.info("objectName={}", bindingResult.getObjectName());
        log.info("target={}", bindingResult.getTarget());

        // 검증 로직
//        if (!StringUtils.hasText(item.getItemName())) {
//            bindingResult.rejectValue("itemName", "required");
//        }
        ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");
        if (item.getPrice() == null
                || item.getPrice() < 1_000 || item.getPrice() > 1_000_000) {
            bindingResult.rejectValue("price", "range", new Object[]{1_000, 1_000_000}, null);
        }
        if (item.getQuantity() == null
                || item.getQuantity() < 0 || item.getQuantity() > 9_999) {
            bindingResult.rejectValue("quantity", "max", new Object[]{9_999}, null);
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPrice() != null && item.getQuantity() != null) {
            int totalPrice = item.getPrice() * item.getQuantity();
            if (totalPrice < 10_000) {
                bindingResult.reject("totalPriceMin", new Object[]{10_000, totalPrice}, null);
            }
        }

        // 검증에 실패시 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);

            return "/basic/addForm";
        }

        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/basic/items/{itemId}";
    }

    /**
     * Validator 적용
     */
//    @PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        itemValidator.validate(item, bindingResult);

        // 검증에 실패시 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);

            return "/basic/addForm";
        }

        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/basic/items/{itemId}";
    }

    /**
     * WebDataBinder 사용
     */
    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // 검증에 실패시 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);

            return "/basic/addForm";
        }

        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/basic/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);

        model.addAttribute("item", item);
        return "basic/editForm";
    }

    /**
     * 스프링에서 redirect와 PathVariable의 값을 사용할 수 있게 해준다.
     */
    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        log.info("open={}", item.getOpen());
        log.info("item.regions={}", item.getRegions());
        log.info("item.itemType={}", item.getItemType());
        log.info("item.deliveryCode={}", item.getDeliveryCode());

        itemRepository.update(itemId, item);
        return "redirect:/basic/items/{itemId}";
    }

    /**
     * 테스트용 데이터 추가
     */
    @PostConstruct
    public void init() {
        itemRepository.save(new Item("itemA", 10000, 10));
        itemRepository.save(new Item("itemB", 20000, 20));
    }
}
