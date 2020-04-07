package psu.ru.lab.android;

import org.jooq.util.maven.example.tables.pojos.Marker;
import org.jooq.util.maven.example.tables.pojos.Photo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@org.springframework.stereotype.Service
public class Service {

    private final Repository repository;

    @Autowired
    public Service(Repository repository) {
        this.repository = repository;
    }

    public List<Marker> getMarkers() {
        return repository.getMarkers();
    }

    public Long addMarker(String name, Double latitude, Double longitude) {
        return repository.addMarker(name, latitude, longitude);
    }

    public List<Photo> getPhotos(Long markerId) {
        return repository.getPhotos(markerId);
    }

    public void addPhoto(Long markerId, String uri) {
        repository.addPhoto(markerId, uri);
    }
}
