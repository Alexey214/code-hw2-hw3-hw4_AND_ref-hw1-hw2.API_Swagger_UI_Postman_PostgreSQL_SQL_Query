package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.component.RecordMapper;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.exception.StudentIllegalArgumentException;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.recorder.FacultyRecord;
import ru.hogwarts.school.recorder.StudentRecord;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final RecordMapper recordMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentService.class);

    public StudentService(StudentRepository studentRepository,
                          FacultyRepository facultyRepository,
                          RecordMapper recordMapper) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.recordMapper = recordMapper;
    }

    public StudentRecord createStudent(StudentRecord studentRecord) {
        LOGGER.info("Добавляем студента: " + studentRecord);
        Student student = recordMapper.toEntity(studentRecord);
        if (studentRecord.getFaculty() != null) {
            Faculty faculty = facultyRepository.findById(studentRecord.getFaculty().getId()).orElseThrow(FacultyNotFoundException::new);
            student.setFaculty(faculty);
        }
        LOGGER.debug("Добавлен студент: " + studentRecord);
        return recordMapper.toRecord(studentRepository.save(student));
    }

    public StudentRecord readStudent(Long id) {
        LOGGER.info("Получаем студента с id {}:", id);
        return studentRepository.findById(id)
                .map(recordMapper::toRecord)
                .orElseThrow(StudentNotFoundException::new);
    }

    public StudentRecord updateStudent(StudentRecord studentRecord) {
        LOGGER.info("Изменяем студента: " + studentRecord);
        Student studentTmp = studentRepository.findById(studentRecord.getId())
                .orElseThrow(StudentNotFoundException::new);
        studentTmp.setAge(studentRecord.getAge());
        studentTmp.setName(studentRecord.getName());
        return recordMapper.toRecord(studentRepository.save(studentTmp));
    }

    public StudentRecord deleteStudent(Long id) {
        LOGGER.warn("Удаляем студента с id {}", id);
        Student studentTmp = studentRepository.findById(id)
                .orElseThrow(StudentNotFoundException::new);
        studentRepository.delete(studentTmp);
        LOGGER.debug("Удалён студент с id {}", id);
        return recordMapper.toRecord(studentTmp);
    }


    public List<StudentRecord> readAllStudentWhitAge(int age) {
        LOGGER.info("Запрашиваем студентов с возрастом {} лет", age);
        return studentRepository.findAllByAge(age).stream()
                .map(recordMapper::toRecord)
                .collect(Collectors.toList());
    }

    public List<StudentRecord> findByAgeBetween(int minAge, int maxAge) {
        LOGGER.info("Запрашиваем студентов с возрастом от {} до {} лет", minAge, maxAge);
        if (minAge > maxAge) {
            LOGGER.error("Ошибка при запросе студентов с возрастом от {} до {} лет", minAge, maxAge);
            throw new StudentIllegalArgumentException();
        }
        return studentRepository.findByAgeBetween(minAge, maxAge).stream()
                .map(recordMapper::toRecord)
                .collect(Collectors.toList());
    }

    public FacultyRecord findByFacultyOfStudent(long id) {
        LOGGER.info("Запрашиваем факультет студента с id {}", id);
        Student studentTmp = studentRepository.findById(id)
                .orElseThrow(StudentNotFoundException::new);
        if (studentTmp.getFaculty() == null) {
            LOGGER.error("Ошибка при запросе факультета студента {}", id);
            throw new FacultyNotFoundException();
        }
        return recordMapper.toRecord(studentTmp.getFaculty());
    }

    public Student getStudent(Long id) {
        return studentRepository.findById(id).
                orElseThrow(StudentNotFoundException::new);
    }

    public Integer findCountOfAllStudents() {
        LOGGER.info("Запрашиваем количество студентов в школе");
        return studentRepository.findCountOfAllStudents();
    }

    public Double findAverageStudentAge() {
        LOGGER.info("Запрашиваем при помощи БД средний возраст студентов в школе");
        return studentRepository.findAverageStudentAge();
    }

    public List<StudentRecord> findLastsStudents(int lastStudents) {
        LOGGER.info("Запрашиваем список последних {} добавленных студентов", lastStudents);
        return studentRepository.findLastsStudents(lastStudents).stream()
                .map(recordMapper::toRecord)
                .collect(Collectors.toList());
    }

    public List<StudentRecord> findAndSortByFirstChar(String character) {
        LOGGER.info("Запрашиваем список студентов, чьё имя начинается с {}: ", character);
        return studentRepository.findAll().stream()
                .sorted((s1, s2) -> (s1.getName().compareTo(s2.getName())))
                .filter(student -> student.getName().startsWith(character))
                .map(recordMapper::toRecord)
                .collect(Collectors.toList());
    }

    public Double findAverageAgeOfStudents() {
        LOGGER.info("Запрашиваем при помощи stream() средний возраст студентов в школе");
        return studentRepository.findAll().stream()
                .map(recordMapper::toRecord)
                .mapToDouble(StudentRecord::getAge).average().getAsDouble();
    }
}
