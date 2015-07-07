package net.zomis.stackstv

class StackSTV {

    private final List<String> candidates = new ArrayList<>()
    private final List<Vote> votes = new ArrayList<>()
    final int availablePositions

    StackSTV(int availablePositions) {
        this.availablePositions = availablePositions
    }

    void addCandidate(String name) {
        this.candidates.add(name)
    }

    List<String> getCandidates() {
        new ArrayList<>(candidates)
    }

    float getQuota() {
        (float) votes.size() / (availablePositions + 1)
    }

    static class Vote {
        int numVotes
        int[] candidates
        float[] distribution

        static Vote fromLine(String line) {
            String[] data = line.split()
            Vote vote = new Vote()
            vote.numVotes = data[0] as int
            int candidateVotes = data.length - 2
            vote.candidates = new int[candidateVotes]
            for (int i = 0; i < vote.candidates.length; i++) {
                vote.candidates[i] = data[i + 1] as int
            }
            println vote.candidates
            vote
        }
    }

    static final StackSTV fromURL(URL url) {
        BufferedReader reader = url.newReader()
        String[] head = reader.readLine().split()
        int candidates = head[0] as int
        println candidates
        StackSTV stv = new StackSTV(head[1] as int)

        String line = reader.readLine();
        while (line != '0') {
            Vote vote = Vote.fromLine(line)
            stv.votes << vote
            line = reader.readLine();
        }
        for (int i = 0; i < candidates; i++) {
            String name = reader.readLine()
            println name
            stv.addCandidate(name)
        }
        stv
    }

}
