package com.pmi.tpd.cluster.concurrent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

/**
 * Implementation of {@link ITransferableState} that is a container of {@link ITransferableState} instances. When
 * {@link #apply()} is called, {@link ITransferableState#apply()} is called on all contained states. Similarly, when
 * {@link #remove()} is called, {@link ITransferableState#remove()} is called on all contained states.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class CompositeTransferableState implements ITransferableState {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeTransferableState.class);

    /** */
    private final List<ITransferableState> states;

    /**
     * @param states
     */
    public CompositeTransferableState(final Iterable<ITransferableState> states) {
        this.states = ImmutableList.copyOf(states);
    }

    @Override
    public void apply() {
        for (final ITransferableState state : states) {
            try {
                state.apply();
            } catch (final Exception e) {
                LOGGER.warn("Failed to apply thread state", e);
            }
        }
    }

    @Override
    public void remove() {
        for (final ITransferableState state : states) {
            try {
                state.remove();
            } catch (final Exception e) {
                LOGGER.warn("Failed to remove thread state", e);
            }
        }
    }

    @VisibleForTesting
    List<ITransferableState> getStates() {
        return states;
    }
}
