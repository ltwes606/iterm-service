package hello.itemservice.login.domain.member.Member;

import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class member {
    private Long id;

    @NotEmpty
    private String loginId;
    @NotEmpty
    private String name;
    @NotEmpty
    private String password;
}
