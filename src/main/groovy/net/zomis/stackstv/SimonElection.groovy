package net.zomis.stackstv

import java.util.stream.Collectors

class SimonElection implements ElectionStrategy {

    @Override
    ElectionResult elect(Election election) {
        List<Round> rounds = new ArrayList<>()

        int electedCount = 0
        int roundsCount = 0
        double previousExcess = 0
        while (electedCount < election.availablePositions) {
            Round round = new Round(roundsCount, election.maxChoices)
            rounds << round
            double roundQuota = election.calculateQuota(previousExcess)
            roundsCount++
            round.quota = roundQuota
            election.candidates*.votes = 0
            election.votes*.distribute(round)
            List<Candidate> elected = election.candidates.stream()
                    .filter({candidate -> candidate.votes > roundQuota})
                    .collect(Collectors.toList())
            elected.each {
                if (it.state != Election.CandidateState.ELECTED) {
                    electedCount++
                }
                it.state = Election.CandidateState.ELECTED
                it.weighting *= roundQuota / it.votes
            }
            if (elected.isEmpty()) {
                Candidate loser = election.candidates.stream()
                        .filter({it.state == Election.CandidateState.HOPEFUL})
                        .min(Comparator.comparingDouble({it.votes})).get()
                loser.state = Election.CandidateState.EXCLUDED
                loser.weighting = 0
            }
            round.candidates = election.candidates.collect {it.copy()}
            previousExcess = round.excess
        }
        new ElectionResult(rounds: rounds, candidateResults: election.candidates)
    }

}
