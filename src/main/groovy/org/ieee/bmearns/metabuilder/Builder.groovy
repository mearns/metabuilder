
package org.ieee.bmearns.metabuilder;

import com.google.common.base.Supplier;

interface Builder<T> extends Supplier<T> {

    T build();

}

