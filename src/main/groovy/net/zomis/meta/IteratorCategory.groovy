package net.zomis.meta

/*
 * Returns a Closure which iterates while the condition Closure 
 * evaluates to true. The returned Closure expects another Closure, 
 * an action closure, as its single argument.
 * This 'action' Closure is called during each iteration and is passed 
 * the Iterator.next()value. 
 * When the iteration is complete, the Iterator is returned.
 *
 * Example usage: 
 * use(IteratorCategory) {
 *   def iter = reader.iterator().while { it != 'end' }.call { println it }
 * }
 *
 * @param condition Closure to evaluate on each iteration.
 * @return a Closure
 */
@groovy.lang.Category(Iterator)
class IteratorCategory {
    Closure 'while'(Closure condition) {
        {Iterator iter, Closure closure ->
            while(iter.hasNext()) {
                def it = iter.next()
                
                if(condition(it)) {
                    closure(it)
                } else {
                    break
                }
            }
            
            return iter
        }.curry(this)
    }
}
