package leafCatcher.repository;

import leafCatcher.model.Event;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends Neo4jRepository<Event, String> {

    Optional<Event> findById(String id);

    Boolean existsByIsRoot(Boolean isRoot);

    void deleteAll();

    @Query("""
            MATCH (e:Event)
            WHERE e.isRoot = true
            RETURN e
            """)
    Event getRoot();

    @Query("""
                MATCH (p:Event {elementId: $parentElementId})-[:NEXT]->(child:Event)
                RETURN child
            """)
    List<Event> getChildren(@Param("parentElementId") String parentElementId);


    @Query("""
            MATCH (n:Event)-[:NEXT]->(:Event)
            WHERE n.isEnd = FALSE
            RETURN n
            ORDER BY rand()
            LIMIT 1
            """)
    Event getRandomNotEndEvent();

    @Query("""
            MATCH (p:Event), (c:Event)
            WHERE p.elementId = $parentId AND c.elementId = $childId
            MERGE (p)-[:NEXT]->(c)
            MERGE (c)-[:PREVIOUS]->(p)
            """)
    void linkParentChild(@Param("parentId") String parentId,
                         @Param("childId") String childId);

    @Query("""
            MATCH (p:Event), (c:Event)
            WHERE p.elementId = $parentId AND c.elementId = $childId
            MERGE (p)-[:NEXT]->(c)
            """)
    void linkParentChildNoBack(@Param("parentId") String parentId,
                         @Param("childId") String childId);


    @Query("""
            MATCH (child:Event {elementId: $childElementId})-[:PREVIOUS]->(parent:Event)
            RETURN parent
            """)
    Event getParent(@Param("childElementId") String childElementId);

}



