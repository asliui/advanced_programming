package ro.uaic.asli.lab7.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ro.uaic.asli.lab7.dto.ActorRequest;
import ro.uaic.asli.lab7.dto.ActorResponse;
import ro.uaic.asli.lab7.service.ActorService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/actors")
@Profile({"lab7-homework", "lab7-advanced"})
@Tag(name = "Actors", description = "CRUD for actors")
public class ActorController {

    private final ActorService actorService;

    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    @GetMapping
    @Operation(summary = "List all actors")
    @ApiResponse(responseCode = "200", description = "OK")
    public List<ActorResponse> listActors() {
        return actorService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get actor by id")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Not found")
    public ActorResponse getActor(@PathVariable Long id) {
        return actorService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create an actor")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "400", description = "Bad request")
    public ResponseEntity<ActorResponse> createActor(@Valid @RequestBody ActorRequest request) {
        ActorResponse created = actorService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update actor name")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "Not found")
    public ActorResponse updateActor(@PathVariable Long id, @Valid @RequestBody ActorRequest request) {
        return actorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an actor (removes links from movies)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "No content")
    @ApiResponse(responseCode = "404", description = "Not found")
    public ResponseEntity<Void> deleteActor(@PathVariable Long id) {
        actorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
