import groovy.json.JsonBuilder
import net.zomis.stackstv.Election
import net.zomis.stackstv.Round

html {
    head {
        title(title)
        meta('http-equiv': 'Content-Type', content: 'text/html; charset=utf-8')
        link(href: 'examples.css', rel: 'stylesheet', type: 'text/css')
        include unescaped: 'script.html'

        yieldUnescaped "var datasets = " + getCandidateJSON()

        include unescaped: 'script-cont.html'
    }
    body {
        div(id: 'header') {
            h2('Election Data')
        }

        div(id: 'content') {
            div(class: 'demo-container') {
                div(id: 'placeholder', class: 'demo-placeholder', style: 'float:left; width:675px;') {

                }
                p(id: 'choices', style: 'float:right; width:135px;')
            }
            p('This shows the candidate votes after each step in the election.')
        }

        rounds.each {
            h1('Round ' + it.round)
            p(it)
        }
    }
}