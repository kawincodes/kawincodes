package com.kawincodes.ksession.ui.radar;

/** Something drawn on the radar. */
public final class RadarBlip {
    public enum Kind {
        /** A tunnel (inner area). */
        TUNNEL,
        /** A Cloudflare data center the tunnel is connected to (outer ring), labelled with its code. */
        EDGE
    }

    /** Stable id (profile id, or profile id + ":" + connIndex). */
    public final String id;
    public final Kind kind;
    /** Short label, e.g. tunnel name or "BOM". */
    public final String label;
    /** Degrees clockwise from north, 0-360. */
    public final float angle;
    /** 0 (center) to 1 (outer ring). */
    public final float radius;
    /** ARGB color. */
    public final int color;
    /** Draw an expanding pulse ring (e.g. just connected). */
    public final boolean pulse;

    public RadarBlip(String id, Kind kind, String label, float angle, float radius, int color, boolean pulse) {
        this.id = id;
        this.kind = kind;
        this.label = label;
        this.angle = angle;
        this.radius = radius;
        this.color = color;
        this.pulse = pulse;
    }
}
