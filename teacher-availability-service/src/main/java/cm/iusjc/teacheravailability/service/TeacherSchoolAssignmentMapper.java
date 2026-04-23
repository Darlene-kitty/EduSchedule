package cm.iusjc.teacheravailability.service;

import cm.iusjc.teacheravailability.dto.TeacherSchoolAssignmentDTO;
import cm.iusjc.teacheravailability.entity.TeacherSchoolAssignment;
import org.springframework.stereotype.Component;

@Component
public class TeacherSchoolAssignmentMapper {
    
    public TeacherSchoolAssignmentDTO toDTO(TeacherSchoolAssignment entity) {
        if (entity == null) {
            return null;
        }
        
        TeacherSchoolAssignmentDTO dto = new TeacherSchoolAssignmentDTO();
        dto.setId(entity.getId());
        dto.setTeacherId(entity.getTeacherId());
        dto.setSchoolId(entity.getSchoolId());
        dto.setWorkingDays(entity.getWorkingDays());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setTravelTimeMinutes(entity.getTravelTimeMinutes());
        dto.setIsPrimarySchool(entity.getIsPrimarySchool());
        dto.setContractType(entity.getContractType());
        dto.setMaxHoursPerWeek(entity.getMaxHoursPerWeek());
        dto.setIsActive(entity.getIsActive());
        
        return dto;
    }
    
    public TeacherSchoolAssignment toEntity(TeacherSchoolAssignmentDTO dto) {
        if (dto == null) {
            return null;
        }
        
        TeacherSchoolAssignment entity = new TeacherSchoolAssignment();
        entity.setId(dto.getId());
        entity.setTeacherId(dto.getTeacherId());
        entity.setSchoolId(dto.getSchoolId());
        entity.setWorkingDays(dto.getWorkingDays());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setTravelTimeMinutes(dto.getTravelTimeMinutes());
        entity.setIsPrimarySchool(dto.getIsPrimarySchool());
        entity.setContractType(dto.getContractType());
        entity.setMaxHoursPerWeek(dto.getMaxHoursPerWeek());
        entity.setIsActive(dto.getIsActive());
        
        return entity;
    }
    
    public void updateEntityFromDTO(TeacherSchoolAssignment entity, TeacherSchoolAssignmentDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        
        entity.setWorkingDays(dto.getWorkingDays());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setTravelTimeMinutes(dto.getTravelTimeMinutes());
        entity.setIsPrimarySchool(dto.getIsPrimarySchool());
        entity.setContractType(dto.getContractType());
        entity.setMaxHoursPerWeek(dto.getMaxHoursPerWeek());
        entity.setIsActive(dto.getIsActive());
    }
}