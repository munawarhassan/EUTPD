export class Optional<T> {
    private static EMPTY = new Optional<any>();

    private value: T | undefined;

    /**
     * Returns an empty Optional instance.  No value is present for this
     * Optional.
     */
    public static empty<E>(): Optional<E> {
        return Optional.EMPTY;
    }

    /**
     * Returns an Optional with the specified present non-null value.
     *
     * @param value the value to be present, which must be non-null
     */
    public static of<E>(value: E): Optional<E> {
        return new Optional<E>(value);
    }

    /**
     * Returns an Optional describing the specified value, if non-null,
     * otherwise returns an empty Optional.
     */
    public static ofNullable<E>(value: E): Optional<E> {
        return value == null ? Optional.empty() : Optional.of(value);
    }

    /**
     * Constructs an instance with the value present.
     *
     * @param value the non-null value to be present
     */
    private constructor(value?: T) {
        this.value = value;
    }

    /**
     * If a value is present in this Optional, returns the value,
     * otherwise throws Error.
     */
    public get(): T {
        if (this.value == null) {
            throw new Error('No value present');
        }
        return this.value;
    }

    /**
     * Return true if there is a value present, otherwise false.
     */
    public isPresent(): boolean {
        return this.value != null;
    }

    /**
     * If a value is present, invoke the specified consumer with the value,
     * otherwise do nothing.
     *
     * @param consumer  block to be executed if a value is present
     */
    public ifPresent(consumer: (value: T) => void): void {
        if (this.value != null) consumer(this.value);
    }

    /**
     * If a value is present, and the value matches the given predicate,
     * return an Optional describing the value, otherwise return an
     * empty Optional.
     *
     * @param predicate a predicate to apply to the value, if present
     */
    public filter(predicate: (value: T) => boolean): Optional<T> {
        if (!this.isPresent()) return this;
        else return predicate(this.value as T) ? this : Optional.empty();
    }

    /**
     * If a value is present, apply the provided mapping function to it,
     * and if the result is non-null, return an Optional describing the
     * result.  Otherwise return an empty Optional.
     *
     * @param mapper a mapping function to apply to the value, if present
     */
    public map<U>(mapper: (value: T) => U): Optional<U> {
        if (!this.isPresent()) return Optional.empty();
        else {
            return Optional.ofNullable(mapper(this.value as T));
        }
    }

    /**
     * If a value is present, apply the provided Optional-bearing
     * mapping function to it, return that result, otherwise return an empty
     * Optional.  This method is similar to map(Function),
     * but the provided mapper is one whose result is already an Optional,
     * and if invoked, flatMap does not wrap it with an additional
     * {@code Optional}.
     *
     * @param mapper a mapping function to apply to the value, if present
     *           the mapping function
     */
    public flatMap<U>(mapper: (value: T) => Optional<U>): Optional<U> {
        if (!this.isPresent()) return Optional.empty();
        else {
            return mapper(this.value as T);
        }
    }

    /**
     * Return the value if present, otherwise return ther.
     *
     * @param other the value to be returned if there is no value present, may
     * be null
     */
    public orElse(other: T): T {
        return this.value != null ? this.value : other;
    }

    /**
     * Return the value if present, otherwise invoke other and return
     * the result of that invocation.
     *
     * @param other a Supplier whose result is returned if no value
     * is present.
     */
    public orElseGet(other: () => T): T {
        return this.value != null ? this.value : other();
    }
}
