package subway.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LineRequest {
    private final String name;
    private final String color;
}
