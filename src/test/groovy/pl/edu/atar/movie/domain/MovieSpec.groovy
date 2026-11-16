package pl.edu.atar.movie.domain

import pl.edu.atar.movie.dto.MovieDto
import pl.edu.atar.movie.dto.MovieNotFoundException
import spock.lang.Specification


class MovieSpec extends Specification implements SampleMovies {

    // Spock Framework Reference Documentation:
    // https://spockframework.org/spock/docs/2.5/all_in_one.html

    MovieFacade facade

    def setup() {
        facade = new MovieConfiguration().movieFacade()
    }

    def "should show a movie"() {
        given: "movie is in the system"
        facade.add(inception)

        expect: "system return the movie"
        facade.show(inception.movieId) == inception
    }

    def "should add a movie"() {
        given: "movie inventory is empty"
        facade.deleteAll()

        when: "I add one movie"
        facade.add(inception)

        then: "Movie is saved in the inventory"
        facade.show(inception.movieId) == inception
    }

    def "should delete a movie"() {
        given: "one movie is in the inventory"
        facade.deleteAll()
        facade.add(inception)

        when: "I try to delete this movie"
        boolean isDeleted = facade.delete(inception.movieId)

        then: "movie is deleted successfully"
        isDeleted.booleanValue()
    }

    def "should update a movie"() {
        given: "one movie is in the inventory"
        facade.deleteAll()
        facade.add(inception)

        when: "I try to update this movie"
        facade.update(inception.movieId, matrix)
        MovieDto updated = facade.show(inception.movieId)

        then: "movie title is updated, but ID is the same"
        updated.title == matrix.title
    }

    def "should list films"() {
        given: "two movies is in inventory"
        facade.deleteAll()
        facade.add(inception)
        facade.add(matrix)

        expect: "system returns movies as list"
        facade.findAll() instanceof List
    }

    def "should throw exception when asked for a movie that's not in the system"() {
        given: "one movie is in the inventory"
        facade.deleteAll()
        facade.add(matrix)

        when: "I try to get a movie that is not in the system"
        facade.show(inception.movieId)

        then: "MovieNotFoundException is thrown"
        thrown(MovieNotFoundException)
    }

}
