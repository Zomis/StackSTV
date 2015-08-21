package net.zomis.stackstv

class SimonElection implements ElectionStrategy {

    @Override
    ElectionResult elect(Election election) {
        def rounds = [] 
        def electedCount = 0
        def roundsCount = 0
        def previousExcess = 0

        def positionsAreAvailable = {
            electedCount < election.availablePositions
        }

        def createRoundAndQuota = {
            def round = new Round(roundsCount, election.maxChoices)
            def roundQuota = election.calculateQuota(previousExcess)

            rounds << round
            roundsCount++
            round.quota = roundQuota
            election.candidates*.votes = 0
            election.votes*.distribute(round)
            
            [round, roundQuota]
        }

        def electCandidate = {quota, candidate ->
            candidate.with {
                if (state != Election.CandidateState.ELECTED) {
                    electedCount++
                }

                state = Election.CandidateState.ELECTED
                weighting *= quota / votes
            }
        }

        while (positionsAreAvailable()) {
            def (round, roundQuota) = createRoundAndQuota()
            def elected = election.candidates
                .findAll {candidate -> candidate.votes > roundQuota}
                .each electCandidate.curry(roundQuota)

            if(!elected) {
                election.candidates
                    .findAll {it.state == Election.CandidateState.HOPEFUL}
                    .min {it.votes}
                    .with {
                        state = Election.CandidateState.EXCLUDED
                        weighting = 0
                    }
            }

            round.candidates = election.candidates.collect {it.clone()}
            previousExcess = round.excess
        }

        [rounds, election.candidates] as ElectionResult
    }

}
