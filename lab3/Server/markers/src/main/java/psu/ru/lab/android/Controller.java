package psu.ru.lab.android;

import org.jooq.util.maven.example.tables.pojos.Marker;
import org.jooq.util.maven.example.tables.pojos.Photo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class Controller {

    private final Service service;

    @Autowired
    public Controller(Service service) {
        this.service = service;
    }

    @GetMapping("markers")
    public List<Marker> getMarkers() {
        return service.getMarkers();
    }

    @PostMapping("marker/add")
    public Long addMarker(@RequestParam String name,
                          @RequestParam Double latitude,
                          @RequestParam Double longitude) {
        return service.addMarker(name, latitude, longitude);
    }

    @GetMapping("/photos")
    public List<Photo> getPhotos(@RequestParam Long markerId) {
        return service.getPhotos(markerId);
    }

    @PostMapping("photo/add")
    public void addPhoto(@RequestParam Long markerId,
                          @RequestParam String uri) {
        service.addPhoto(markerId, uri);
    }

}
