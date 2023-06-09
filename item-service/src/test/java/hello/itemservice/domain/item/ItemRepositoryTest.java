package hello.itemservice.domain.item;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ItemRepositoryTest {

    ItemRepository itemRepository = new ItemRepository();

    @BeforeEach
    void beforeEach() {
        itemRepository.clearStore();
    }

    @AfterEach
    void afterEach() {
        itemRepository.clearStore();
    }

    // 저장
    @Test
    void save() {
        // given
        Item item = new Item("item", 100000, 10);

        // when
        Item savedItem = itemRepository.save(item);

        // then
        Item findItem = itemRepository.findById(item.getId());
        assertThat(findItem).isEqualTo(savedItem);
    }

    // 전체 조회
    @Test
    void findAll() {
        // given
        Item item1 = new Item("item1", 100000, 10);
        Item item2 = new Item("item2", 200000, 20);

        itemRepository.save(item1);
        itemRepository.save(item2);

        // when
        List<Item> items = itemRepository.findAll();

        // then
        assertThat(items).contains(item1, item2);
        assertThat(items).hasSize(2);
    }

    // 업데이트
    @Test
    void update() {
        // given
        Item item = new Item("item", 100000, 10);

        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        // when
        Item updateParam = new Item("updateItem", 5000, 5);
        itemRepository.update(itemId, updateParam);

        Item findItem = itemRepository.findById(itemId);

        // then
        assertThat(findItem.getId()).isEqualTo(itemId);
        assertThat(findItem.getItemName()).isEqualTo(updateParam.getItemName());
        assertThat(findItem.getPrice()).isEqualTo(updateParam.getPrice());
        assertThat(findItem.getQuantity()).isEqualTo(updateParam.getQuantity());
    }
}
