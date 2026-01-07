package leafCatcher.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;


@Node("Event")
@AllArgsConstructor
@Getter @Setter
@NoArgsConstructor
public class Event {
    @Id
    private String elementId = UUID.randomUUID().toString();

    private Long telegramId;
    private Integer updateId;
    private String description;
    private String shortName;

    private Boolean isRoot = false;
    private Boolean isEnd = false;
    private Boolean isChangeable = true;

    private Boolean isDummy = true;
    private String originalId;

    private Integer endNumber;
    private String author;


}

