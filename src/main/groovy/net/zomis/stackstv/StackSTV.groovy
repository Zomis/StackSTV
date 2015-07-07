package net.zomis.stackstv

class StackSTV {

    private final List<String> candidates = new ArrayList<>()
    private final List<String> votes = new ArrayList<>()
    final int availablePositions

    StackSTV(int availablePositions) {
        this.availablePositions = availablePositions
    }

    void addCandidates(String... names) {

    }

    static class Votes {
        int numVotes
        int[] candidates
        float[] distribution
    }

    static final StackSTV fromURL(URL url) {
        BufferedReader reader = url.newReader()
        String[] head = reader.readLine().split()
        int candidates = head[0] as int
        println candidates
        StackSTV stv = new StackSTV(head[1] as int)
        stv
    }

}
