package com.gov.doc.engine.service;

import com.gov.doc.engine.entity.WfParticipant;

import java.util.List;
import java.util.Map;

public interface WfParticipantResolverService {

    List<ResolvedParticipant> resolveParticipants(List<WfParticipant> participants, Map<String, Object> variables);

    ResolvedParticipant resolveParticipant(WfParticipant participant, Map<String, Object> variables);

    class ResolvedParticipant {
        private String userId;
        private String userName;
        private String participantType;
        private String participantValue;
        private String participantName;

        public ResolvedParticipant() {
        }

        public ResolvedParticipant(String userId, String userName, String participantType, String participantValue, String participantName) {
            this.userId = userId;
            this.userName = userName;
            this.participantType = participantType;
            this.participantValue = participantValue;
            this.participantName = participantName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getParticipantType() {
            return participantType;
        }

        public void setParticipantType(String participantType) {
            this.participantType = participantType;
        }

        public String getParticipantValue() {
            return participantValue;
        }

        public void setParticipantValue(String participantValue) {
            this.participantValue = participantValue;
        }

        public String getParticipantName() {
            return participantName;
        }

        public void setParticipantName(String participantName) {
            this.participantName = participantName;
        }
    }
}
