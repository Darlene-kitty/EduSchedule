package cm.iusjc.course.service;

import cm.iusjc.course.dto.CourseGroupDTO;
import cm.iusjc.course.dto.CourseGroupRequest;
import cm.iusjc.course.entity.CourseGroup;
import cm.iusjc.course.repository.CourseGroupRepository;
import cm.iusjc.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseGroupService {
    
    private final CourseGroupRepository courseGroupRepository;
    private final CourseRepository courseRepository;
    private final UserServiceClient userServiceClient;
    
    @Transactional
    public CourseGroupDTO createGroup(CourseGroupRequest request) {
        log.info("Creating new course group: {} for course {}", request.getGroupName(), request.getCourseId());
        
        // Vérifier que le cours existe
        if (!courseRepository.existsById(request.getCourseId())) {
            throw new RuntimeException("Course not found with id: " + request.getCourseId());
        }
        
        // Vérifier si le nom de groupe existe déjà pour ce cours
        if (courseGroupRepository.existsByCourseIdAndGroupNameAndActiveTrue(
                request.getCourseId(), request.getGroupName())) {
            throw new RuntimeException("Group name already exists for this course: " + request.getGroupName());
        }
        
        CourseGroup group = new CourseGroup();
        group.setCourseId(request.getCourseId());
        group.setGroupName(request.getGroupName());
        group.setType(request.getType());
        group.setMaxStudents(request.getMaxStudents());
        group.setCurrentStudents(0);
        group.setTeacherId(request.getTeacherId());
        group.setActive(true);
        
        CourseGroup savedGroup = courseGroupRepository.save(group);
        log.info("Course group created successfully: {}", savedGroup.getGroupName());
        
        return mapToDTO(savedGroup);
    }
    
    public CourseGroupDTO getGroupById(Long id) {
        CourseGroup group = courseGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course group not found with id: " + id));
        return mapToDTO(group);
    }
    
    public List<CourseGroupDTO> getGroupsByCourse(Long courseId) {
        return courseGroupRepository.findByCourseIdAndActiveTrue(courseId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<CourseGroupDTO> getGroupsByTeacher(Long teacherId) {
        return courseGroupRepository.findByTeacherIdAndActiveTrue(teacherId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<CourseGroupDTO> getGroupsByType(String type) {
        return courseGroupRepository.findByTypeAndActiveTrue(type)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<CourseGroupDTO> getAvailableGroups() {
        return courseGroupRepository.findGroupsWithAvailableSpots()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<CourseGroupDTO> getAvailableGroupsByCourse(Long courseId) {
        return courseGroupRepository.findAvailableGroupsByCourse(courseId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CourseGroupDTO updateGroup(Long id, CourseGroupRequest request) {
        log.info("Updating course group: {}", id);
        
        CourseGroup group = courseGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course group not found with id: " + id));
        
        // Vérifier si le nouveau nom existe déjà (sauf pour ce groupe)
        if (!group.getGroupName().equals(request.getGroupName()) && 
            courseGroupRepository.existsByCourseIdAndGroupNameAndActiveTrue(
                    request.getCourseId(), request.getGroupName())) {
            throw new RuntimeException("Group name already exists for this course: " + request.getGroupName());
        }
        
        group.setGroupName(request.getGroupName());
        group.setType(request.getType());
        group.setMaxStudents(request.getMaxStudents());
        group.setTeacherId(request.getTeacherId());
        
        CourseGroup updatedGroup = courseGroupRepository.save(group);
        log.info("Course group updated successfully: {}", updatedGroup.getGroupName());
        
        return mapToDTO(updatedGroup);
    }
    
    @Transactional
    public void deleteGroup(Long id) {
        log.info("Deleting course group: {}", id);
        
        CourseGroup group = courseGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course group not found with id: " + id));
        
        // Soft delete
        group.setActive(false);
        courseGroupRepository.save(group);
        
        log.info("Course group deleted successfully: {}", id);
    }
    
    @Transactional
    public CourseGroupDTO addStudentToGroup(Long groupId) {
        CourseGroup group = courseGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Course group not found with id: " + groupId));
        
        if (group.getCurrentStudents() >= group.getMaxStudents()) {
            throw new RuntimeException("Group is full: " + group.getGroupName());
        }
        
        group.setCurrentStudents(group.getCurrentStudents() + 1);
        CourseGroup updatedGroup = courseGroupRepository.save(group);
        
        log.info("Student added to group: {} (current: {}/{})", 
                group.getGroupName(), group.getCurrentStudents(), group.getMaxStudents());
        
        return mapToDTO(updatedGroup);
    }
    
    @Transactional
    public CourseGroupDTO removeStudentFromGroup(Long groupId) {
        CourseGroup group = courseGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Course group not found with id: " + groupId));
        
        if (group.getCurrentStudents() <= 0) {
            throw new RuntimeException("No students in group: " + group.getGroupName());
        }
        
        group.setCurrentStudents(group.getCurrentStudents() - 1);
        CourseGroup updatedGroup = courseGroupRepository.save(group);
        
        log.info("Student removed from group: {} (current: {}/{})", 
                group.getGroupName(), group.getCurrentStudents(), group.getMaxStudents());
        
        return mapToDTO(updatedGroup);
    }
    
    private CourseGroupDTO mapToDTO(CourseGroup group) {
        CourseGroupDTO dto = new CourseGroupDTO();
        dto.setId(group.getId());
        dto.setCourseId(group.getCourseId());
        dto.setGroupName(group.getGroupName());
        dto.setType(group.getType());
        dto.setMaxStudents(group.getMaxStudents());
        dto.setCurrentStudents(group.getCurrentStudents());
        dto.setTeacherId(group.getTeacherId());
        dto.setActive(group.getActive());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());
        
        // Récupérer le nom de l'enseignant
        if (group.getTeacherId() != null) {
            try {
                dto.setTeacherName(userServiceClient.getUserName(group.getTeacherId()));
            } catch (Exception e) {
                log.warn("Could not fetch teacher name for ID: {}", group.getTeacherId());
                dto.setTeacherName("Enseignant inconnu");
            }
        }
        
        return dto;
    }
}