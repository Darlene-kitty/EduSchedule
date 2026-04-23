package cm.iusjc.teacheravailability.service;

import cm.iusjc.teacheravailability.dto.TeacherAvailabilityDTO;
import cm.iusjc.teacheravailability.entity.TeacherAvailability;
import org.springframework.stereotype.Component;

@Component
public class TeacherAvailabilityMapper {
    
    public TeacherAvailabilityDTO toDTO(TeacherAvailability entity) {
        if (entity == null) {
            return null;
        }
        
        TeacherAvailabilityDTO dto = new TeacherAvailabilityDTO();
        dto.setId(entity.getId());
        dto.setTeacherId(entity.getTeacherId());
        dto.setSchoolId(entity.getSchoolId());
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setAvailabilityType(entity.getAvailabilityType());
        dto.setSpecificDate(entity.getSpecificDate());
        dto.setIsRecurring(entity.getIsRecurring());
        dto.setPriorityLevel(entity.getPriorityLevel());
        dto.setNotes(entity.getNotes());
        dto.setIsActive(entity.getIsActive());
        
        return dto;
    }
    
    public TeacherAvailability toEntity(TeacherAvailabilityDTO dto) {
        if (dto == null) {
            return null;
        }
        
        TeacherAvailability entity = new TeacherAvailability();
        entity.setId(dto.getId());
        entity.setTeacherId(dto.getTeacherId());
        entity.setSchoolId(dto.getSchoolId());
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setAvailabilityType(dto.getAvailabilityType());
        entity.setSpecificDate(dto.getSpecificDate());
        entity.setIsRecurring(dto.getIsRecurring());
        entity.setPriorityLevel(dto.getPriorityLevel());
        entity.setNotes(dto.getNotes());
        entity.setIsActive(dto.getIsActive());
        
        return entity;
    }
    
    public void updateEntityFromDTO(TeacherAvailability entity, TeacherAvailabilityDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setAvailabilityType(dto.getAvailabilityType());
        entity.setSpecificDate(dto.getSpecificDate());
        entity.setIsRecurring(dto.getIsRecurring());
        entity.setPriorityLevel(dto.getPriorityLevel());
        entity.setNotes(dto.getNotes());
        entity.setIsActive(dto.getIsActive());
    }
}