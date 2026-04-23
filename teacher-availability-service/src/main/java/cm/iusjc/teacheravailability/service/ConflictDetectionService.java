package cm.iusjc.teacheravailability.service;

import cm.iusjc.teacheravailability.dto.ConflictDetectionDTO;
import cm.iusjc.teacheravailability.entity.TeacherAvailability;
import cm.iusjc.teacheravailability.entity.TeacherSchoolAssignment;
import cm.iusjc.teacheravailability.repository.TeacherAvailabilityRepository;
import cm.iusjc.teacheravailability.repository.TeacherSchoolAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
public class ConflictDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConflictDetectionService.class);
    
    @Autowired
    private TeacherAvailabilityRepository availabilityRepository;
    
    @Autowired
    private TeacherSchoolAssignmentRepository assignmentRepository;
    
    @Autowired
    private MultiSchoolService multiSchoolService;
    
    // Main conflict detection method
    public ConflictDetectionDTO checkConflicts(Long teacherId, DayOfWeek dayOfWeek, 
                                              LocalTime startTime, LocalTime endTime, Long schoolId) {
        logger.info("Checking conflicts for teacher {} on {} from {} to {}", 
                   teacherId, dayOfWeek, startTime, endTime);
        
        ConflictDetectionDTO result = new ConflictDetectionDTO(teacherId, dayOfWeek, startTime, endTime);
        result.setSchoolId(schoolId);
        
        // Check time overlap conflicts
        checkTimeOverlapConflicts(result);
        
        // Check inter-school conflicts (travel time)
        checkInterSchoolConflicts(result);
        
        // Check working day conflicts
        checkWorkingDayConflicts(result);
        
        // Set final message
        if (result.isHasConflicts()) {
            result.setMessage(String.format("Found %d conflict(s) for the requested time slot", 
                                           result.getConflictCount()));
        } else {
            result.setMessage("No conflicts detected");
        }
        
        logger.info("Conflict check completed for teacher {}: {} conflicts found", 
                   teacherId, result.getConflictCount());
        
        return result;
    }
    
    // Check for time overlap conflicts
    private void checkTimeOverlapConflicts(ConflictDetectionDTO result) {
        List<TeacherAvailability> conflictingAvailabilities = availabilityRepository
            .findConflictingAvailabilities(
                result.getTeacherId(),
                result.getDayOfWeek(),
                result.getStartTime(),
                result.getEndTime()
            );
        
        for (TeacherAvailability conflicting : conflictingAvailabilities) {
            ConflictDetectionDTO.ConflictDTO conflict = new ConflictDetectionDTO.ConflictDTO();
            conflict.setConflictingAvailabilityId(conflicting.getId());
            conflict.setConflictStartTime(conflicting.getStartTime());
            conflict.setConflictEndTime(conflicting.getEndTime());
            conflict.setConflictType("TIME_OVERLAP");
            conflict.setDescription(String.format("Time overlap with existing %s slot from %s to %s",
                                                 conflicting.getAvailabilityType().getDisplayName(),
                                                 conflicting.getStartTime(),
                                                 conflicting.getEndTime()));
            conflict.setConflictingSchoolId(conflicting.getSchoolId());
            
            result.addConflict(conflict);
        }
    }
    
    // Check for inter-school conflicts (travel time)
    private void checkInterSchoolConflicts(ConflictDetectionDTO result) {
        if (result.getSchoolId() == null) {
            return; // No school specified, skip inter-school checks
        }
        
        // Get teacher's school assignments
        List<TeacherSchoolAssignment> assignments = assignmentRepository
            .findByTeacherIdAndIsActiveTrue(result.getTeacherId());
        
        // Check if teacher works at multiple schools
        if (assignments.size() <= 1) {
            return; // Teacher only works at one school, no inter-school conflicts
        }
        
        // Check for conflicts with other schools on the same day
        for (TeacherSchoolAssignment assignment : assignments) {
            if (!assignment.getSchoolId().equals(result.getSchoolId()) && 
                assignment.isWorkingDay(result.getDayOfWeek())) {
                
                // Calculate travel time needed
                Integer travelTime = multiSchoolService.calculateTravelTime(
                    assignment.getSchoolId(), result.getSchoolId());
                
                if (travelTime > 0) {
                    // Check if there's enough time between slots
                    LocalTime earliestArrival = result.getStartTime().minusMinutes(travelTime);
                    LocalTime latestDeparture = result.getEndTime().plusMinutes(travelTime);
                    
                    // Find conflicting availabilities at the other school
                    List<TeacherAvailability> otherSchoolAvailabilities = availabilityRepository
                        .findByTeacherIdAndSchoolIdAndIsActiveTrue(result.getTeacherId(), assignment.getSchoolId());
                    
                    for (TeacherAvailability otherAvailability : otherSchoolAvailabilities) {
                        if (otherAvailability.getDayOfWeek().equals(result.getDayOfWeek())) {
                            // Check if travel time causes conflict
                            if (hasInterSchoolConflict(earliestArrival, latestDeparture, 
                                                     otherAvailability.getStartTime(), 
                                                     otherAvailability.getEndTime())) {
                                
                                ConflictDetectionDTO.ConflictDTO conflict = new ConflictDetectionDTO.ConflictDTO();
                                conflict.setConflictingAvailabilityId(otherAvailability.getId());
                                conflict.setConflictStartTime(otherAvailability.getStartTime());
                                conflict.setConflictEndTime(otherAvailability.getEndTime());
                                conflict.setConflictType("INTER_SCHOOL_TRAVEL");
                                conflict.setDescription(String.format(
                                    "Insufficient travel time (%d minutes) between schools. " +
                                    "Conflicts with %s slot at another school from %s to %s",
                                    travelTime,
                                    otherAvailability.getAvailabilityType().getDisplayName(),
                                    otherAvailability.getStartTime(),
                                    otherAvailability.getEndTime()));
                                conflict.setConflictingSchoolId(assignment.getSchoolId());
                                
                                result.addConflict(conflict);
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Check working day conflicts
    private void checkWorkingDayConflicts(ConflictDetectionDTO result) {
        if (result.getSchoolId() == null) {
            return;
        }
        
        // Check if teacher is assigned to work at this school on this day
        TeacherSchoolAssignment assignment = assignmentRepository
            .findByTeacherIdAndSchoolIdAndIsActiveTrue(result.getTeacherId(), result.getSchoolId())
            .orElse(null);
        
        if (assignment != null && !assignment.isWorkingDay(result.getDayOfWeek())) {
            ConflictDetectionDTO.ConflictDTO conflict = new ConflictDetectionDTO.ConflictDTO();
            conflict.setConflictType("WORKING_DAY");
            conflict.setDescription(String.format(
                "Teacher is not assigned to work at this school on %s", 
                result.getDayOfWeek()));
            conflict.setConflictingSchoolId(result.getSchoolId());
            
            result.addConflict(conflict);
        }
        
        // Check time restrictions
        if (assignment != null && assignment.getStartTime() != null && assignment.getEndTime() != null) {
            if (result.getStartTime().isBefore(assignment.getStartTime()) || 
                result.getEndTime().isAfter(assignment.getEndTime())) {
                
                ConflictDetectionDTO.ConflictDTO conflict = new ConflictDetectionDTO.ConflictDTO();
                conflict.setConflictType("TIME_RESTRICTION");
                conflict.setDescription(String.format(
                    "Requested time is outside teacher's working hours (%s - %s) at this school",
                    assignment.getStartTime(), assignment.getEndTime()));
                conflict.setConflictingSchoolId(result.getSchoolId());
                
                result.addConflict(conflict);
            }
        }
    }
    
    // Helper method to check inter-school conflicts
    private boolean hasInterSchoolConflict(LocalTime earliestArrival, LocalTime latestDeparture,
                                          LocalTime otherStart, LocalTime otherEnd) {
        // Check if the time slots overlap considering travel time
        return !(latestDeparture.isBefore(otherStart) || earliestArrival.isAfter(otherEnd));
    }
    
    // Quick conflict check (simplified version)
    public boolean hasConflicts(Long teacherId, DayOfWeek dayOfWeek, 
                               LocalTime startTime, LocalTime endTime) {
        List<TeacherAvailability> conflicts = availabilityRepository
            .findConflictingAvailabilities(teacherId, dayOfWeek, startTime, endTime);
        
        return !conflicts.isEmpty();
    }
    
    // Get all conflicts for a teacher on a specific day
    public List<ConflictDetectionDTO> getDayConflicts(Long teacherId, DayOfWeek dayOfWeek) {
        logger.info("Getting all conflicts for teacher {} on {}", teacherId, dayOfWeek);
        
        List<TeacherAvailability> dayAvailabilities = availabilityRepository
            .findByTeacherIdAndDayOfWeekAndIsActiveTrue(teacherId, dayOfWeek);
        
        return dayAvailabilities.stream()
            .map(availability -> checkConflicts(
                teacherId, 
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getSchoolId()))
            .filter(ConflictDetectionDTO::isHasConflicts)
            .collect(java.util.stream.Collectors.toList());
    }
}