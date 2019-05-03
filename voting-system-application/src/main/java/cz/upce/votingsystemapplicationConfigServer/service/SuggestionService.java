package cz.upce.votingsystemapplicationConfigServer.service;

import cz.upce.votingsystemapplicationConfigServer.dao.SuggestionDao;
import cz.upce.votingsystemapplicationConfigServer.dto.SuggestionDto;
import cz.upce.votingsystemapplicationConfigServer.dto.SuggestionForMeetingDto;
import cz.upce.votingsystemapplicationConfigServer.model.Suggestion;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuggestionService {

  private SuggestionDao suggestionDao;
  private MeetingService meetingService;
  private final static Logger LOGGER = Logger.getLogger(SuggestionService.class.getName());

  @Autowired
  public SuggestionService(SuggestionDao suggestionDao, MeetingService meetingService) {
    this.meetingService = meetingService;
    this.suggestionDao = suggestionDao;
  }

  public List<Suggestion> findAll() {
    return suggestionDao.findAll();
  }

  public SuggestionDto findById(Long id) {

    if (suggestionDao.findById(id).isPresent()) {
      Suggestion suggestion = suggestionDao.findById(id).get();
      return mapSuggestionToDto(suggestion);
    } else {
      //i think logging a message and returning null value is better than throwing an exception
      LOGGER.log(Level.WARNING, String.format("Suggestion with id %d does not exist in the database.", id));
      return null;
    }
  }

  public List<SuggestionForMeetingDto> findAllSuggestionsOnMeeting(Long id){

    List<Suggestion> all = suggestionDao.findAll();
    List<SuggestionForMeetingDto> dtoOutList = new ArrayList<>();

    all.removeIf(suggestion -> !suggestion.getMeetingId().equals(id));
    if(all.isEmpty()) {
      LOGGER.log(Level.WARNING, String.format("Meeting with id %d does not exist in the database."
          + " So there are no suggestions for this meeting", id));
      return null;
    } else {
      all.forEach(suggestion -> {
        dtoOutList.add(mapSuggestionToDtoForMeeting(suggestion));
      });
    }
    return dtoOutList;
  }

  public void add(Suggestion suggestion) {
    suggestionDao.save(suggestion);
  }

  public void deleteById(Long id) {
    try{
      suggestionDao.deleteById(id);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("Suggestion with id %d could not be deleted.", id));
    }
  }

  public void delete(Suggestion suggestion) {
    try{
      suggestionDao.delete(suggestion);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("Suggestion with id %d could not be deleted.", suggestion.getId()));
    }
  }

  public void markAsAccepted(Long suggestionId) {
    markAcceptance(suggestionId, true);
  }

  public void markAsRejected(Long suggestionId) {
    markAcceptance(suggestionId, false);
  }

  private void markAcceptance(Long suggestionId, boolean b) {
    Suggestion suggestion = suggestionDao.getOne(suggestionId);
    suggestion.setAccepted(b);
    suggestionDao.save(suggestion);
  }

  private SuggestionDto mapSuggestionToDto(Suggestion suggestion) {
    SuggestionDto dtoOut = new SuggestionDto();
    dtoOut.setId(suggestion.getId());
    dtoOut.setContent(suggestion.getContent());
    dtoOut.setAccepted(suggestion.getAccepted());

    dtoOut.setMeeting(meetingService.findByIdForSuggestion(suggestion.getMeetingId()));
    return dtoOut;
  }

  private SuggestionForMeetingDto mapSuggestionToDtoForMeeting(Suggestion suggestion) {
    SuggestionForMeetingDto dtoOut = new SuggestionForMeetingDto();
    dtoOut.setId(suggestion.getId());
    dtoOut.setContent(suggestion.getContent());
    dtoOut.setAccepted(suggestion.getAccepted());

    return dtoOut;
  }
}
