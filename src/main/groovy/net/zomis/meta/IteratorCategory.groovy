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

    /*
     * Similar to Number.upto(Number, Closure), executes the Closure
     * UP TO a specified number of times. However, instead of returning
     * the Closure's return value, it returns the Iterator where
     * it left off.
     *
     * Example usage:
     * use(IteratorCategory) {
     *   def iter = reader.iterator().upto(5) {it, i -> println "$i - $it" }
     * }
     *
     * @param to number of times to iterate
     * @param closure to execute. Called with Iterator.next() and index.
     * @return Iterator
     */
    Iterator upto(int to, Closure closure) {
        int i = 0

        while(this.hasNext() && i < to) {
            closure this.next(), i
            i++
        }

        return this
    }
}
