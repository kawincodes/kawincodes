package com.kawincodes.ksession.core.tunnel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kawincodes.ksession.AppGraph;

import java.util.ArrayList;
import java.util.List;

/** Saved tunnel profiles, persisted as JSON in filesDir/profiles.json. Thread-safe. */
public final class TunnelRepository {

    private final AppGraph graph;
    private final MutableLiveData<List<TunnelProfile>> profiles = new MutableLiveData<>(new ArrayList<>());

    public TunnelRepository(AppGraph graph) {
        this.graph = graph;
    }

    /** All profiles ordered by creation time (copies). */
    public LiveData<List<TunnelProfile>> profiles() {
        return profiles;
    }

    /** Synchronous copy of all profiles. */
    public List<TunnelProfile> all() {
        throw new UnsupportedOperationException("TODO runtime");
    }

    @Nullable
    public TunnelProfile get(String id) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Case-insensitive exact name match, then unique prefix match, then null. */
    @Nullable
    public TunnelProfile findByName(String name) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Inserts or replaces by id and persists. */
    public void save(TunnelProfile profile) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** Removes the profile (secrets are removed by TunnelProvisioner). */
    public void delete(String id) {
        throw new UnsupportedOperationException("TODO runtime");
    }

    /** A name not used by any profile, e.g. "quick-2". */
    public String uniqueName(String base) {
        throw new UnsupportedOperationException("TODO runtime");
    }
}
