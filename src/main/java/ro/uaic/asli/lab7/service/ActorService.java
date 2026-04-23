package ro.uaic.asli.lab7.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.uaic.asli.lab7.dto.ActorRequest;
import ro.uaic.asli.lab7.dto.ActorResponse;
import ro.uaic.asli.lab7.entity.ActorEntity;
import ro.uaic.asli.lab7.entity.MovieEntity;
import ro.uaic.asli.lab7.exception.BadRequestException;
import ro.uaic.asli.lab7.exception.ResourceNotFoundException;
import ro.uaic.asli.lab7.repository.ActorRepository;
import ro.uaic.asli.lab7.repository.MovieRepository;

import java.util.List;

@Service
public class ActorService {

    private final ActorRepository actorRepository;
    private final MovieRepository movieRepository;

    public ActorService(ActorRepository actorRepository, MovieRepository movieRepository) {
        this.actorRepository = actorRepository;
        this.movieRepository = movieRepository;
    }

    @Transactional(readOnly = true)
    public List<ActorResponse> findAll() {
        return actorRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ActorResponse findById(Long id) {
        return actorRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + id));
    }

    @Transactional
    public ActorResponse create(ActorRequest request) {
        String name = request.getName().strip();
        if (actorRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("An actor with this name already exists.");
        }
        ActorEntity a = new ActorEntity();
        a.setName(name);
        ActorEntity saved = actorRepository.save(a);
        return toResponse(saved);
    }

    @Transactional
    public ActorResponse update(Long id, ActorRequest request) {
        ActorEntity a = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + id));
        String name = request.getName().strip();
        if (actorRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BadRequestException("Another actor already has this name.");
        }
        a.setName(name);
        actorRepository.save(a);
        return toResponse(a);
    }

    @Transactional
    public void delete(Long id) {
        if (!actorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Actor not found with id: " + id);
        }
        List<MovieEntity> movies = movieRepository.findMoviesByActorId(id);
        for (MovieEntity m : movies) {
            m.getActors().removeIf(actor -> actor.getId().equals(id));
        }
        movieRepository.saveAll(movies);
        actorRepository.deleteById(id);
    }

    private ActorResponse toResponse(ActorEntity e) {
        ActorResponse r = new ActorResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        return r;
    }
}
