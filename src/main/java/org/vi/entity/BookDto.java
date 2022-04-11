package org.vi.entity;

import lombok.*;

/**
 * @author Eric Tseng
 * @description Book
 * @since 2022/4/3 22:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BookDto {
    private Long id;
    private String tag;
    private String author;
    private String name;
    private String desc;
}
