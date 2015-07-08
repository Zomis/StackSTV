html {
    head {
        title(title)
    }
    body {
        rounds.each {
            h1('Round ' + it.round)
            p(it)
        }
    }
}