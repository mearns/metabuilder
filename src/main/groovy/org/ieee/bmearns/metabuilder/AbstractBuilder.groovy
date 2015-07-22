
package org.ieee.bmearns.metabuilder;

abstract class AbstractBuilder<T, B extends AbstractBuilder> implements Builder<T> {

    @Override
    public T get() {
        return this.build();
    }

    public B update(Closure closure) {
        closure = closure.clone()
        closure.delegate = this
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure()

        return this
    }

    public B update(Map<String, Object> map) {
        map.each{ entry ->
            String k = entry.getKey()
            Object v = entry.getValue()
            this."$k"(v)
        }
        return this
    }

}

