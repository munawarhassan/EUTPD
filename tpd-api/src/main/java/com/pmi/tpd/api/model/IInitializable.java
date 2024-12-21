package com.pmi.tpd.api.model;

/**
 * Implemented by entities which require secondary initialization to ensure Hibernate relationships are loaded prior to
 * traversing a transaction boundary. Entities which have lazy associations will generally need to implement this
 * interface, and should have handling built into their DAO to ensure it is invoked.
 * <p/>
 * Because entities can have relationships with entities which, themselves, have other relationships, some entities may
 * need to initialize other entities. When implementing such logic, care must be taken to avoid circular initialization.
 * To generalize the solution used there:
 * <ul>
 * <li>The <i>owning</i> entity uses a simple {@code Hibernate.initialize(Object)} on the dependent entity <i>and</i> on
 * specific fields within that entity.</li>
 * <li>The <i>dependent</i> entity uses {@link com.pmi.tpd.database.hibernate.HibernateUtils#initialize(Object)} on the
 * owning entity.</li>
 * </ul>
 * This prevents circular initialization, and also ensures that, if the graph is loaded from a <i>dependent</i> entity
 * instead of from the <i>owning</i> entity, everything will still be reliably initialized.
 *
 * @since 1.0
 * @see HibernateUtils
 * @author devacfr
 */
public interface IInitializable {

    /**
     * Implementations should {@link com.pmi.tpd.database.hibernate.HibernateUtils} and
     * {@code Hibernate.initialize(Object)} to ensure all lazy collections and associations are initialized. The goal is
     * to ensure that, after the entity has traversed a transaction boundary and no Hibernate session is available, all
     * fields are ready to use and will never trigger {@code LazyInitializationException}s
     * <p/>
     * In general, fields with the following annotations should be initialized:
     * <ul>
     * <li>{@code &#064;ManyToOne}</li>
     * <li>{@code &#064;OneToOne}</li>
     * <li>{@code &#064;OneToMany}</li>
     * <li>{@code &#064;ManyToMany}</li>
     * </ul>
     * When initializing {@code *ToMany} relationships, the code must initialize the collection itself <i>and then</i>
     * every member in the collection. For example: <code><pre>
     *     &#064;ManyToOne(...)
     *     private List&lt;InternalEntity&gt; collection;
     *
     *     public List&lt;InternalEntity&gt; getCollection() {
     *         return collection;
     *     }
     *
     *     &#064;Override
     *     public void initialize() {
     *         Hibernate.initialize(getCollection());
     *         for (InternalEntity entity : getCollection()) {
     *             Hibernate.initialize(entity);
     *         }
     *     }
     * </pre></code> If {@code InternalEntity} implements {@code Initializable},
     * {@link com.pmi.tpd.database.hibernate.HibernateUtils#initialize(Object)} should be used instead of
     * {@code Hibernate.initialize(Object)}, inside the loop. Note that the {@code getCollection()} accessor was used to
     * get the collection, rather than directly referring to the private field. This is a best practice when dealing
     * with Hibernate entities.
     * <p/>
     * As noted in the class-level documentation, when implementing this method in classes that have bidirectional
     * associations, carefully review the implementations to ensure {@code initialize()} calls don't result in circular
     * initialization and {@code StackOverflowError}s.
     */
    void initialize();

}
