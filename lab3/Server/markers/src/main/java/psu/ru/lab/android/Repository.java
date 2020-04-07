package psu.ru.lab.android;

import org.jooq.DSLContext;
import org.jooq.util.maven.example.Sequences;
import org.jooq.util.maven.example.tables.pojos.Marker;
import org.jooq.util.maven.example.tables.pojos.Photo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.jooq.util.maven.example.tables.Photo.PHOTO;
import static org.jooq.util.maven.example.tables.Marker.MARKER;

@org.springframework.stereotype.Repository
public class Repository {

    private final DSLContext dslContext;

    @Autowired
    public Repository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    List<Marker> getMarkers() {
        return dslContext.selectFrom(MARKER).fetchInto(Marker.class);
    }

    Long addMarker(String name, Double latitude, Double longitude) {
        Long nextId = dslContext.nextval(Sequences.MARKER_ID_SEQ);
        dslContext.insertInto(MARKER)
                .columns(MARKER.ID, MARKER.NAME, MARKER.LATITUDE, MARKER.LONGITUDE)
                .values(nextId, name, latitude, longitude)
                .execute();

        return nextId;
    }

    List<Photo> getPhotos(Long markerId) {
        return dslContext.selectFrom(PHOTO)
                .where(PHOTO.MARKER_ID.eq(markerId))
                .fetchInto(Photo.class);
    }

    void addPhoto(Long markerId, String uri) {
        dslContext.insertInto(PHOTO)
                .columns(PHOTO.MARKER_ID, PHOTO.URI)
                .values(markerId, uri)
                .execute();
    }
}