package com.demo.filmdb.person;

import com.demo.filmdb.role.RoleRepository;
import com.demo.filmdb.util.EntityNotFoundException;
import com.demo.filmdb.utils.SortUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.demo.filmdb.util.ErrorUtil.personNotFoundMessage;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final PersonMapper personMapper;

    public PersonService(PersonRepository personRepository, RoleRepository roleRepository, PersonMapper personMapper) {
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
        this.personMapper = personMapper;
    }

    /**
     * Returns a {@link Page} of all {@link Person} entities
     *
     * @param pageable must not be null
     * @return the resulting page, may be empty but not null
     */
    public Page<Person> getPeople(Pageable pageable) {
        Pageable filteredPageable = SortUtil.filterSort(pageable, Person.class);
        return personRepository.findAll(filteredPageable);
    }

    /**
     * Returns a {@link Page} of {@link Person} entities matching the given {@link Specification}.
     *
     * @param spec must not be {@code null}.
     * @param pageable must not be {@code null}.
     * @return a page of filtered entities.
     */
    public Page<Person> search(Specification<Person> spec, Pageable pageable) {
        return personRepository.findAll(spec, pageable);
    }

    /**
     * Create a {@link Person}
     *
     * @param personInfo person info
     * @return created entity
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Person createPerson(PersonInfo personInfo) {
        final Person person = personMapper.personInfoToPerson(personInfo);
        return personRepository.save(person);
    }

    /**
     * Returns a {@linkplain Person} entity with the given id or empty {@code Optional} if it doesn't exist
     *
     * @param personId must not be {@code null}
     * @return the found entity or empty {@code Optional}
     */
    public Optional<Person> getPerson(Long personId) {
        return personRepository.findById(personId);
    }

    /**
     * Update a {@link Person}
     *
     * @param personId person to update
     * @param personInfo person info
     * @return the updated entity
     * @throws EntityNotFoundException if person could not be found
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Person updatePerson(Long personId, PersonInfo personInfo) throws EntityNotFoundException {
        Person personToUpdate = personRepository.findById(personId).orElseThrow(() ->
                new EntityNotFoundException(personNotFoundMessage(personId))
        );
        personMapper.updatePersonFromPersonInfo(personInfo, personToUpdate);
        return personRepository.save(personToUpdate);
    }

    /**
     * Deletes the {@link Person} with the given id
     *
     * @param personId person id
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePerson(Long personId) {
        getPerson(personId).ifPresent(person -> {
            person.removeFilmsDirected();
            roleRepository.deleteById_PersonId(personId);
            personRepository.deleteById(personId);
        });
    }

    /**
     * Returns whether a {@link Person} with the given id exists
     *
     * @param personId must not be {@code null}
     * @return true if exists, false otherwise
     */
    public boolean personExists(Long personId) {
        return personRepository.existsById(personId);
    }
}
