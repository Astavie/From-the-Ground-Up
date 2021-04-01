package ftgumod.util;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.function.Predicate;

public class SubCollection<T> extends AbstractCollection<T> {

	private final Iterable<? extends T> iterable;
	private final Predicate<T> predicate;

	public SubCollection(Iterable<? extends T> iterable, Predicate<T> predicate) {
		this.iterable = iterable;
		this.predicate = predicate;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			private final Iterator<? extends T> iterator = iterable.iterator();
			private T next = getNext();

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public T next() {
				T t = next;
				next = getNext();
				return t;
			}

			private T getNext() {
				T t;
				do {
					if (iterator.hasNext())
						t = iterator.next();
					else
						t = null;
				} while (t != null && !predicate.test(t));
				return t;
			}

		};
	}

	@Override
	public int size() {
		int i = 0;
		for (T t : iterable)
			if (predicate.test(t))
				i++;
		return i;
	}

}
